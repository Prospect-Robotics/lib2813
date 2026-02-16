/*
Copyright 2026 Prospect Robotics SWENext Club

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.team2813.lib2813.control.motor;

import com.team2813.lib2813.control.ControlMode;
import com.team2813.lib2813.control.Encoder;
import com.team2813.lib2813.control.Motor;
import com.team2813.lib2813.control.PIDMotor;
import com.team2813.lib2813.robot.PeriodicRegistry;
import com.team2813.lib2813.robot.RobotState;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.networktables.BooleanPublisher;
import edu.wpi.first.networktables.DoublePublisher;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.units.AngleUnit;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.util.sendable.SendableRegistry;
import edu.wpi.first.wpilibj.DriverStation;
import java.util.Objects;
import java.util.function.Supplier;
import org.apiguardian.api.API;

/**
 * A wrapper around a motor and a PID controller that supports predefined positions.
 *
 * <p>{@code PositionalMotor} supports a dual operation mode:
 *
 * <ul>
 *   <li><b>PID Control Mode</b> - the user set a destination position (aka "setpoint") and the
 *       motor moves towards the setpoint under the control of the PID controller.
 *   <li><b>Direct User Input Mode</b> - the subsystem responds to direct input from the user (i.e.,
 *       voltage or duty cycle).
 * </ul>
 *
 * <p>When the motor is created, PID control is disabled. The current mode of the motor can be
 * determined by calling {@link #isPIDControlEnabled()}.
 *
 * <p>To enable <b>PID Control Mode</b> call {@link #setSetpoint(P)}; the motor will move toward the
 * setpoint and then maintain position at the setpoint under the control of the PID controller.
 *
 * <p>The <b>Direct User Input Mode</b> is activated when the user calls the {@link
 * #set(ControlMode, double)}. The PID Mode is interrupted and disengaged, and the provided demand
 * will be directly sent to the motor.
 *
 * <p>To stop the motor, call {@link #stopMotor()}.
 *
 * @param <P> an enum implementing {@link Supplier<Angle>} used to specify setpoints.
 */
