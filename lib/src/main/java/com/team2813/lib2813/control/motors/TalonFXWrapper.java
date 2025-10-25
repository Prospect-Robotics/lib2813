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
 * Wrapper around a CTRE TalonFX providing a PIDMotor interface and safer defaults.
 *
 * <p>This wrapper applies conservative defaults at construction time:
 *
 * <ul>
 *   <li>40A stator current limit to protect motors during testing
 *   <li>Validated CAN ID to catch configuration errors early
 *   <li>Required inversion type to prevent accidental direction mismatches
 * </ul>
 *
 * <p>The follower list prevents garbage collection of follower motor objects, which is necessary
 * because Phoenix 6 uses weak references internally and would otherwise lose the follower
 * configuration.
 */
public class TalonFXWrapper implements PIDMotor {
  /**
   * Maintains strong references to follower motors to prevent GC. Phoenix 6 internally uses device
   * IDs for follower control, but the TalonFX objects must persist to maintain configuration state.
   */
  private final List<TalonFX> followers = new ArrayList<>();

  /** the internal motor */
  private final TalonFX motor;

  /**
   * Device identification (id + optional bus) used by follower APIs. Stored separately to avoid
   * repeated CAN bus queries.
   */
  private final DeviceInformation information;

  /**
   * Create a TalonFXWrapper on the specified canbus.
   *
   * @param canID [0, 62] the can ID of the motor
   * @param canbus the canbus that the motor is on
   * @param invertType the invert type
   * @throws NullPointerException if either {@code invertType} or {@code canbus} are null
   * @throws IllegalArgumentException if {@code invertType} is not in {@link
   *     InvertType#rotationValues}
   * @throws InvalidCanIdException if the CAN id is invalid
   */
  public TalonFXWrapper(int canID, String canbus, InvertType invertType) {
    Objects.requireNonNull(invertType, "invertType should not be null");
    Objects.requireNonNull(canbus, "canbus should not be null");
    if (!InvertType.rotationValues.contains(invertType)) {
      throw new IllegalArgumentException("invertType invalid");
    }
    motor = new TalonFX(checkCanId(canID), canbus);

    TalonFXConfiguration config = new TalonFXConfiguration();
    // rotationValues are guaranteed by the enum to map to Phoenix inversions;
    // if missing, it indicates a programming error, hence AssertionError.
    config.MotorOutput.Inverted = invertType.phoenixInvert().orElseThrow(AssertionError::new);
    // Conservative current limit for safety during development/testing
    config.CurrentLimits =
        new CurrentLimitsConfigs().withStatorCurrentLimit(40).withSupplyCurrentLimitEnable(true);
    TalonFXConfigurator configurator = motor.getConfigurator();
    configurator.apply(config);

    information = new DeviceInformation(canID, canbus);
  }

  /**
   * Create a TalonFXWrapper on the RoboRIO's default CAN bus.
   *
   * @param canID [0, 62] the can ID of the motor
   * @param invertType the invert type
   * @throws NullPointerException if {@code invertType} is null
   * @throws IllegalArgumentException if {@code invertType} is not in {@link
   *     InvertType#rotationValues}
   * @throws InvalidCanIdException if the CAN id is invalid
   */
  public TalonFXWrapper(int canID, InvertType invertType) {
    Objects.requireNonNull(invertType, "invertType should not be null");
    motor = new TalonFX(checkCanId(canID));
    if (!InvertType.rotationValues.contains(invertType)) {
      throw new IllegalArgumentException("invertType invalid");
    }

    TalonFXConfiguration config = new TalonFXConfiguration();
    config.MotorOutput.Inverted = invertType.phoenixInvert().orElseThrow(AssertionError::new);
    config.CurrentLimits =
        new CurrentLimitsConfigs().withStatorCurrentLimit(40).withSupplyCurrentLimitEnable(true);
    TalonFXConfigurator configurator = motor.getConfigurator();
    configurator.apply(config);

    information = new DeviceInformation(canID);
  }

  /**
   * Convenience overload forwarding to {@link #set(ControlMode,double,double)} with zero
   * feed-forward.
   *
   * @param controlMode control algorithm to use
   * @param demand setpoint value
   */
  public void set(ControlMode controlMode, double demand) {
    set(controlMode, demand, 0);
  }

  /**
   * Set controller output using Phoenix 6 control request objects.
   *
   * <p>Each control mode creates its own request object to leverage Phoenix 6's type-safe control
   * API. Request objects are created per-call rather than cached to ensure thread safety and avoid
   * state accumulation.
   *
   * @param controlMode control algorithm to use
   * @param demand numeric setpoint (units depend on mode)
   * @param feedForward feed-forward term applied where supported
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
   * @return current position in rotations
   */
  @Override
  public double position() {
    return getPositionMeasure().in(Units.Rotations);
  }

  /**
   * @return current position as a unit-safe {@link Angle}
   */
  @Override
  public Angle getPositionMeasure() {
    return Units.Rotations.of(motor.getPosition().getValueAsDouble());
  }

