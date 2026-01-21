/*
Copyright 2023-2026 Prospect Robotics SWENext Club

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
package com.team2813.lib2813.subsystems;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Rotations;

import com.team2813.lib2813.control.ControlMode;
import com.team2813.lib2813.control.Encoder;
import com.team2813.lib2813.control.Motor;
import com.team2813.lib2813.control.PIDMotor;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.networktables.BooleanPublisher;
import edu.wpi.first.networktables.DoublePublisher;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.units.AngleUnit;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Defines a generic subsystem comprising a motor and encoder.
 *
 * <p>The MotorSybsystem supports a dual operation mode:
 *
 * <ul>
 *   <li><b>PID Mode</b> - the user set a destination position (aka "setpoint") and the motor
 *       subsystem engages a PID Controller to drive the motor to that setpoint.
 *   <li><b>Direct User Input Mode</b> - the subsystem responds to direct input from the user (i.e.,
 *       voltage or duty cycle).
 * </ul>
 *
 * <p>The <b>PID Mode</b> is enabled by calling {@link #setSetpoint(T)}. The subsystem starts moving
 * toward the setpoint and maintains position at the setpoint under the control of the PID
 * controller. The motor system's {@link #isEnabled()} returns {@code true}.
 *
 * <p>The <b>Direct User Input Mode</b> is activated when the user calls the {@link
 * #set(ControlMode,double,double)} or {@link #set(ControlMode,double)} method, where the user
 * provides direct input of type ControlType (specified via {@link
 * MotorSubsystemConfiguration#controlMode(ControlMode)}). The PID Mode is interrupted and
 * disengaged, and {@link #isEnabled()} returns {@code false}. It can be re-engaged with the {@link
 * #enable()} method and will resume movement toward setpoint.
 *
 * @param <T> the type of the {@link Supplier<Angle>} used to specify setpoints.
 */
