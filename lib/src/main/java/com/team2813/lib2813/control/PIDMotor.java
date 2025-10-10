package com.team2813.lib2813.control;

/**
 * Interface defining a motor controller with integrated encoder feedback and PID control
 * capabilities.
 *
 * <p>This interface combines the functionality of both {@link Motor} and {@link Encoder}
 * interfaces, representing motor controllers that have built-in position/velocity feedback and can
 * perform closed-loop PID control. This is the most common type of motor controller used in
 * competitive robotics, where precise control and position feedback are essential.
 *
 * <p>The interface extends the basic motor control capabilities with PID configuration methods that
 * allow tuning of closed-loop control parameters. Multiple PID slots are supported to enable
 * different control parameters for different operating conditions (e.g., different gains for
 * velocity vs position control, or different gains for different mechanisms).
 *
 * <p>Key features inherited and added:
 *
 * <ul>
 *   <li><b>From Motor:</b> Control mode support, current monitoring, feedforward capability
 *   <li><b>From Encoder:</b> Position and velocity feedback with type-safe units
 *   <li><b>Added:</b> PID parameter configuration with multi-slot support
 * </ul>
 *
 * <p>Common implementations include:
 *
 * <ul>
 *   <li>TalonFX controllers with integrated encoders
 *   <li>SPARK MAX controllers with attached encoders
 *   <li>Other smart motor controllers with feedback and PID capabilities
 * </ul>
 *
 * <p>This interface enables sophisticated control strategies such as:
 *
 * <ul>
 *   <li>Velocity control for flywheels and intake mechanisms
 *   <li>Position control for arms, elevators, and turrets
 *   <li>Motion magic for smooth, profiled movements
 *   <li>Cascaded control loops with multiple feedback sources
 * </ul>
 *
 * @author Team 2813
 * @since 1.0
 */
public interface PIDMotor extends Motor, Encoder {

  /**
   * Configures the PID and feedforward constants for a specific control slot.
   *
   * <p>Motor controllers typically support multiple PID parameter sets (slots) that can be switched
   * between during operation. This allows different control parameters for different operating
   * conditions without reconfiguration overhead.
   *
   * <p>PID parameter meanings:
   *
   * <ul>
   *   <li><b>P (Proportional):</b> Responds to current error magnitude
   *   <li><b>I (Integral):</b> Eliminates steady-state error by accumulating past error
   *   <li><b>D (Derivative):</b> Reduces oscillation by responding to error rate of change
   *   <li><b>F (Feedforward):</b> Provides base output for known system behavior
   * </ul>
   *
   * <p><b>Note:</b> The interpretation of the 'f' parameter may vary by implementation. Some
   * controllers use it for velocity feedforward (kV), while others use it for arbitrary feedforward
   * (kF). Consult the specific implementation documentation.
   *
   * @param slot the PID slot number to configure (typically 0-3, range depends on implementation)
   * @param p the proportional gain coefficient
   * @param i the integral gain coefficient
   * @param d the derivative gain coefficient
   * @param f the feedforward gain coefficient
   */
  void configPIDF(int slot, double p, double i, double d, double f);

  /**
   * Configures the PID and feedforward constants for the default control slot (slot 0).
   *
   * <p>This is a convenience method equivalent to calling {@link #configPIDF(int, double, double,
   * double, double)} with slot 0. Most applications start with slot 0 for their primary control
   * parameters.
   *
   * <p>This method is ideal for simple applications that only need one set of PID parameters or for
   * initial tuning before implementing more advanced multi-slot strategies.
   *
   * @param p the proportional gain coefficient
   * @param i the integral gain coefficient
   * @param d the derivative gain coefficient
   * @param f the feedforward gain coefficient
   */
  void configPIDF(double p, double i, double d, double f);

  /**
   * Configures the PID constants for a specific control slot without feedforward.
   *
   * <p>This method is equivalent to calling {@link #configPIDF(int, double, double, double,
   * double)} with a feedforward value of 0. It's useful when feedforward is not needed or will be
   * handled separately through the {@link #set(ControlMode, double, double)} method.
   *
   * <p>Pure PID control without feedforward is common for:
   *
   * <ul>
   *   <li>Position control where the mechanism is balanced (no gravity/friction compensation
   *       needed)
   *   <li>Systems where disturbances are unpredictable
   *   <li>Initial tuning stages before adding feedforward refinement
   * </ul>
   *
   * @param slot the PID slot number to configure
   * @param p the proportional gain coefficient
   * @param i the integral gain coefficient
   * @param d the derivative gain coefficient
   */
  void configPID(int slot, double p, double i, double d);

  /**
   * Configures the PID constants for the default control slot (slot 0) without feedforward.
   *
   * <p>This is a convenience method equivalent to calling {@link #configPID(int, double, double,
   * double)} with slot 0. It provides the simplest interface for basic PID control configuration.
   *
   * <p>This method is perfect for:
   *
   * <ul>
   *   <li>Simple mechanisms that don't require feedforward
   *   <li>Initial testing and tuning of PID parameters
   *   <li>Applications where feedforward is applied externally
   * </ul>
   *
   * @param p the proportional gain coefficient
   * @param i the integral gain coefficient
   * @param d the derivative gain coefficient
   */
  void configPID(double p, double i, double d);
}
