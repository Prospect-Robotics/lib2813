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
 * Wrapper class for REV Robotics SPARK MAX motor controller.
 * 
 * <p>This class provides a standardized interface for controlling SPARK MAX motor controllers
 * while maintaining compatibility with the team's control system architecture. It implements
 * the {@link PIDMotor} interface and supports both brushed and brushless motors with integrated
 * encoder feedback.
 * 
 * <p>Key features include:
 * <ul>
 * <li>Type-safe unit support using WPILib's units system</li>
 * <li>PID control configuration with multiple slot support</li>
 * <li>Follower motor management with independent inversion settings</li>
 * <li>Reliable configuration using {@link ConfigUtils} for REV devices</li>
 * <li>Support for various control modes (duty cycle, voltage)</li>
 * </ul>
 * 
 * <p>The wrapper uses safe parameter reset mode and non-persistent parameters by default
 * for reliable operation during competition.
 * 
 * @author Team 2813
 * @since 1.0
 */
public class SparkMaxWrapper implements PIDMotor {
  
  /** List of follower SPARK MAX controllers managed by this wrapper */
  private final List<SparkMax> followers = new ArrayList<>();
  
  /** The primary SPARK MAX motor controller */
  private final SparkBase motor;
  
  /** The integrated relative encoder from the SPARK MAX */
  private final RelativeEncoder encoder;
  
  /** Whether the motor output is inverted */
  private final boolean inverted;
  
  /** Configuration object for the SPARK MAX */
  private final SparkBaseConfig config;
  
  /** Reset mode used for configuration operations */
  private final SparkBase.ResetMode resetMode;
  
  /** Persistence mode used for configuration operations */
  private final SparkBase.PersistMode persistMode;

