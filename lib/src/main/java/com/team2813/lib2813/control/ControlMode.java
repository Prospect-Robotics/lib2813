package com.team2813.lib2813.control;

import com.revrobotics.spark.SparkBase.ControlType;

/**
 * Enumeration defining standardized motor control modes for the team's control system.
 *
 * <p>This enum provides a vendor-neutral abstraction over different motor controller control modes
 * while maintaining compatibility with specific hardware implementations. Each control mode maps to
 * the appropriate vendor-specific control type for seamless integration with different motor
 * controller families.
 *
 * <p>The enum currently includes mappings for REV Robotics SPARK controllers, with each mode
 * corresponding to a specific {@link ControlType} from the SPARK API. Additional vendor mappings
 * can be added as needed.
 *
 * @author Team 2813
 * @since 1.0
 */
public enum ControlMode {

  /**
   * Open-loop duty cycle control mode.
   *
   * <p>Controls the motor by setting the duty cycle (percentage of time the motor is receiving a
   * signal/the signal is on) directly. The motor output is proportional to the pulse width,
   * typically ranging from -1.0 (full reverse) to +1.0 (full forward). This is the most basic
   * control mode and does not use feedback control.
   *
   * <p>Maps to {@link ControlType#kDutyCycle} for SPARK controllers.
   *
   * <p>Here is a visualization of the duty cycle: <hr> <br>
   * <br>
   * <img src="../doc-files/dutycyclediagram.jpeg" width-"100"></img>
   */
  DUTY_CYCLE(ControlType.kDutyCycle),

  /**
   * Closed-loop velocity control mode.
   *
   * <p>Controls the motor to maintain a specific velocity using PID feedback control. The
   * controller continuously adjusts the motor output to minimize the error between the demanded
   * velocity and the actual measured velocity from the encoder. This mode is ideal for applications
   * requiring consistent speed regardless of load variations.
   *
   * <p>Maps to {@link ControlType#kVelocity} for SPARK controllers.
   */
  VELOCITY(ControlType.kVelocity),

  /**
   * Closed-loop position control mode with motion profiling.
   *
   * <p>Controls the motor to reach a specific position using advanced motion profiling algorithms.
   * The controller generates smooth velocity and acceleration profiles to move the mechanism to the
   * target position while respecting configured motion constraints (max velocity, max
   * acceleration). This mode provides the smoothest and most controlled movement for precise
   * positioning applications.
   *
   * <p>Maps to {@link ControlType#kPosition} for SPARK controllers.
   *
   * <p><b>Note:</b> Despite the name "MOTION_MAGIC," this maps to position control type for SPARK
   * controllers, as the motion profiling is handled internally.
   */
  MOTION_MAGIC(ControlType.kPosition),

  /**
   * Open-loop voltage control mode.
   *
   * <p>Controls the motor by applying a specific voltage directly to the motor terminals. Unlike
   * duty cycle control, voltage control compensates for battery voltage variations to provide more
   * consistent motor behavior. The demand value represents the desired voltage (in Volts) to apply
   * to the motor.
   *
   * <p>Maps to {@link ControlType#kVoltage} for SPARK controllers.
   */
  VOLTAGE(ControlType.kVoltage);

  /** The corresponding SPARK controller control type for this mode */
  private final ControlType sparkMode;

  /**
   * Creates a ControlMode with the specified SPARK controller mapping.
   *
   * @param sparkMode the corresponding {@link ControlType} for SPARK controllers
   */
  ControlMode(ControlType sparkMode) {
    this.sparkMode = sparkMode;
  }

  /**
   * Gets the corresponding SPARK controller control type for this mode.
   *
   * <p>This method provides the mapping from the vendor-neutral ControlMode to the specific {@link
   * ControlType} required by REV Robotics SPARK controllers. This abstraction allows the same
   * control mode enum to be used across different motor controller implementations.
   *
   * <p>The last references to this method were removed a while ago.
   *
   * @return the corresponding {@link ControlType} for SPARK controllers
   */
  @Deprecated(forRemoval = true)
  public ControlType getSparkMode() {
    return sparkMode;
  }
}
