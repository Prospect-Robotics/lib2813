package com.team2813.lib2813.control.motors;

import static com.team2813.lib2813.util.InputValidation.checkCanId;

import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.SlotConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.configs.TalonFXConfigurator;
import com.ctre.phoenix6.controls.*;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.team2813.lib2813.control.ControlMode;
import com.team2813.lib2813.control.DeviceInformation;
import com.team2813.lib2813.control.InvertType;
import com.team2813.lib2813.control.PIDMotor;
import com.team2813.lib2813.util.InvalidCanIdException;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Wrapper class for CTRE Phoenix 6 TalonFX motor controller (Falcon 500 motor).
 * 
 * <p>This class provides a standardized interface for controlling TalonFX motor controllers
 * while maintaining compatibility with the team's control system architecture. It implements
 * the {@link PIDMotor} interface and supports advanced control modes including velocity
 * control, motion magic, and position control with the integrated encoder.
 * 
 * <p>Key features include:
 * <ul>
 * <li>Type-safe unit support using WPILib's units system</li>
 * <li>Advanced control modes: duty cycle, voltage, velocity, and motion magic</li>
 * <li>PID control configuration with multiple slot support</li>
 * <li>Follower motor management with strict and regular following modes</li>
 * <li>Built-in current limiting for motor protection (40A stator limit)</li>
 * <li>Input validation for CAN IDs and invert types</li>
 * </ul>
 * 
 * <p>The wrapper automatically configures current limits (40A stator current limit with
 * supply current limiting enabled) and validates all input parameters to prevent
 * configuration errors.
 * 
 * @author Team 2813
 * @since 1.0
 */
public class TalonFXWrapper implements PIDMotor {
  
  /** A list of follower TalonFX controllers to prevent garbage collection */
  private final List<TalonFX> followers = new ArrayList<>();

  /** The primary TalonFX motor controller */
  private final TalonFX motor;

  /** Device information containing CAN ID and bus details */
  private final DeviceInformation information;

  /**
   * Creates a TalonFXWrapper on the specified CAN bus.
   *
   * <p>The constructor automatically configures:
   * <ul>
   * <li>Motor output inversion based on the specified {@code invertType}</li>
   * <li>Current limits: 40A stator current limit with supply current limiting enabled</li>
   * <li>Input validation for CAN ID and invert type</li>
   * </ul>
   *
   * @param canID the CAN ID of the motor controller, must be in range [0, 62]
   * @param canbus the CAN bus that the motor is connected to (e.g., "rio", "canivore")
   * @param invertType the inversion type for motor output, must be a rotation value
   * @throws NullPointerException if either {@code invertType} or {@code canbus} are null
   * @throws IllegalArgumentException if {@code invertType} is not in {@link InvertType#rotationValues}.
   *         This exception is thrown when passed an {@link InvertType} that is for following motors
   * @throws InvalidCanIdException if the CAN ID is invalid (outside range [0, 62])
   */
  public TalonFXWrapper(int canID, String canbus, InvertType invertType) {
    Objects.requireNonNull(invertType, "invertType should not be null");
    Objects.requireNonNull(canbus, "canbus should not be null");
    if (!InvertType.rotationValues.contains(invertType)) {
      throw new IllegalArgumentException("invertType invalid");
    }
    motor = new TalonFX(checkCanId(canID), canbus);

    TalonFXConfiguration config = new TalonFXConfiguration();
    // should never throw anything, as the tests guarantee that everything in
    // rotationValues
    // returns a non-empty value with phoenixInvert
    config.MotorOutput.Inverted = invertType.phoenixInvert().orElseThrow(AssertionError::new);
    config.CurrentLimits =
        new CurrentLimitsConfigs().withStatorCurrentLimit(40).withSupplyCurrentLimitEnable(true);
    TalonFXConfigurator configurator = motor.getConfigurator();
    configurator.apply(config);

    information = new DeviceInformation(canID, canbus);
  }