public abstract class MotorSubsystem<T extends Supplier<Angle>> extends SubsystemBase
    implements Motor, Encoder {

  protected final Motor motor;
  protected final Encoder encoder;
  protected final ControlMode controlMode;
  protected final AngleUnit rotationUnit;
  protected final double acceptableError;
  protected final PIDController controller;
  private final DoublePublisher positionPublisher;
  private final BooleanPublisher atPositionPublisher;
  private final DoublePublisher appliedCurrentPublisher;
  private final DoublePublisher setpointPublisher;

  private boolean isEnabled;

  protected MotorSubsystem(MotorSubsystemConfiguration builder) {
    controller = builder.controller;
    controller.setTolerance(builder.acceptableError);
    controller.setSetpoint(builder.startingPosition);
    acceptableError = builder.acceptableError;
    motor = builder.motor;
    encoder = builder.encoder;
    controlMode = builder.controlMode;
    rotationUnit = builder.rotationUnit;
    if (builder.ntInstance != null) {
      NetworkTable networkTable = builder.ntInstance.getTable(getName());
      positionPublisher = networkTable.getDoubleTopic("position").publish();
      atPositionPublisher = networkTable.getBooleanTopic("at position").publish();
      appliedCurrentPublisher = networkTable.getDoubleTopic("applied current").publish();
      setpointPublisher = networkTable.getDoubleTopic("setpoint").publish();
    } else {
      positionPublisher = null;
      atPositionPublisher = null;
      appliedCurrentPublisher = null;
      setpointPublisher = null;
    }
  }

  /**
   * Sets the desired setpoint to the provided value, and enables the PID control.
   *
   * @param position the position to go to.
   */
  public final void setSetpoint(T position) {
    if (!isEnabled()) {
      enable();
    }
    double setpoint = position.get().in(rotationUnit);
    controller.setSetpoint(setpoint);
  }

  /**
   * Returns a command that sets the desired setpoint to the provided value.
   *
   * @param setpoint the position to go to.
   * @since 2.0.0
   */
  public final Command setSetpointCommand(T setpoint) {
    return new InstantCommand(() -> this.setSetpoint(setpoint), this);
  }

  /** Returns the current setpoint as an angle. */
  public final Angle getSetpoint() {
    return rotationUnit.of(controller.getSetpoint());
  }

  /** Determines if the motor is at the current setpoint, within the acceptable error. */
  public final boolean atPosition() {
    return Math.abs(getMeasurement() - controller.getSetpoint()) <= acceptableError;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Additionally, this method disables PID control of the subsystem
   */
  @Override
  public final void set(ControlMode mode, double demand, double feedForward) {
    isEnabled = false;
    motor.set(mode, demand, feedForward);
  }

  @Override
  public final Current getAppliedCurrent() {
    return motor.getAppliedCurrent();
  }

  /**
   * Engages the PID controller.
   *
   * <p>The motor voltage will be periodically updated to move the motor towards the current
   * setpoint.
   *
   * <p>If this method is called after the motor drive toward setpoint was interrupted by user
   * interruption, the motor will resume its movement towards the last set setpoint.
   */
  public final void enable() {
    isEnabled = true;
  }

  /**
   * Stops the motor.
   *
   * <p>The motor voltage will be set to zero, and the motor will not adjust to move towards the
   * current setpoint.
   *
   * @since 2.0.0
   */
  @Override
  public final void stopMotor() {
    isEnabled = false;
    motor.stopMotor();
  }

  /**
   * Returns whether the PID controller is engaged.
   *
   * <p>When the PID controller is engaged, the motor system is moving toward the user-specified
   * setpoint position (set via {@link #setSetpoint(T)}), or, if it has already reached the
   * setpoint, is maintaining that position.
   *
   * @return Whether the PID controller is engaged.
   */
  public final boolean isEnabled() {
    return isEnabled;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Additionally, this method disables PID control of the subsystem. It <em>does not</em> clamp
   * the provided value.
   */
  @Override
  public final void set(ControlMode mode, double demand) {
    isEnabled = false;
    motor.set(mode, demand);
  }

  /**
   * Clamps the given output value and provides it to the motor.
   *
   * <p>This is called by {@link #periodic()} if this subsystem is enabled.
   */
  private void useOutput(double output) {
    motor.set(controlMode, clampOutput(output));
  }

  /**
   * Extension point that allows subclasses to clamp the output.
   *
   * <p>The default implementation returns the provided value.
   *
   * @param output Output provided by the PID controller.
   * @return Output to provide to the motor.
   * @see edu.wpi.first.math.MathUtil#clamp(double, double, double)
   * @since 2.0.0
   */
  protected double clampOutput(double output) {
    return output;
  }

  /**
   * Returns the current position of the motor subsystem in the units specified by rotationUnit.
   * This position is used by the PID controller (when it is enabled) to determine if it is already
   * at position or needs to adjust the subsystem position to reach a user-specified setpoint.
   */
  protected final double getMeasurement() {
    return encoder.getPositionMeasure().in(rotationUnit);
  }

  @Override
  public final Angle getPositionMeasure() {
    return encoder.getPositionMeasure();
  }

  @Override
  public final void setPosition(Angle position) {
    encoder.setPosition(position);
  }

  @Override
  public final AngularVelocity getVelocityMeasure() {
    return encoder.getVelocityMeasure();
  }

  /** Applies the PID output to the motor if this subsystem is enabled. */
  @Override
  public void periodic() {
    if (isEnabled) {
      useOutput(controller.calculate(getMeasurement()));
    }
    if (positionPublisher != null) {
      positionPublisher.set(getPositionMeasure().in(Rotations));
      setpointPublisher.set(getSetpoint().in(Rotations));
      atPositionPublisher.set(atPosition());
      appliedCurrentPublisher.set(getAppliedCurrent().in(Amps));
    }
  }

  /** A configuration for a MotorSubsystem */
  public static class MotorSubsystemConfiguration {
    /** The default acceptable position error. */
    public static final double DEFAULT_ERROR = 5.0;

    /** The default starting position if one is not provided. */
    public static final double DEFAULT_STARTING_POSITION = 0.0;

    private ControlMode controlMode;
    private AngleUnit rotationUnit;
    private final Motor motor;
    private final Encoder encoder;
    private PIDController controller;
    private double acceptableError;
    private double startingPosition;
    private NetworkTableInstance ntInstance;

    /**
     * Creates a new configuration for a MotorSubsystems. The default acceptable error is {@value
     * #DEFAULT_ERROR}, the PID constants are set to 0, and the starting position is {@value
     * #DEFAULT_STARTING_POSITION}
     *
     * @param motor the motor to control
     * @param encoder the encoder providing feedback
     */
    public MotorSubsystemConfiguration(Motor motor, Encoder encoder) {
      this.motor = Objects.requireNonNull(motor, "motor should not be null");
      this.encoder = Objects.requireNonNull(encoder, "encoder should not be null");
      controller = new PIDController(0, 0, 0);
      acceptableError = DEFAULT_ERROR;
      startingPosition = DEFAULT_STARTING_POSITION;
      controlMode = ControlMode.DUTY_CYCLE;
      rotationUnit = Units.Rotations;
    }

    /**
     * Creates a new config for MotorSubsystems using a motor that has a built-in encoder. The
     * default acceptable error is {@value #DEFAULT_ERROR}, the PID constants are set to 0, and the
     * starting position is {@value #DEFAULT_STARTING_POSITION}
     *
     * @param motor the integrated motor controller
     */
    public MotorSubsystemConfiguration(PIDMotor motor) {
      this(motor, motor);
    }

    /**
     * Sets the controller used to calculate the next value
     *
     * @param controller The PID controller
     * @return {@code this} for chaining
     */
    public MotorSubsystemConfiguration controller(PIDController controller) {
      this.controller = controller;
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
    public MotorSubsystemConfiguration controlMode(ControlMode controlMode) {
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
    public MotorSubsystemConfiguration PID(double p, double i, double d) {
      controller.setPID(p, i, d);
      return this;
    }

    /**
     * Sets the initial setpoint of the controller
     *
     * @param startingPosition the initial setpoint
     * @return {@code this} for chaining
     */
    public MotorSubsystemConfiguration startingPosition(Angle startingPosition) {
      this.startingPosition = startingPosition.in(this.rotationUnit);
      return this;
    }

    /**
     * Sets the initial setpoint of the controller from the current value of a supplier.
     *
     * <p>This is provided to allow the subclass to define an {@code Enum} (that implements {@code
     * Supplier<Angle>}) which defines the supported positions of this subsystem.
     *
     * @param startingPositionSupplier supplier to use to get the initial setpoint
     * @return {@code this} for chaining
     */
    public MotorSubsystemConfiguration startingPosition(Supplier<Angle> startingPositionSupplier) {
      return startingPosition(startingPositionSupplier.get());
    }

    /**
     * Sets the acceptable position error.
     *
     * @param error the error which is considered tolerable for use with {@code }atPosition()}
     * @return {@code this} for chaining
     */
    public MotorSubsystemConfiguration acceptableError(double error) {
      this.acceptableError = error;
      return this;
    }

    /**
     * Sets the unit to use for PID calculations
     *
     * @param rotationUnit the angle unit to use for calculations
     * @return {@code this} for chaining
     */
    public MotorSubsystemConfiguration rotationUnit(AngleUnit rotationUnit) {
      startingPosition = rotationUnit.convertFrom(startingPosition, this.rotationUnit);
      this.rotationUnit = rotationUnit;
      return this;
    }

    /**
     * Enables publishing of data to the provided network table instance.
     *
     * @param ntInstance the network table instance to publish to
     * @return {@code this} for chaining
     * @since 2.0.0
     */
    public MotorSubsystemConfiguration publishTo(NetworkTableInstance ntInstance) {
      this.ntInstance = ntInstance;
      return this;
    }
  }
}
