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

  protected final Motor m_motor;
  protected final Encoder m_encoder;
  protected final ControlMode m_controlMode;
  protected final AngleUnit m_rotationUnit;
  protected final double m_acceptableError;
  protected final PIDController m_pidController;
  private final DoublePublisher m_positionPublisher;
  private final BooleanPublisher m_atPositionPublisher;
  private final DoublePublisher m_appliedCurrentPublisher;
  private final DoublePublisher m_setpointPublisher;

  private boolean m_isEnabled;

  /** A configuration for a MotorSubsystem */
  public static class MotorSubsystemConfiguration {
    /** The default acceptable position error. */
    public static final double DEFAULT_ERROR = 5.0;

    /** The default starting position if one is not provided. */
    public static final double DEFAULT_STARTING_POSITION = 0.0;

    /** Control mode for the MotorSubsystem motor. */
    private ControlMode controlMode;

    /** */
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

  protected MotorSubsystem(MotorSubsystemConfiguration builder) {
    m_pidController = builder.controller;
    m_pidController.setTolerance(builder.acceptableError);
    m_acceptableError = builder.acceptableError;
    m_motor = builder.motor;
    m_encoder = builder.encoder;
    m_controlMode = builder.controlMode;
    m_rotationUnit = builder.rotationUnit;
    if (builder.ntInstance != null) {
      NetworkTable networkTable = builder.ntInstance.getTable(getName());
      m_positionPublisher = networkTable.getDoubleTopic("position").publish();
      m_atPositionPublisher = networkTable.getBooleanTopic("at position").publish();
      m_appliedCurrentPublisher = networkTable.getDoubleTopic("applied current").publish();
      m_setpointPublisher = networkTable.getDoubleTopic("setpoint").publish();
    } else {
      m_positionPublisher = null;
      m_atPositionPublisher = null;
      m_appliedCurrentPublisher = null;
      m_setpointPublisher = null;
    }

    m_pidController.setSetpoint(builder.startingPosition);
  }

  /**
   * Sets the desired setpoint to the provided value, and enables the PID control.
   *
   * @param position the position to go to.
   */
  public final void setSetpoint(T position) {
    if (!m_isEnabled) {
      enable();
    }
    double setpoint = position.get().in(m_rotationUnit);
    m_pidController.setSetpoint(setpoint);
  }

  /**
   * Returns a command that sets the desired setpoint to the provided value.
   *
   * @param setpoint the position to go to.
   */
  public final Command setSetpointCommand(T setpoint) {
    return new InstantCommand(() -> this.setSetpoint(setpoint), this);
  }

  /** Returns the current setpoint as an angle. */
  public final Angle getSetpoint() {
    return m_rotationUnit.of(m_pidController.getSetpoint());
  }

  /** Determines if the motor is at the current setpoint, within the acceptable error. */
  public final boolean atPosition() {
    return Math.abs(getMeasurement() - m_pidController.getSetpoint()) <= m_acceptableError;
  }

  /**
   * Enables the motor
   *
   * <p>The motor voltage will be periodically updated to move the motor towards the current
   * setupoint.
   */
  public final void enable() {
    m_isEnabled = true;
  }

  /**
   * Stops the motor.
   *
   * <p>The motor voltage will be set to zero, and the motor will not adjust to move towards the
   * current setpoint.
   */
  public final void disable() {
    m_isEnabled = false;
    m_motor.set(m_controlMode, 0);
  }

  /**
   * Returns whether the controller is enabled. If this is enabled, then PID control will be used.
   *
   * @return Whether the controller is enabled.
   */
  public final boolean isEnabled() {
    return m_isEnabled;
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
    m_motor.set(m_controlMode, clampOutput(output));
  }

  /**
   * Clamps the given output value and provides it to the motor.
   *
   * <p>This is called by {@link #periodic()} if this subsystem is enabled.
   */
  private void useOutput(double output) {
    useOutput(output, m_pidController.getSetpoint());
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

  protected final double getMeasurement() {
    return m_encoder.getPositionMeasure().in(m_rotationUnit);
  }

  // `Motor` method overrides.
  // -------------------------

  /**
   * {@inheritDoc}
   *
   * <p>Additionally, this method disables PID control of the subsystem
   */
  @Override
  public final void set(ControlMode mode, double demand, double feedForward) {
    if (m_isEnabled) {
      disable();
    }
    m_motor.set(mode, demand, feedForward);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Additionally, this method disables PID control of the subsystem. It <em>does not</em> clamp
   * the provided value.
   */
  @Override
  public final void set(ControlMode mode, double demand) {
    m_isEnabled = false;
    m_motor.set(mode, demand);
  }

  /** {@inheritDoc} */
  @Override
  public final Current getAppliedCurrent() {
    return m_motor.getAppliedCurrent();
  }

  // `Encoder` method overrides.
  // ---------------------------

  @Override
  @Deprecated(forRemoval = true)
  public double position() {
    return m_encoder.position();
  }

  @Override
  public final Angle getPositionMeasure() {
    return m_encoder.getPositionMeasure();
  }

  @Override
  @Deprecated(forRemoval = true)
  public void setPosition(double position) {
    m_encoder.setPosition(position);
  }

  @Override
  public final void setPosition(Angle position) {
    m_encoder.setPosition(position);
  }

  @Override
  @Deprecated(forRemoval = true)
  public double getVelocity() {
    return m_encoder.getVelocity();
  }

  @Override
  public final AngularVelocity getVelocityMeasure() {
    return m_encoder.getVelocityMeasure();
  }

  // `Subsystem` method overrides.
  // -----------------------------

  /** Applies the PID output to the motor if this subsystem is enabled. */
  @Override
  public void periodic() {
    if (m_isEnabled) {
      useOutput(m_pidController.calculate(getMeasurement()));
    }
    if (m_positionPublisher != null) {
      m_positionPublisher.set(getPositionMeasure().in(Rotations));
      m_setpointPublisher.set(getSetpoint().in(Rotations));
      m_atPositionPublisher.set(atPosition());
      m_appliedCurrentPublisher.set(getAppliedCurrent().in(Amps));
    }
  }
}