  /**
   * Creates a TalonFXWrapper on the RoboRIO's default CAN bus.
   *
   * <p>This is a convenience constructor that creates a TalonFX on the default "rio" CAN bus.
   * The constructor automatically configures the same settings as the primary constructor.
   *
   * @param canID the CAN ID of the motor controller, must be in range [0, 62]
   * @param invertType the inversion type for motor output, must be a rotation value
   * @throws NullPointerException if {@code invertType} is null
   * @throws IllegalArgumentException if {@code invertType} is not in {@link InvertType#rotationValues}.
   *         This exception is thrown when passed an {@link InvertType} that is for following motors
   * @throws InvalidCanIdException if the CAN ID is invalid (outside range [0, 62])
   */
  public TalonFXWrapper(int canID, InvertType invertType) {
    Objects.requireNonNull(invertType, "invertType should not be null");
    motor = new TalonFX(checkCanId(canID));
    if (!InvertType.rotationValues.contains(invertType)) {
      throw new IllegalArgumentException("invertType invalid");
    }

    TalonFXConfiguration config = new TalonFXConfiguration();
    // should never throw anything, as the tests guarantee that everything in
    // rotationValues
    // returns a non-empty value with phoenixInvert
    config.MotorOutput.Inverted = invertType.phoenixInvert().orElseThrow(AssertionError::new);
    config.CurrentLimits =
        new CurrentLimitsConfigs().withStatorCurrentLimit(40).withSupplyCurrentLimitEnable(true);
    TalonFXConfigurator configurator = motor.getConfigurator();
    configurator.apply(config);

    information = new DeviceInformation(canID);
  }

  /**
   * Sets the motor output using the specified control mode and demand value.
   * 
   * <p>This is a convenience method that calls {@link #set(ControlMode, double, double)}
   * with a feedforward value of 0.
   * 
   * @param controlMode the control mode to use
   * @param demand the demand value (interpretation depends on control mode)
   */
  public void set(ControlMode controlMode, double demand) {
    set(controlMode, demand, 0);
  }

  /**
   * Sets the motor output using the specified control mode, demand value, and feedforward.
   * 
   * <p>Supported control modes:
   * <ul>
   * <li>{@code VELOCITY}: Velocity control in rotations per second with optional feedforward</li>
   * <li>{@code MOTION_MAGIC}: Motion magic position control with trapezoidal motion profiling</li>
   * <li>{@code VOLTAGE}: Direct voltage control (demand in volts)</li>
   * <li>{@code DUTY_CYCLE} (default): Duty cycle control (demand as fraction from -1.0 to 1.0)</li>
   * </ul>
   * 
   * <p>Feedforward is supported for velocity and motion magic control modes to improve
   * tracking performance and reduce steady-state error.
   * 
   * @param controlMode the control mode to use
   * @param demand the demand value (interpretation depends on control mode)
   * @param feedForward the feedforward value (used for velocity and motion magic modes)
   */
  @Override
  public void set(ControlMode controlMode, double demand, double feedForward) {
    switch (controlMode) {
      case VELOCITY:
        VelocityDutyCycle v = new VelocityDutyCycle(demand);
        v.FeedForward = feedForward;
        motor.setControl(v);
        break;
      case MOTION_MAGIC:
        MotionMagicDutyCycle mm = new MotionMagicDutyCycle(demand);
        mm.FeedForward = feedForward;
        motor.setControl(mm);
        break;
      case VOLTAGE:
        VoltageOut vo = new VoltageOut(demand);
        motor.setControl(vo);
        break;
      default:
        DutyCycleOut dc = new DutyCycleOut(demand);
        motor.setControl(dc);
        break;
    }
  }

  /**
   * Gets the current position of the integrated encoder as a raw double value.
   * 
   * @deprecated Use {@link #getPositionMeasure()} instead for type safety with units
   * @return the current position in rotations as a double
   */
  @Override
  public double position() {
    return getPositionMeasure().in(Units.Rotations);
  }

  /**
   * Gets the current position of the integrated encoder using type-safe units.
   * 
   * <p>This method returns the position as an {@link Angle} measurement in rotations,
   * which can be converted to other angular units as needed. The TalonFX uses its
   * integrated encoder for high-resolution position feedback.
   * 
   * @return the current position as an {@link Angle} measurement
   */
  @Override
  public Angle getPositionMeasure() {
    return Units.Rotations.of(motor.getPosition().getValueAsDouble());
  }

