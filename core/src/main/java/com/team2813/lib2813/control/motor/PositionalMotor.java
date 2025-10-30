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
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.networktables.BooleanPublisher;
import edu.wpi.first.networktables.DoublePublisher;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.units.AngleUnit;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Angle;
import java.util.Objects;
import java.util.function.Supplier;
import org.apiguardian.api.API;

/**
 * A wrapper around a motor and a PID controller that supports predefined positions.
 *
 * <p>The creator is responsible for calling {@link #periodic()} periodically.
 *
 * <p>When the motor is created, PID control is disabled. To enable PID control, call {@link
 * #setSetpoint(P)}; the motor will moving toward the setpoint and maintains position at the
 * setpoint under the control of the PID controller. To stop the motor, call {@link #stopMotor()}.
 *
 * @param <P> an enum implementing {@link Supplier<Angle>} used to specify setpoints.
 */
@API(status = API.Status.EXPERIMENTAL)
public final class PositionalMotor<P extends Enum<P> & Supplier<Angle>> implements AutoCloseable {
  /** The default acceptable position error. */
  public static final double DEFAULT_ERROR = 5.0;

  private static final AngleUnit ROTATION_UNIT = Units.Rotations;
  private final Motor motor;
  private final Encoder encoder;
  private final PIDController controller;
  private final Clamper clamper;
  private final Publishers publishers;
  private final ControlMode controlMode;
  private final double acceptableError;
  private boolean isPIDControlEnabled;

  /**
   * Creates a new builder for a {@code PositionalMotor}.
   *
   * <p>The default acceptable error is {@value #DEFAULT_ERROR} and the PID constants are set to 0.
   *
   * @param motor the motor to control
   * @param encoder the encoder providing feedback
   * @param initialPosition the initial position of the controller
   * @return builder instance
   */
  public static <P extends Enum<P> & Supplier<Angle>> Builder<P> builder(
      Motor motor, Encoder encoder, P initialPosition) {
    return new Builder<>(motor, encoder, initialPosition);
  }

  /**
   * Creates a new builder for a {@code PositionalMotor} using a motor that has a built-in encoder.
   *
   * <p>The default acceptable error is {@value #DEFAULT_ERROR} and the PID constants are set to 0.
   *
   * @param motor the integrated motor controller
   * @param initialPosition the initial position of the controller
   * @return builder instance
   */
  public static <P extends Enum<P> & Supplier<Angle>> Builder<P> builder(
      PIDMotor motor, P initialPosition) {
    return new Builder<>(motor, motor, initialPosition);
  }

  /** A builder for a {@code PositionalMotor}. */
  public static class Builder<P extends Enum<P> & Supplier<Angle>> {

    private final Motor motor;
    private final Encoder encoder;
    private final double initialPosition;
    private ControlMode controlMode = ControlMode.DUTY_CYCLE;
    private PIDController controller;
    private double acceptableError = DEFAULT_ERROR;
    private Clamper clamper = value -> value;
    private NetworkTable networkTable;

    private Builder(Motor motor, Encoder encoder, P initialPosition) {
      this.motor = Objects.requireNonNull(motor, "motor should not be null");
      this.encoder = Objects.requireNonNull(encoder, "encoder should not be null");
      Objects.requireNonNull(initialPosition, "initialPosition should not be null");
      this.initialPosition = initialPosition.get().in(ROTATION_UNIT);
      controller = new PIDController(0, 0, 0);
    }

    /**
     * Sets the controller used to calculate the next value.
     *
     * @param controller The PID controller
     * @return {@code this} for chaining
     */
    public Builder<P> controller(PIDController controller) {
      this.controller = Objects.requireNonNull(controller, "controller should not be null");
      return this;
    }