  /**
   * Creates a new SparkMaxWrapper to control a SPARK MAX motor controller.
   * 
   * <p>The constructor initializes the motor controller with safe default settings:
   * <ul>
   * <li>Reset mode: {@code kResetSafeParameters} for reliable startup</li>
   * <li>Persist mode: {@code kNoPersistParameters} to prevent parameter corruption</li>
   * <li>Alternate encoder configuration with specified inversion</li>
   * </ul>
   * 
   * @param deviceId the CAN ID of the SPARK MAX controller
   * @param type the motor type connected to the controller. Brushless motor wires must be 
   *             connected to their matching colors and the hall sensor must be plugged in.
   *             Brushed motors must be connected to the Red and Black terminals only
   * @param inverted the inversion type for the motor output
   * @throws RuntimeException if the inversion type cannot be converted to a SPARK MAX setting
   */
  public SparkMaxWrapper(int deviceId, SparkLowLevel.MotorType type, InvertType inverted) {
    motor = new SparkMax(deviceId, type);
    this.inverted = inverted.sparkMaxInvert().orElseThrow();
    config = new SparkMaxConfig().apply(new AlternateEncoderConfig().inverted(this.inverted));
    persistMode = SparkBase.PersistMode.kNoPersistParameters;
    resetMode = SparkBase.ResetMode.kResetSafeParameters;
    ConfigUtils.revConfig(() -> motor.configure(config, resetMode, persistMode));
    encoder = motor.getEncoder();
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
  @Override
  public void set(ControlMode controlMode, double demand) {
    set(controlMode, demand, 0);
  }

  /**
   * Sets the motor output using the specified control mode, demand value, and feedforward.
   * 
   * <p>Supported control modes:
   * <ul>
   * <li>{@code VOLTAGE}: Sets motor voltage directly (demand in volts)</li>
   * <li>{@code DUTY_CYCLE}: Sets motor duty cycle (demand as fraction from -1.0 to 1.0)</li>
   * </ul>
   * 
   * <p><b>Note:</b> The feedforward parameter is currently ignored but included for
   * interface compatibility.
   * 
   * @param controlMode the control mode to use
   * @param demand the demand value (interpretation depends on control mode)
   * @param feedForward the feedforward value (currently unused)
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
   * Gets the current position of the motor encoder as a raw double value.
   * 
   * @deprecated Use {@link #getPositionMeasure()} instead for type safety with units
   * @return the current position in rotations as a double
   */
  @Override
  public double position() {
    return encoder.getPosition();
  }

  /**
   * Gets the current position of the motor encoder using type-safe units.
   * 
   * <p>This method returns the position as an {@link Angle} measurement in rotations,
   * which can be converted to other angular units as needed.
   * 
   * @return the current position as an {@link Angle} measurement
   */
  @Override
  public Angle getPositionMeasure() {
    return Units.Rotations.of(encoder.getPosition());
  }

  /**
   * Gets the current applied current of the motor controller.
   * 
   * <p>This represents the actual current being drawn by the motor, which can be
   * useful for monitoring motor load and detecting mechanical issues.
   * 
   * @return the current applied current as a {@link Current} measurement
   */
  @Override
  public Current getAppliedCurrent() {
    return Units.Amps.of(motor.getOutputCurrent());
  }

  /**
   * Sets the encoder position to the specified raw double value.
   * 
   * @deprecated Use {@link #setPosition(Angle)} instead for type safety with units
   * @param position the new position in rotations
   */
  @Override
  public void setPosition(double position) {
    encoder.setPosition(position);
  }

  /**
   * Sets the encoder position using type-safe units.
   * 
   * <p>This method accepts any {@link Angle} measurement and converts it to rotations
   * internally for the SPARK MAX encoder.
   * 
   * @param position the new position as an {@link Angle} measurement
   */
  @Override
  public void setPosition(Angle position) {
    encoder.setPosition(position.in(Units.Rotations));
  }

  /**
   * Gets the current velocity of the motor encoder as a raw double value.
   * 
   * @deprecated Use {@link #getVelocityMeasure()} instead for type safety with units
   * @return the current velocity in RPM as a double
   */
  @Override
  public double getVelocity() {
    return encoder.getVelocity();
  }

  /**
   * Gets the current velocity of the motor encoder using type-safe units.
   * 
   * <p>This method returns the velocity as an {@link AngularVelocity} measurement
   * in rotations per minute (RPM), which can be converted to other angular velocity
   * units as needed.
   * 
   * @return the current velocity as an {@link AngularVelocity} measurement
   */
  @Override
  public AngularVelocity getVelocityMeasure() {
    return Units.Rotations.per(Units.Minute).of(encoder.getVelocity());
  }

  /**
   * Configures the PIDF constants for a specific closed-loop slot.
   * 
   * <p>The SPARK MAX supports multiple PID slot configurations that can be switched
   * between during operation. This allows for different control parameters for
   * different operating conditions.
   * 
   * @param slot the closed-loop slot to configure (0-3)
   * @param p the proportional gain
   * @param i the integral gain  
   * @param d the derivative gain
   * @param f the feedforward gain
   * @throws RuntimeException if the slot number is invalid (not 0-3)
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

  /**
   * Configures the PIDF constants for the default closed-loop slot (slot 0).
   * 
   * <p>This is a convenience method that calls {@link #configPIDF(int, double, double, double, double)}
   * with slot 0.
   * 
   * @param p the proportional gain
   * @param i the integral gain
   * @param d the derivative gain  
   * @param f the feedforward gain
   */
  @Override
  public void configPIDF(double p, double i, double d, double f) {
    configPIDF(0, p, i, d, f);
  }

  /**
   * Configures the PID constants for a specific closed-loop slot.
   * 
   * <p>This is a convenience method that calls {@link #configPIDF(int, double, double, double, double)}
   * with a feedforward value of 0.
   * 
   * @param slot the closed-loop slot to configure (0-3)
   * @param p the proportional gain
   * @param i the integral gain
   * @param d the derivative gain
   * @throws RuntimeException if the slot number is invalid (not 0-3)
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
   * @param p the proportional gain
   * @param i the integral gain
   * @param d the derivative gain
   */
  @Override
  public void configPID(double p, double i, double d) {
    configPIDF(0, p, i, d, 0);
  }

  /**
   * Adds a follower SPARK MAX motor controller to this primary controller.
   * 
   * <p>Follower motors will automatically mirror the output of the primary motor
   * but can have independent inversion settings. The follower relationship is
   * configured at the hardware level for optimal performance.
   * 
   * <p>Inversion behavior:
   * <ul>
   * <li>{@code CLOCKWISE/COUNTER_CLOCKWISE}: Uses the specified absolute direction</li>
   * <li>{@code FOLLOW_MASTER}: Matches the primary motor's inversion setting</li>
   * <li>{@code OPPOSE_MASTER}: Inverts opposite to the primary motor's setting</li>
   * </ul>
   * 
   * <p>The follower SPARK MAX object is retained in the followers list to prevent
   * garbage collection and maintain the hardware relationship.
   * 
   * @param deviceId the CAN ID of the follower SPARK MAX
   * @param type the motor type connected to the follower controller
   * @param inverted the inversion type for the follower motor
   * @throws RuntimeException if the inversion type cannot be converted to a SPARK MAX setting
   */
  public void addFollower(int deviceId, SparkLowLevel.MotorType type, InvertType inverted) {
    SparkMax follower = new SparkMax(deviceId, type);
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
    followers.add(follower); // add to follower list so CANSparkMax follower object is preserved
  }
}