  /**
   * Gets the current stator current of the motor controller.
   * 
   * <p>Stator current represents the current flowing through the motor windings and
   * is useful for monitoring motor load, detecting mechanical binding, and ensuring
   * the motor stays within safe operating limits.
   * 
   * @return the current stator current as a {@link Current} measurement
   */
  @Override
  public Current getAppliedCurrent() {
    return motor.getStatorCurrent().getValue();
  }

  /**
   * Sets the encoder position to the specified raw double value.
   * 
   * @deprecated Use {@link #setPosition(Angle)} instead for type safety with units
   * @param position the new position in rotations
   */
  @Override
  public void setPosition(double position) {
    motor.setPosition(position);
  }

  /**
   * Sets the encoder position using type-safe units.
   * 
   * <p>This method accepts any {@link Angle} measurement and converts it to rotations
   * internally for the TalonFX's integrated encoder.
   * 
   * @param position the new position as an {@link Angle} measurement
   */
  @Override
  public void setPosition(Angle position) {
    motor.setPosition(position.in(Units.Rotations));
  }

  /**
   * Gets the current velocity of the integrated encoder as a raw double value.
   * 
   * @deprecated Use {@link #getVelocityMeasure()} instead for type safety with units
   * @return the current velocity in rotations per second as a double
   */
  @Override
  public double getVelocity() {
    return motor.getVelocity().getValueAsDouble();
  }

  /**
   * Gets the current velocity of the integrated encoder using type-safe units.
   * 
   * <p>This method returns the velocity as an {@link AngularVelocity} measurement
   * in rotations per second, which can be converted to other angular velocity
   * units as needed.
   * 
   * @return the current velocity as an {@link AngularVelocity} measurement
   */
  @Override
  public AngularVelocity getVelocityMeasure() {
    return Units.RotationsPerSecond.of(motor.getVelocity().getValueAsDouble());
  }

  /**
   * Provides direct access to the underlying TalonFX hardware object.
   * 
   * <p>This method allows access to Phoenix-specific configuration and methods
   * that are not exposed through the {@link PIDMotor} interface, such as
   * advanced motion profiling parameters and diagnostic information.
   * 
   * @return the underlying {@link TalonFX} hardware object
   */
  public TalonFX motor() {
    return motor;
  }

  /**
   * Sets the neutral mode (brake/coast) of the motor controller.
   * 
   * <p>This method allows runtime changes to the motor's neutral behavior:
   * <ul>
   * <li>{@code NeutralModeValue.Brake}: Motor actively resists motion when disabled</li>
   * <li>{@code NeutralModeValue.Coast}: Motor freewheels when disabled</li>
   * </ul>
   * 
   * @param mode the neutral mode to set
   */
  public void setNeutralMode(NeutralModeValue mode) {
    motor.setNeutralMode(mode);
  }

  /**
   * Configures the PIDF constants for a specific closed-loop slot.
   * 
   * <p>The TalonFX supports multiple PID slot configurations that can be switched
   * between during operation. This allows for different control parameters for
   * different operating conditions (e.g., different PID gains for velocity vs position control).
   * 
   * <p><b>Note:</b> The 'f' parameter represents velocity feedforward (kV) in Phoenix 6,
   * not traditional feedforward gain.
   * 
   * @param slot the closed-loop slot to configure
   * @param p the proportional gain (kP)
   * @param i the integral gain (kI)
   * @param d the derivative gain (kD)
   * @param f the velocity feedforward gain (kV)
   */
  @Override
  public void configPIDF(int slot, double p, double i, double d, double f) {
    SlotConfigs conf = new SlotConfigs();
    conf.SlotNumber = slot;
    motor.getConfigurator().apply(conf.withKP(p).withKI(i).withKD(d).withKV(f));
  }

  /**
   * Configures the PIDF constants for the default closed-loop slot (slot 0).
   * 
   * <p>This is a convenience method that calls {@link #configPIDF(int, double, double, double, double)}
   * with slot 0.
   * 
   * @param p the proportional gain (kP)
   * @param i the integral gain (kI)
   * @param d the derivative gain (kD)
   * @param f the velocity feedforward gain (kV)
   */
  @Override
  public void configPIDF(double p, double i, double d, double f) {
    configPIDF(0, p, i, d, f);
  }

