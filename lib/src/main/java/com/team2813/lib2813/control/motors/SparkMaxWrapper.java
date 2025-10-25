package com.team2813.lib2813.control.motors;

import com.revrobotics.RelativeEncoder;
import com.revrobotics.spark.ClosedLoopSlot;
import com.revrobotics.spark.SparkBase;
import com.revrobotics.spark.SparkLowLevel;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.AlternateEncoderConfig;
import com.revrobotics.spark.config.ClosedLoopConfig;
import com.revrobotics.spark.config.SparkBaseConfig;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.team2813.lib2813.control.ControlMode;
import com.team2813.lib2813.control.InvertType;
import com.team2813.lib2813.control.PIDMotor;
import com.team2813.lib2813.util.ConfigUtils;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import java.util.ArrayList;
import java.util.List;

/**
 * SPARK MAX motor controller wrapper implementing PID control with unit-safe methods. Handles
 * configuration persistence, parameter resets, and follower motor synchronization.
 */
public class SparkMaxWrapper implements PIDMotor {
  // Stores follower motors to prevent garbage collection and maintain synchronization
  private final List<SparkMax> followers = new ArrayList<>();
  private final SparkBase motor;
  private final RelativeEncoder encoder;
  private final boolean inverted;

  // Configuration state management
  private final SparkBaseConfig config;
  private final SparkBase.ResetMode resetMode;
  private final SparkBase.PersistMode persistMode;

  /**
   * Creates a new SPARK MAX wrapper with specified configuration. Initializes the controller with
   * non-persistent parameters that reset safely, preventing configuration conflicts during code
   * updates or power cycles.
   *
   * @param deviceId CAN ID (1-62)
   * @param type Motor type (brushed/brushless)
   * @param inverted Direction setting (affects both standalone and follower behavior)
   */
  public SparkMaxWrapper(int deviceId, SparkLowLevel.MotorType type, InvertType inverted) {
    motor = new SparkMax(deviceId, type);
    this.inverted = inverted.sparkMaxInvert().orElseThrow();
    // Apply inversion settings through alternate encoder config to ensure proper direction control
    config = new SparkMaxConfig().apply(new AlternateEncoderConfig().inverted(this.inverted));
    persistMode = SparkBase.PersistMode.kNoPersistParameters;
    resetMode = SparkBase.ResetMode.kResetSafeParameters;
    ConfigUtils.revConfig(() -> motor.configure(config, resetMode, persistMode));
    encoder = motor.getEncoder();
  }

  /**
   * @param controlMode Control mode to use
   * @param demand Setpoint value
   */
  @Override
  public void set(ControlMode controlMode, double demand) {
    set(controlMode, demand, 0);
  }

  /**
   * @param controlMode Control mode (VOLTAGE: -12V to +12V, DUTY_CYCLE: -1.0 to +1.0)
   * @param demand Setpoint value
   * @param feedForward Additional feed-forward term
   */
  @Override
  public void set(ControlMode controlMode, double demand, double feedForward) {
    switch (controlMode) {
      case VOLTAGE:
        motor.setVoltage(demand);
        break;
      case DUTY_CYCLE:
      default:
        motor.set(demand);
        break;
    }
  }

  /**
   * @return Current position in rotations
   */
  @Override
  public double position() {
    return encoder.getPosition();
  }

  /**
   * @return Current position as an Angle measure
   */
  @Override
  public Angle getPositionMeasure() {
    return Units.Rotations.of(encoder.getPosition());
  }

  /**
   * @return Applied current in amperes
   */
  @Override
  public Current getAppliedCurrent() {
    return Units.Amps.of(motor.getOutputCurrent());
  }

  @Override
  public void disable() {
    motor.stopMotor();
  }

  /**
   * @param position Position in rotations
   */
  @Override
  public void setPosition(double position) {
    encoder.setPosition(position);
  }

  /**
   * @param position Position as an Angle measure
   */
  @Override
  public void setPosition(Angle position) {
    encoder.setPosition(position.in(Units.Rotations));
  }

  /**
   * @return Velocity in RPM
   */
  @Override
  public double getVelocity() {
    return encoder.getVelocity();
  }

  /**
   * @return Velocity as an AngularVelocity measure
   */
  @Override
  public AngularVelocity getVelocityMeasure() {
    return Units.Rotations.per(Units.Minute).of(encoder.getVelocity());
  }

  /**
   * Configures closed-loop control gains for a specific slot. Each slot can store different PID
   * configurations for various operating modes (e.g., different gains for high speed vs. precise
   * positioning).
   *
   * @param slot Slot index (0-3)
   * @param p Proportional gain - corrects present error
   * @param i Integral gain - corrects accumulated error
   * @param d Derivative gain - reduces oscillation
   * @param f Feed-forward gain - provides base output
   * @throws RuntimeException if slot is invalid
   */
  @Override
  public void configPIDF(int slot, double p, double i, double d, double f) {
    ClosedLoopSlot[] slots = ClosedLoopSlot.values();
    if (slot < 0 || slot > slots.length) {
      throw new RuntimeException("Invalid slot!");
    }
    ClosedLoopSlot cSlot = slots[slot];
    config.apply(new ClosedLoopConfig().pidf(p, i, d, f, cSlot));
    ConfigUtils.revConfig(() -> motor.configure(config, resetMode, persistMode));
  }

  // Convenience methods defaulting to slot 0
  @Override
  public void configPIDF(double p, double i, double d, double f) {
    configPIDF(0, p, i, d, f);
  }

  @Override
  public void configPID(int slot, double p, double i, double d) {
    configPIDF(slot, p, i, d, 0);
  }

  @Override
  public void configPID(double p, double i, double d) {
    configPIDF(0, p, i, d, 0);
  }

  /**
   * Configures a motor to follow this one's output. The follower motor is added to an internal list
   * to prevent garbage collection, ensuring the follower configuration persists throughout the
   * program's lifetime.
   *
   * @param deviceId CAN ID of follower (1-62)
   * @param type Motor type of follower
   * @param inverted Direction relative to leader: FOLLOW/OPPOSE_MASTER for relative inversion,
   *     CLOCKWISE/COUNTER_CLOCKWISE for absolute direction
   */
  public void addFollower(int deviceId, SparkLowLevel.MotorType type, InvertType inverted) {
    SparkMax follower = new SparkMax(deviceId, type);
    // Determine inversion based on whether the follower should match or oppose the leader
    boolean isInverted =
        switch (inverted) {
          case CLOCKWISE, COUNTER_CLOCKWISE -> inverted.sparkMaxInvert().orElseThrow();
          case FOLLOW_MASTER -> this.inverted;
          case OPPOSE_MASTER -> !this.inverted;
        };
    ConfigUtils.revConfig(
        () ->
            follower.configure(
                new SparkMaxConfig().follow(motor).inverted(isInverted), resetMode, persistMode));
    followers.add(follower); // Prevent garbage collection of follower configuration
  }
}