    /**
     * Sets the control mode to use when giving output to the motor. Defaults to {@link
     * ControlMode#DUTY_CYCLE}.
     *
     * @param controlMode The mode to use when controlling the motor
     * @return {@code this} for chaining
     * @throws IllegalArgumentException If {@code controlMode} is for positional control
     */
    public Builder<P> controlMode(ControlMode controlMode) {
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
     * @param p the proportional
     * @param i the integral
     * @param d the derivative
     * @return {@code this} for chaining
     */
    public Builder<P> PID(double p, double i, double d) {
      controller.setPID(p, i, d);
      return this;
    }

    /**
     * Sets the acceptable position error.
     *
     * @return {@code this} for chaining
     */
    public Builder<P> acceptableError(double error) {
      this.acceptableError = error;
      return this;
    }

    /**
     * Sets the function to use to clamp output values.
     *
     * @param clamper Function to use to clamp output values
     * @return {@code this} for chaining
     */
    public Builder<P> clamper(Clamper clamper) {
      this.clamper = Objects.requireNonNull(clamper, "clamper should not be null");
      return this;
    }

    /**
     * Publish metrics about the motor and controller to the provided table.
     *
     * @param networkTable Table to publish to
     * @return {@code this} for chaining
     */
    public Builder<P> publishTo(NetworkTable networkTable) {
      this.networkTable = Objects.requireNonNull(networkTable, "networkTable should not be null");
      return this;
    }

    /** Builds a {@code PositionalMotor} using the current values of this builder. */
    public PositionalMotor<P> build() {
      return new PositionalMotor<>(this);
    }
  }

  private PositionalMotor(Builder<P> builder) {
    controller = builder.controller;
    acceptableError = builder.acceptableError;
    motor = builder.motor;
    encoder = builder.encoder;
    controlMode = builder.controlMode;
    clamper = builder.clamper;
    if (builder.networkTable != null) {
      publishers = new Publishers(builder.networkTable);
    } else {
      publishers = null;
    }

    controller.setTolerance(builder.acceptableError);
    controller.setSetpoint(builder.initialPosition);

    // TODO: Should we update the position of the controller using controller.calculate()?
    // TODO: Should we update the position of the encoder using encoder.setPosition()?
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
    double setpoint = position.get().in(ROTATION_UNIT);
    controller.setSetpoint(setpoint);
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
  public void stopMotor() {
    isPIDControlEnabled = false;
    motor.stopMotor();
  }

  /** Returns the current setpoint as an angle. */
  public Angle getSetpoint() {
    return ROTATION_UNIT.of(controller.getSetpoint());
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
  public boolean atPosition() {
    return Math.abs(getMeasurement() - controller.getSetpoint()) <= acceptableError;
  }

  /**
   * Applies the PID output to the motor if this motor is enabled.
   *
   * <p>Should be called from {@link edu.wpi.first.wpilibj2.command.Subsystem#periodic()}.
   */
  public void periodic() {
    if (isPIDControlEnabled) {
      double nextOutput = controller.calculate(getMeasurement());
      motor.set(controlMode, clamper.clampValue(nextOutput));
    }
    if (publishers != null) {
      publishers.positionPublisher.set(getPositionMeasure().in(ROTATION_UNIT));
      publishers.setpointPublisher.set(getSetpoint().in(ROTATION_UNIT));
      publishers.atPositionPublisher.set(atPosition());
      publishers.appliedCurrentPublisher.set(motor.getAppliedCurrent().in(Units.Amps));
    }
  }

  @Override
  public void close() {
    if (publishers != null) {
      publishers.close();
    }
  }

  private double getMeasurement() {
    return encoder.getPositionMeasure().in(ROTATION_UNIT);
  }

  private record Publishers(
      DoublePublisher positionPublisher,
      BooleanPublisher atPositionPublisher,
      DoublePublisher appliedCurrentPublisher,
      DoublePublisher setpointPublisher) {

    Publishers(NetworkTable networkTable) {
      this(
          networkTable.getDoubleTopic("position").publish(),
          networkTable.getBooleanTopic("at position").publish(),
          networkTable.getDoubleTopic("applied current").publish(),
          networkTable.getDoubleTopic("setpoint").publish());
    }

    void close() {
      positionPublisher.close();
      atPositionPublisher.close();
      appliedCurrentPublisher.close();
      setpointPublisher.close();
    }
  }
}