  /**
   * Configures the PID constants for a specific closed-loop slot.
   * 
   * <p>This is a convenience method that calls {@link #configPIDF(int, double, double, double, double)}
   * with a velocity feedforward value of 0.
   * 
   * @param slot the closed-loop slot to configure
   * @param p the proportional gain (kP)
   * @param i the integral gain (kI)
   * @param d the derivative gain (kD)
   */
  @Override
  public void configPID(int slot, double p, double i, double d) {
    configPIDF(slot, p, i, d, 0);
  }

  /**
   * Configures the PID constants for the default closed-loop slot (slot 0).
   * 
   * <p>This is a convenience method that calls {@link #configPID(int, double, double, double)}
   * with slot 0.
   * 
   * @param p the proportional gain (kP)
   * @param i the integral gain (kI)
   * @param d the derivative gain (kD)
   */
  @Override
  public void configPID(double p, double i, double d) {
    configPIDF(0, p, i, d, 0);
  }

  /**
   * Adds a follower TalonFX motor controller on the specified CAN bus.
   * 
   * <p>Follower motors will automatically mirror the output of the primary motor
   * but can have independent inversion settings. The wrapper supports both strict
   * following and regular following modes based on the invert type:
   * 
   * <ul>
   * <li><b>Strict Following:</b> Used when {@code invertType} is a rotation value 
   *     (CLOCKWISE/COUNTER_CLOCKWISE). The follower uses hardware-level strict following
   *     with the specified absolute inversion.</li>
   * <li><b>Regular Following:</b> Used when {@code invertType} is FOLLOW_MASTER or 
   *     OPPOSE_MASTER. The follower uses regular following with relative inversion.</li>
   * </ul>
   * 
   * <p>The follower TalonFX object is retained in the followers list to prevent
   * garbage collection and maintain the hardware relationship.
   * 
   * @param deviceNumber the CAN ID of the follower TalonFX, must be in range [0, 62]
   * @param canbus the CAN bus that the follower is connected to
   * @param invertType the inversion type for the follower motor
   * @throws InvalidCanIdException if the CAN ID is invalid (outside range [0, 62])
   */
  public void addFollower(int deviceNumber, String canbus, InvertType invertType) {
    TalonFX follower = new TalonFX(checkCanId(deviceNumber), canbus);
    if (InvertType.rotationValues.contains(invertType)) {
      TalonFXConfiguration conf = new TalonFXConfiguration();
      // guaranteed to succeed
      conf.MotorOutput.Inverted = invertType.phoenixInvert().orElseThrow(AssertionError::new);
      follower.setControl(new StrictFollower(information.id()));
    } else {
      follower.setControl(
          new Follower(information.id(), invertType.equals(InvertType.OPPOSE_MASTER)));
    }
    followers.add(follower); // add to follower list so TalonFX follower object is preserved
  }

  /**
   * Adds a follower TalonFX motor controller on the default CAN bus.
   * 
   * <p>This is a convenience method that calls {@link #addFollower(int, String, InvertType)}
   * with the default CAN bus. See that method for detailed behavior documentation.
   * 
   * @param deviceNumber the CAN ID of the follower TalonFX, must be in range [0, 62]
   * @param invertType the inversion type for the follower motor
   * @throws InvalidCanIdException if the CAN ID is invalid (outside range [0, 62])
   */
  public void addFollower(int deviceNumber, InvertType invertType) {
    TalonFX follower = new TalonFX(checkCanId(deviceNumber));
    if (InvertType.rotationValues.contains(invertType)) {
      TalonFXConfiguration conf = new TalonFXConfiguration();
      // guaranteed to succeed
      conf.MotorOutput.Inverted = invertType.phoenixInvert().orElseThrow(AssertionError::new);
      follower.setControl(new StrictFollower(information.id()));
    } else {
      follower.setControl(
          new Follower(information.id(), invertType.equals(InvertType.OPPOSE_MASTER)));
    }
    followers.add(follower); // add to follower list so TalonFX follower object is preserved
  }
}