  /**
   * @return applied stator current as a unit-safe {@link Current}
   */
  @Override
  public Current getAppliedCurrent() {
    return motor.getStatorCurrent().getValue();
  }

  /**
   * Set encoder position in raw rotations.
   *
   * @param position position in rotations
   */
  @Override
  public void setPosition(double position) {
    motor.setPosition(position);
  }

  /**
   * Set encoder position using a unit-safe {@link Angle}.
   *
   * @param position angle to set (converted to rotations)
   */
  @Override
  public void setPosition(Angle position) {
    motor.setPosition(position.in(Units.Rotations));
  }

  /**
   * @return sensor velocity as raw double (device units: rotations/sec)
   */
  @Override
  public double getVelocity() {
    return motor.getVelocity().getValueAsDouble();
  }

  /**
   * @return velocity as a unit-safe {@link AngularVelocity}
   */
  @Override
  public AngularVelocity getVelocityMeasure() {
    return Units.RotationsPerSecond.of(motor.getVelocity().getValueAsDouble());
  }

  /**
   * Expose the underlying TalonFX instance for advanced use.
   *
   * @return the raw TalonFX
   */
  public TalonFX motor() {
    return motor;
  }

  /**
   * Sets the behavior the motor should exhibit upon receiving a request to stop.
   *
   * @param mode neutral mode (COAST/BRAKE)
   */
  public void setNeutralMode(NeutralModeValue mode) {
    motor.setNeutralMode(mode);
  }

  /**
   * Sends a disable command to the motor, placing it in its neutral value.
   *
   * @see TalonFXWrapper#setNeutralMode(NeutralModeValue)
   */
  @Override
  public void disable() {
    motor.disable();
  }

  /**
   * Configure PIDF gains for a slot using the CTRE SlotConfigs API.
   *
   * @param slot slot index
   * @param p proportional gain
   * @param i integral gain
   * @param d derivative gain
   * @param f feed-forward (velocity/feed-forward term)
   */
  @Override
  public void configPIDF(int slot, double p, double i, double d, double f) {
    SlotConfigs conf = new SlotConfigs();
    conf.SlotNumber = slot;
    motor.getConfigurator().apply(conf.withKP(p).withKI(i).withKD(d).withKV(f));
  }

  /**
   * Configure PIDF on default slot 0.
   *
   * @param p proportional
   * @param i integral
   * @param d derivative
   * @param f feed-forward
   */
  @Override
  public void configPIDF(double p, double i, double d, double f) {
    configPIDF(0, p, i, d, f);
  }

  /**
   * Configure PID on a specific slot (no feed-forward).
   *
   * @param slot slot index
   * @param p proportional
   * @param i integral
   * @param d derivative
   */
  @Override
  public void configPID(int slot, double p, double i, double d) {
    configPIDF(slot, p, i, d, 0);
  }

  /**
   * Configure PID on default slot 0 (no feed-forward).
   *
   * @param p proportional
   * @param i integral
   * @param d derivative
   */
  @Override
  public void configPID(double p, double i, double d) {
    configPIDF(0, p, i, d, 0);
  }

  /**
   * Add a follower motor on a specified CAN bus.
   *
   * <p>Phoenix 6 provides two follower modes:
   *
   * <ul>
   *   <li><b>StrictFollower:</b> Used when the follower has an absolute direction
   *       (CLOCKWISE/COUNTER_CLOCKWISE). The follower's inversion is configured independently of
   *       the leader.
   *   <li><b>Follower:</b> Used for relative inversion (FOLLOW/OPPOSE_MASTER). The second parameter
   *       determines if output is inverted relative to leader.
   * </ul>
   *
   * @param deviceNumber CAN id of follower (0-62)
   * @param canbus CAN bus name
   * @param invertType inversion rule
   */
  public void addFollower(int deviceNumber, String canbus, InvertType invertType) {
    TalonFX follower = new TalonFX(checkCanId(deviceNumber), canbus);
    if (InvertType.rotationValues.contains(invertType)) {
      TalonFXConfiguration conf = new TalonFXConfiguration();
      conf.MotorOutput.Inverted = invertType.phoenixInvert().orElseThrow(AssertionError::new);
      follower.setControl(new StrictFollower(information.id()));
    } else {
      follower.setControl(
          new Follower(information.id(), invertType.equals(InvertType.OPPOSE_MASTER)));
    }
    followers.add(follower);
  }

  /**
   * Add a follower motor on the default CAN bus.
   *
   * @param deviceNumber CAN id of follower (0-62)
   * @param invertType inversion rule
   */
  public void addFollower(int deviceNumber, InvertType invertType) {
    TalonFX follower = new TalonFX(checkCanId(deviceNumber));
    if (InvertType.rotationValues.contains(invertType)) {
      TalonFXConfiguration conf = new TalonFXConfiguration();
      conf.MotorOutput.Inverted = invertType.phoenixInvert().orElseThrow(AssertionError::new);
      follower.setControl(new StrictFollower(information.id()));
    } else {
      follower.setControl(
          new Follower(information.id(), invertType.equals(InvertType.OPPOSE_MASTER)));
    }
    followers.add(follower);
  }
}