@API(status = API.Status.EXPERIMENTAL)
public final class PositionalMotor<P extends Enum<P> & Supplier<Angle>>
    implements AutoCloseable, Motor, Sendable {
  /** The default error tolerance for the controller. */
  public static final double DEFAULT_TOLERANCE = 0.05;

  private final Motor motor;
  private final Encoder encoder;
  private final PIDController controller;
  private final Clamper clamper;
  private final Publishers publishers;
  private final ControlMode controlMode;
  private final AngleUnit rotationUnit;
  private boolean isPIDControlEnabled;

  /**
   * Creates a new builder for a {@code PositionalMotor} using a motor that has a built-in encoder.
   *
   * <p>The default error tolerance is {@value #DEFAULT_TOLERANCE}, the PID constants are set to 0,
   * and the rotational unit is set to {@link Units#Rotations}.
   *
   * @param periodicRegistry periodic registry that the motor should use
   * @param motor the integrated motor controller
   * @return builder instance
   */
  public static Builder builder(PeriodicRegistry periodicRegistry, PIDMotor motor) {
    return new Builder(periodicRegistry, motor, motor);
  }

  /**
   * Creates a new builder for a {@code PositionalMotor}.
   *
   * <p>The default error tolerance is {@value #DEFAULT_TOLERANCE}, the PID constants are set to 0,
   * and the rotational unit is set to {@link Units#Rotations}.
   *
   * @param periodicRegistry periodic registry that the motor should use
   * @param motor the motor to control
   * @param encoder the encoder providing feedback
   * @return builder instance
   */
  public static Builder builder(PeriodicRegistry periodicRegistry, Motor motor, Encoder encoder) {
    return new Builder(periodicRegistry, motor, encoder);
  }

  /** A builder for a {@code PositionalMotor}. */
  public static class Builder {

    private final PeriodicRegistry periodicRegistry;
    private final Motor motor;
    private final Encoder encoder;
    private final PIDController controller;
    private ControlMode controlMode = ControlMode.DUTY_CYCLE;
    private AngleUnit rotationUnit = Units.Rotations;
    private Clamper clamper = value -> value;
    private NetworkTable networkTable;

    private Builder(PeriodicRegistry periodicRegistry, Motor motor, Encoder encoder) {
      this.periodicRegistry =
          Objects.requireNonNull(periodicRegistry, "periodicRegistry should not be null");
      this.motor = Objects.requireNonNull(motor, "motor should not be null");
      this.encoder = Objects.requireNonNull(encoder, "encoder should not be null");
      controller = new PIDController(0, 0, 0);
      controller.setTolerance(DEFAULT_TOLERANCE);
    }

    /**
     * Sets the control mode to use when giving output to the motor. Defaults to {@link
     * ControlMode#DUTY_CYCLE}.
     *
     * @param controlMode The mode to use when controlling the motor
     * @return {@code this} for chaining
     * @throws IllegalArgumentException If {@code controlMode} is for positional control
     */
    public Builder controlMode(ControlMode controlMode) {
      if (controlMode.isPositionalControl()) {
        throw new IllegalArgumentException(
            String.format(
                "Control mode %s is for positional control. This is invalid! Please use a different"
                    + " control mode",
                controlMode));
      }
      this.controlMode = controlMode;
      return this;
    }

    /**
     * Sets the PID constants for the controller
     *
     * @param p the proportional coefficient
     * @param i the integral coefficient
     * @param d the derivative coefficient
     * @return {@code this} for chaining
     */
    public Builder PID(double p, double i, double d) {
      controller.setPID(p, i, d);
      return this;
    }

    /**
     * Sets the error which is considered tolerable for use with {@link #atSetpoint()}.
     *
     * @param errorTolerance Error which is tolerable
     * @return {@code this} for chaining
     */
    public Builder tolerance(double errorTolerance) {
      controller.setTolerance(errorTolerance);
      return this;
    }

    /**
     * Sets the error values which are considered tolerable for use with {@link #atSetpoint()}.
     *
     * @param errorTolerance Error which is tolerable
     * @param errorDerivativeTolerance Error derivative which is tolerable
     * @return {@code this} for chaining
     */
    public Builder tolerance(double errorTolerance, double errorDerivativeTolerance) {
      controller.setTolerance(errorTolerance, errorDerivativeTolerance);
      return this;
    }

    /**
     * Sets the low and high values for the output to use when PID control is enabled.
     *
     * @param low The lower boundary to which to clamp output values.
     * @param high The higher boundary to which to clamp output values.
     * @return {@code this} for chaining
     */
    public Builder limits(double low, double high) {
      if (low >= high) {
        throw new IllegalArgumentException("Low value must be smaller than high value");
      }
      clamper = output -> MathUtil.clamp(output, low, high);
      return this;
    }

    /**
     * Sets the function to use to clamp output values when PID control is enabled.
     *
     * @param clamper Function to use to clamp output values
     * @return {@code this} for chaining
     */
    public Builder clamper(Clamper clamper) {
      this.clamper = Objects.requireNonNull(clamper, "clamper should not be null");
      return this;
    }

    /**
     * Sets the unit to use for PID calculations
     *
     * @param rotationUnit The angle unit to use for calculations
     */
    public Builder rotationUnit(AngleUnit rotationUnit) {
      this.rotationUnit = rotationUnit;
      return this;
    }

    /**
     * Publish metrics about the motor and controller to the provided table.
     *
     * @param networkTable Table to publish to
     * @return {@code this} for chaining
     */
    public Builder publishTo(NetworkTable networkTable) {
      this.networkTable = Objects.requireNonNull(networkTable, "networkTable should not be null");
      return this;
    }

    /**
     * Builds a {@code PositionalMotor} using the current values of this builder.
     *
     * @param initialPosition the initial position of the controller
     */
    public <P extends Enum<P> & Supplier<Angle>> PositionalMotor<P> build(P initialPosition) {
      Objects.requireNonNull(initialPosition, "initialPosition should not be null");
      return new PositionalMotor<>(this, initialPosition);
    }
  }

  private PositionalMotor(Builder builder, P initialPosition) {
    controller = builder.controller;
    motor = builder.motor;
    encoder = builder.encoder;
    controlMode = builder.controlMode;
    rotationUnit = builder.rotationUnit;
    clamper = builder.clamper;

    if (builder.networkTable != null) {
      publishers = new Publishers(builder.networkTable);
    } else {
      publishers = null;
    }

    double initialPositionAsDouble = initialPosition.get().in(rotationUnit);
    controller.setSetpoint(initialPositionAsDouble);
    encoder.setPosition(rotationUnit.of(initialPositionAsDouble));

    builder.periodicRegistry.addPeriodic(this::periodic);

    PIDSendable pidSendable = new PIDSendable();
    SendableRegistry.add(pidSendable, "Controller");
    SendableRegistry.addChild(this, pidSendable);
  }

  @Override
  public void initSendable(SendableBuilder builder) {
    builder.setSmartDashboardType("PositionalMotor");
    builder.setActuator(true);
    builder.setSafeState(this::stopMotor);

    builder.addBooleanProperty(
        "pidControlEnabled",
        this::isPIDControlEnabled,
        enabled -> {
          if (enabled) {
            enablePIDControl();
          } else {
            stopMotor();
          }
        });
    builder.publishConstDouble("position", encoder.getPositionMeasure().in(rotationUnit));
    builder.publishConstString("rotationUnit", rotationUnit.name());
  }

  /**
   * Sets the desired setpoint to the provided value, and enables the PID control.
   *
   * @param position the position to go to.
   */
  public void setSetpoint(P position) {
    Objects.requireNonNull(position, "position should not be null");
    if (!isPIDControlEnabled) {
      enablePIDControl();
    }
    double setpoint = position.get().in(rotationUnit);
    controller.setSetpoint(setpoint);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Additionally, this method disables PID control of the subsystem
   */
  @Override
  public void set(ControlMode mode, double demand) {
    isPIDControlEnabled = false;
    motor.set(mode, demand);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Additionally, this method disables PID control of the subsystem
   */
  @Override
  public void set(ControlMode mode, double demand, double feedForward) {
    isPIDControlEnabled = false;
    motor.set(mode, demand, feedForward);
  }

  @Override
  public Current getAppliedCurrent() {
    return motor.getAppliedCurrent();
  }

  /**
   * Engages the PID Controller.
   *
   * <p>The motor voltage will be periodically updated to move the motor towards the current
   * setpoint.
   *
   * <p>If this method is called after the motor drive toward setpoint was interrupted by user
   * interruption, the motor will resume its movement towards the last set setpoint.
   */
  public void enablePIDControl() {
    isPIDControlEnabled = true;
  }

  /**
   * Returns whether PID control is enabled.
   *
   * @return Whether PID control is enabled.
   */
  public boolean isPIDControlEnabled() {
    return isPIDControlEnabled;
  }

  /**
   * Stops the motor.
   *
   * <p>The motor voltage will be set to zero, and the motor will not adjust to move towards the
   * current setpoint.
   */
  @Override
  public void stopMotor() {
    isPIDControlEnabled = false;
    motor.stopMotor();
  }

  /** Returns the current setpoint as an angle. */
  public Angle getSetpoint() {
    return rotationUnit.of(controller.getSetpoint());
  }

  /**
   * Gets the position of the encoder.
   *
   * @return the position of the encoder as a measure
   */
  public Angle getPositionMeasure() {
    return encoder.getPositionMeasure();
  }

  /** Determines if the motor is at the current setpoint, within the acceptable error. */
  public boolean atSetpoint() {
    return controller.atSetpoint();
  }

  /** Applies the PID output to the motor if this motor is enabled. */
  private void periodic(RobotState robotState) {
    if (isPIDControlEnabled) {
      double nextOutput = controller.calculate(getMeasurement());
      motor.set(controlMode, clamper.clampValue(nextOutput));
    }
    if (publishers != null) {
      publishers.positionPublisher.set(getPositionMeasure().in(rotationUnit));
      publishers.setpointPublisher.set(getSetpoint().in(rotationUnit));
      publishers.atSetpointPublisher.set(atSetpoint());
      publishers.appliedCurrentPublisher.set(motor.getAppliedCurrent().in(Units.Amps));
      publishers.pidControlEnabledPublisher.set(isPIDControlEnabled);
    }
  }

  @Override
  public void close() {
    if (publishers != null) {
      publishers.close();
    }
  }

  private double getMeasurement() {
    return encoder.getPositionMeasure().in(rotationUnit);
  }

  private record Publishers(
      DoublePublisher positionPublisher,
      BooleanPublisher atSetpointPublisher,
      DoublePublisher appliedCurrentPublisher,
      DoublePublisher setpointPublisher,
      BooleanPublisher pidControlEnabledPublisher) {

    Publishers(NetworkTable networkTable) {
      this(
          networkTable.getDoubleTopic("position").publish(),
          networkTable.getBooleanTopic("at setpoint").publish(),
          networkTable.getDoubleTopic("applied current").publish(),
          networkTable.getDoubleTopic("setpoint").publish(),
          networkTable.getBooleanTopic("PID control enabled").publish());
    }

    void close() {
      positionPublisher.close();
      atSetpointPublisher.close();
      appliedCurrentPublisher.close();
      setpointPublisher.close();
    }
  }

  /** A Sendable implementation that allows users to update PID settings. */
  private class PIDSendable implements Sendable {

    /**
     * {@inheritDoc
     *
     * <p>This is modeled after {@link PIDController#initSendable(SendableBuilder)},
     * but does not support changing the setpoint.
     */
    @Override
    public void initSendable(SendableBuilder builder) {
      builder.setSmartDashboardType("PIDController");
      builder.addDoubleProperty("p", controller::getP, controller::setP);
      builder.addDoubleProperty("i", controller::getI, controller::setI);
      builder.addDoubleProperty("d", controller::getD, controller::setD);
      builder.addDoubleProperty(
          "izone",
          controller::getIZone,
          (double toSet) -> {
            try {
              controller.setIZone(toSet);
            } catch (IllegalArgumentException e) {
              DriverStation.reportError("IZone must be a non-negative number!", e.getStackTrace());
            }
          });
      builder.addDoubleProperty(
          "acceptableError", controller::getErrorTolerance, controller::setTolerance);
    }
  }
}
