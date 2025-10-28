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
 * Defines PID control over a motor, with values specified by an encoder
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
    this.controller = builder.controller;
    this.controller.setTolerance(builder.acceptableError);
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

    this.controller.setSetpoint(builder.startingPosition);
  }

  /**
   * Sets the desired setpoint to the provided value, and enables the PID control.
   *
   * @param position the position to go to.
   */
  public void setSetpoint(T position) {
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
   */
  public Command setSetpointCommand(T setpoint) {
    return new InstantCommand(() -> this.setSetpoint(setpoint), this);
  }

  /** Returns the current setpoint as an angle. */
  public Angle getSetpoint() {
    return rotationUnit.of(controller.getSetpoint());
  }

  /** Determines if the motor is at the current setpoint, within the acceptable error. */
  public boolean atPosition() {
    return Math.abs(getMeasurement() - controller.getSetpoint()) <= acceptableError;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Additionally, this method disables PID control of the subsystem
   */
  @Override
  public void set(ControlMode mode, double demand, double feedForward) {
    if (isEnabled()) {
      disable();
    }
    motor.set(mode, demand, feedForward);
  }

  @Override
  public Current getAppliedCurrent() {
    return motor.getAppliedCurrent();
  }

  /**
   * Enables the motor
   *
   * <p>The motor voltage will be periodically updated to move the motor towards the current
   * setupoint.
   */
  public void enable() {
    isEnabled = true;
  }

  /**
   * Stops the motor.
   *
   * <p>The motor voltage will be set to zero, and the motor will not adjust to move towards the
   * current setpoint.
   */
  public void disable() {
    isEnabled = false;
    motor.disable();
  }

  /**
   * Returns whether the controller is enabled. If this is enabled, then PID control will be used.
   *
   * @return Whether the controller is enabled.
   */
  public boolean isEnabled() {
    return isEnabled;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Additionally, this method disables PID control of the subsystem. It <em>does not</em> clamp
   * the provided value.
   */
  @Override
  public void set(ControlMode mode, double demand) {
    isEnabled = false;
    motor.set(mode, demand);
  }

  /**
   * Clamps the given output value and provides it to the motor.
   *
   * <p>This was protected and non-final to allow subclasses to clamp the output. Subclasses should
   * override {@link #clampOutput(double)}.
   *
   * @param output The output calculated by the PID algorithm.
   * @param setpoint Ignored.
   * @deprecated Subclasses should override {@link #clampOutput(double)}.
   */
  @Deprecated
  protected void useOutput(double output, double setpoint) {
    motor.set(controlMode, clampOutput(output));
  }

  /**
   * Clamps the given output value and provides it to the motor.
   *
   * <p>This is called by {@link #periodic()} if this subsystem is enabled.
   */
  private void useOutput(double output) {
    useOutput(output, controller.getSetpoint());
  }

  /**
   * Extension point that allows subclasses to clamp the output.
   *
   * <p>The default implementation returns the provided value.
   *
   * @param output Output provided by the PID controller.
   * @return Output to provide to the motor.
   * @see edu.wpi.first.math.MathUtil#clamp(double, double, double)
   */
  protected double clampOutput(double output) {
    return output;
  }

  protected double getMeasurement() {
    return encoder.getPositionMeasure().in(rotationUnit);
  }

  @Override
  @Deprecated(forRemoval = true)
  public double position() {
    return encoder.position();
  }

  @Override
  public Angle getPositionMeasure() {
    return encoder.getPositionMeasure();
  }

  @Override
  @Deprecated(forRemoval = true)
  public void setPosition(double position) {
    encoder.setPosition(position);
  }

  @Override
  public void setPosition(Angle position) {
    encoder.setPosition(position);
  }

  @Override
  @Deprecated(forRemoval = true)
  public double getVelocity() {
    return encoder.getVelocity();
  }

  @Override
  public AngularVelocity getVelocityMeasure() {
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

    /** Sets the acceptable position error. */
    public MotorSubsystemConfiguration acceptableError(double error) {
      this.acceptableError = error;
      return this;
    }

    /**
     * Sets the unit to use for PID calculations
     *
     * @param rotationUnit The angle unit to use for calculations
     */
    public MotorSubsystemConfiguration rotationUnit(AngleUnit rotationUnit) {
      startingPosition = rotationUnit.convertFrom(startingPosition, this.rotationUnit);
      this.rotationUnit = rotationUnit;
      return this;
    }

    public MotorSubsystemConfiguration publishTo(NetworkTableInstance ntInstance) {
      this.ntInstance = ntInstance;
      return this;
    }
  }
}
