package com.team2813.lib2813.subsystems;

import com.team2813.lib2813.control.ControlMode;
import com.team2813.lib2813.control.Encoder;
import com.team2813.lib2813.control.Motor;
import com.team2813.lib2813.control.PIDMotor;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.units.AngleUnit;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.Measure;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Velocity;
import edu.wpi.first.wpilibj2.command.PIDSubsystem;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Defines PID controll over a motor, with values specified by an encoder
 *
 * @param <T> the {@link MotorSubsystem.Position} type to use positions from.
 */
public abstract class MotorSubsystem<T extends Supplier<Angle>> extends PIDSubsystem
    implements Motor, Encoder {

  protected final Motor motor;
  protected final Encoder encoder;
  protected final ControlMode controlMode;
  protected final AngleUnit rotationUnit;
  protected final double acceptableError;

  protected MotorSubsystem(MotorSubsystemConfiguration builder) {
    super(builder.controller, builder.startingPosition);
    getController().setTolerance(builder.acceptableError);
    acceptableError = builder.acceptableError;
    motor = builder.motor;
    encoder = builder.encoder;
    controlMode = builder.controlMode;
    rotationUnit = builder.rotationUnit;
  }

  /**
   * Sets the desired setpoint to the current setpoint, and enables the PID. control.
   *
   * <p>Prefer calling this over calling {@link #setSetpoint(double)}.
   *
   * @param setpoint the position to go to
   */
  public void setSetpoint(T setpoint) {
    if (!isEnabled()) {
      enable();
    }
    setSetpoint(setpoint.get().in(rotationUnit));
  }

  public boolean atPosition() {
    return Math.abs(getMeasurement() - getSetpoint()) <= acceptableError;
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

  /**
   * {@inheritDoc} If this is enabled, then PID control will be used.
   *
   * @return
   */
  @Override
  public boolean isEnabled() {
    return super.isEnabled();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Additionally, this method disables PID control of the subsystem
   */
  @Override
  public void set(ControlMode mode, double demand) {
    if (isEnabled()) {
      disable();
    }
    motor.set(mode, demand);
  }

  @Override
  protected void useOutput(double output, double setpoint) {
    motor.set(controlMode, output);
  }

  @Override
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

  /** A configuration for a MotorSubsystem */
  public static class MotorSubsystemConfiguration {
    /** The default error of a motor subsystems */
    public static final double DEFAULT_ERROR = 5.0;

    /** The default starting position if one is not defined */
    public static final double DEFAULT_STARTING_POSITION = 0.0;

    private ControlMode controlMode;
    private AngleUnit rotationUnit;
    private Motor motor;
    private Encoder encoder;
    private PIDController controller;
    private double acceptableError;
    private double startingPosition;

    /**
     * Creates a new config for MotorSubsystems. The default acceptable error is {@value
     * #DEFAULT_ERROR}, the PID constants are set to 0, and the starting position is {@value
     * #DEFAULT_STARTING_POSITION}
     *
     * @param motor the motor to use
     * @param encoder the encoder to use
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
     * @param motor the motor to use that also supports an encoder
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
     */
    public MotorSubsystemConfiguration controlMode(ControlMode controlMode) {
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
     * sets the starting position.
     *
     * @param startingPosition the position to start at
     * @return {@code this} for chaining
     */
    public MotorSubsystemConfiguration startingPosition(Angle startingPosition) {
      this.startingPosition = startingPosition.in(this.rotationUnit);
      return this;
    }

    public MotorSubsystemConfiguration startingPosition(Supplier<Angle> startingPosition) {
      this.startingPosition = startingPosition.get().in(this.rotationUnit);
      return this;
    }

    public MotorSubsystemConfiguration acceptableError(double error) {
      this.acceptableError = error;
      return this;
    }

    /**
     * Sets the unit to use for PID calculations
     *
     * @param rotationUnit
     */
    public MotorSubsystemConfiguration rotationUnit(AngleUnit rotationUnit) {
      startingPosition = rotationUnit.convertFrom(startingPosition, this.rotationUnit);
      this.rotationUnit = rotationUnit;
      return this;
    }
  }
}
