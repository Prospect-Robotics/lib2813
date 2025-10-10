package com.team2813.lib2813.subsystems;

import com.team2813.lib2813.control.ControlMode;
import com.team2813.lib2813.control.Encoder;
import com.team2813.lib2813.control.Motor;
import com.team2813.lib2813.control.PIDMotor;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.units.AngleUnit;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Abstract subsystem that manages a motor with PID control based on encoder measurements.
 *
 * <p>This class allows:
 *
 * <ul>
 *   <li>Setting a desired position via a {@link Supplier<Angle>} setpoint.
 *   <li>Applying PID control to reach that setpoint using a {@link PIDController}.
 *   <li>Using different {@link ControlMode}s for manual or PID-driven motor control.
 *   <li>Flexible angle units via {@link AngleUnit} for calculations and measurements.
 * </ul>
 *
 * @param <T> the type of {@link Supplier<Angle>} used to provide dynamic setpoints.
 */
public abstract class MotorSubsystem<T extends Supplier<Angle>> extends SubsystemBase
    implements Motor, Encoder {

  /** The motor used by this subsystem */
  protected final Motor motor;

  /** The encoder providing feedback for PID calculations */
  protected final Encoder encoder;

  /** The control mode used when applying outputs to the motor */
  protected final ControlMode controlMode;

  /** The angle unit for measurements and setpoints */
  protected final AngleUnit rotationUnit;

  /** Maximum allowed position error for "at position" checks */
  protected final double acceptableError;

  /** The PID controller managing the motor to reach the setpoint */
  protected final PIDController controller;

  private double setpoint;
  private boolean isEnabled;

  /**
   * Constructs a MotorSubsystem using the provided configuration.
   *
   * @param builder the configuration for the motor subsystem
   */
  protected MotorSubsystem(MotorSubsystemConfiguration builder) {
    this.setpoint = builder.startingPosition;
    this.controller = builder.controller;
    this.controller.setTolerance(builder.acceptableError);
    this.acceptableError = builder.acceptableError;
    this.motor = builder.motor;
    this.encoder = builder.encoder;
    this.controlMode = builder.controlMode;
    this.rotationUnit = builder.rotationUnit;
  }

  /**
   * Sets the target position for this subsystem and enables PID control if not already enabled.
   *
   * @param setpoint the supplier of the target angle
   */
  public void setSetpoint(T setpoint) {
    if (!isEnabled()) {
      enable();
    }
    this.setpoint = setpoint.get().in(rotationUnit);
    controller.setSetpoint(this.setpoint);
  }

  /**
   * Returns the current PID setpoint.
   *
   * @return the target setpoint as an {@link Angle}
   */
  public Angle getSetpoint() {
    return rotationUnit.of(setpoint);
  }

  /**
   * Checks whether the subsystem is within the acceptable error of the setpoint.
   *
   * @return {@code true} if within acceptable error, {@code false} otherwise
   */
  public boolean atPosition() {
    return Math.abs(getMeasurement() - setpoint) <= acceptableError;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Also disables PID control if enabled.
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

  /** Enables PID control of the subsystem. */
  public void enable() {
    isEnabled = true;
  }

  /** Disables PID control and sets motor output to zero. */
  public void disable() {
    isEnabled = false;
    useOutput(0, 0);
  }

  /**
   * Returns whether PID control is currently enabled.
   *
   * @return {@code true} if enabled, {@code false} otherwise
   */
  public boolean isEnabled() {
    return isEnabled;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Also disables PID control if enabled.
   */
  @Override
  public void set(ControlMode mode, double demand) {
    if (isEnabled()) {
      disable();
    }
    motor.set(mode, demand);
  }

  /**
   * Applies the PID output to the motor. Can be overridden for advanced control.
   *
   * @param output the PID output value
   * @param setpoint the target setpoint (for reference)
   */
  protected void useOutput(double output, double setpoint) {
    motor.set(controlMode, output);
  }

  /** Returns the encoder measurement converted to the configured {@link AngleUnit}. */
  protected double getMeasurement() {
    return encoder.getPositionMeasure().in(rotationUnit);
  }

  @Override
  @Deprecated
  public double position() {
    return encoder.position();
  }

  public Angle getPositionMeasure() {
    return encoder.getPositionMeasure();
  }

  @Override
  @Deprecated
  public void setPosition(double position) {
    encoder.setPosition(position);
  }

  public void setPosition(Angle position) {
    encoder.setPosition(position);
  }

  @Override
  @Deprecated
  public double getVelocity() {
    return encoder.getVelocity();
  }

  public AngularVelocity getVelocityMeasure() {
    return encoder.getVelocityMeasure();
  }

  /** Periodic update that applies PID output if enabled. */
  @Override
  public void periodic() {
    if (isEnabled) {
      useOutput(controller.calculate(getMeasurement()), setpoint);
    }
  }

  /**
   * Builder-style configuration class for {@link MotorSubsystem}.
   *
   * <p>Allows setting PID constants, control mode, starting position, and other configuration
   * parameters.
   */
  public static class MotorSubsystemConfiguration {
    /** Default allowed error for position checks */
    public static final double DEFAULT_ERROR = 5.0;

    /** Default starting position */
    public static final double DEFAULT_STARTING_POSITION = 0.0;

    private ControlMode controlMode;
    private AngleUnit rotationUnit;
    private final Motor motor;
    private final Encoder encoder;
    private PIDController controller;
    private double acceptableError;
    private double startingPosition;

    /**
     * Creates a new configuration for a motor subsystem.
     *
     * <p>Defaults: PID(0,0,0), acceptable error = {@value #DEFAULT_ERROR}, starting position =
     * {@value #DEFAULT_STARTING_POSITION}, control mode = {@link ControlMode#DUTY_CYCLE}, angle
     * unit = {@link Units#Rotations}.
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
     * Creates a new configuration using a PIDMotor with a built-in encoder.
     *
     * @param motor the motor that implements {@link PIDMotor}
     */
    public MotorSubsystemConfiguration(PIDMotor motor) {
      this(motor, motor);
    }

    /** Sets the PID controller to use. */
    public MotorSubsystemConfiguration controller(PIDController controller) {
      this.controller = controller;
      return this;
    }

    /** Sets the control mode for motor output. */
    public MotorSubsystemConfiguration controlMode(ControlMode controlMode) {
      this.controlMode = controlMode;
      return this;
    }

    /** Sets the PID constants. */
    public MotorSubsystemConfiguration PID(double p, double i, double d) {
      controller.setPID(p, i, d);
      return this;
    }

    /** Sets the starting position in the configured angle unit. */
    public MotorSubsystemConfiguration startingPosition(Angle startingPosition) {
      this.startingPosition = startingPosition.in(this.rotationUnit);
      return this;
    }

    /** Sets the starting position via a supplier of an angle. */
    public MotorSubsystemConfiguration startingPosition(Supplier<Angle> startingPosition) {
      this.startingPosition = startingPosition.get().in(this.rotationUnit);
      return this;
    }

    /** Sets the acceptable position error. */
    public MotorSubsystemConfiguration acceptableError(double error) {
      this.acceptableError = error;
      return this;
    }

    /** Sets the angle unit for PID calculations. */
    public MotorSubsystemConfiguration rotationUnit(AngleUnit rotationUnit) {
      startingPosition = rotationUnit.convertFrom(startingPosition, this.rotationUnit);
      this.rotationUnit = rotationUnit;
      return this;
    }
  }
}
