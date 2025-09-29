package com.team2813.lib2813.control;

import edu.wpi.first.units.measure.Current;

/**
 * Interface defining the fundamental contract for motor control devices.
 * 
 * <p>This interface provides a vendor-neutral abstraction for motor controllers,
 * enabling standardized motor control across different hardware implementations.
 * It defines the essential operations needed for motor control: setting output
 * values with various control modes and monitoring current draw.
 * 
 * <p>The interface supports multiple control modes through the {@link ControlMode}
 * enumeration, allowing the same code to work with different control strategies
 * such as duty cycle, voltage, velocity, and motion magic control. This abstraction
 * enables easy switching between control modes without changing the calling code.
 * 
 * <p>Key features:
 * <ul>
 * <li>Vendor-neutral motor control abstraction</li>
 * <li>Multiple control modes with consistent API</li>
 * <li>Feedforward support for advanced control</li>
 * <li>Type-safe current monitoring using WPILib units</li>
 * </ul>
 * 
 * <p>Common implementations include TalonFX controllers, SPARK MAX controllers,
 * and other motor control devices that support the specified control modes.
 * 
 * @author Team 2813
 * @since 1.0
 */
public interface Motor {

    /**
     * Sets the motor to run with a specified control mode and demand value.
     * 
     * <p>This method provides basic motor control without feedforward compensation.
     * The interpretation of the demand value depends on the selected control mode:
     * 
     * <ul>
     * <li><b>DUTY_CYCLE:</b> Demand represents percentage output (-1.0 to +1.0)</li>
     * <li><b>VOLTAGE:</b> Demand represents voltage to apply (in volts)</li>
     * <li><b>VELOCITY:</b> Demand represents target velocity (units depend on implementation)</li>
     * <li><b>MOTION_MAGIC:</b> Demand represents target position (units depend on implementation)</li>
     * </ul>
     * 
     * <p>This is equivalent to calling {@link #set(ControlMode, double, double)}
     * with a feedforward value of 0.0.
     * 
     * @param mode the control mode to use for motor operation
     * @param demand the demand value whose interpretation depends on the control mode
     */
    void set(ControlMode mode, double demand);

    /**
     * Sets the motor to run with a specified control mode, demand value, and feedforward.
     * 
     * <p>This method provides advanced motor control with feedforward compensation
     * to improve control performance. Feedforward helps the controller anticipate
     * the required output, reducing tracking error and improving response time.
     * 
     * <p>The demand value interpretation is the same as in {@link #set(ControlMode, double)}.
     * The feedforward value provides additional output that is added to the
     * closed-loop controller output:
     * 
     * <ul>
     * <li><b>Velocity Control:</b> Feedforward often represents the expected motor
     *     voltage needed to maintain the target velocity</li>
     * <li><b>Position Control:</b> Feedforward can compensate for gravity, friction,
     *     or other predictable loads</li>
     * <li><b>Open-loop modes:</b> Feedforward is typically ignored or added directly
     *     to the output</li>
     * </ul>
     * 
     * <p>Feedforward values are typically calculated based on system identification
     * or theoretical models of the mechanism being controlled.
     * 
     * @param mode the control mode to use for motor operation
     * @param demand the demand value whose interpretation depends on the control mode
     * @param feedForward the feedforward compensation value to improve control performance
     */
    void set(ControlMode mode, double demand, double feedForward);

    /**
     * Gets the current that is currently being applied to the motor.
     * 
     * <p>This method returns the actual current flowing through the motor windings,
     * which is useful for:
     * <ul>
     * <li>Monitoring motor load and detecting mechanical binding</li>
     * <li>Implementing current-based control strategies</li>
     * <li>Ensuring motors operate within safe current limits</li>
     * <li>Diagnostic monitoring and fault detection</li>
     * <li>Power consumption analysis</li>
     * </ul>
     * 
     * <p>The returned current measurement uses WPILib's type-safe units system,
     * allowing easy conversion between different current units (amps, milliamps)
     * and providing type safety in calculations.
     * 
     * <p>Example usage:
     * <pre>{@code
     * Current motorCurrent = motor.getAppliedCurrent();
     * double amps = motorCurrent.in(Units.Amps);
     * if (amps > 30.0) {
     *     // Handle high current condition
     * }
     * }</pre>
     * 
     * @return the current applied current as a type-safe {@link Current} measurement
     * @since 2.0.0
     */
    Current getAppliedCurrent();
}