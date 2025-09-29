package com.team2813.lib2813.control;

import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;

/**
 * Interface specifying a device that can perceive rotational positions and velocities.
 * 
 * <p>This interface defines the contract for encoder devices that provide angular
 * position and velocity feedback. It supports both legacy double-based methods
 * (deprecated) and modern type-safe unit-based methods using WPILib's units system.
 * 
 * <p>The interface provides a migration path from unsafe raw double values to
 * type-safe {@link Angle} and {@link AngularVelocity} measurements. New implementations
 * should focus on the unit-safe methods, while legacy methods are maintained for
 * backward compatibility but marked for removal.
 * 
 * <p>Key features:
 * <ul>
 * <li>Type-safe position and velocity measurements using WPILib units</li>
 * <li>Default implementations to ease migration from legacy methods</li>
 * <li>Backward compatibility with existing code through deprecated methods</li>
 * <li>Flexible unit conversion through the units system</li>
 * </ul>
 * 
 * <p>Common implementations include absolute encoders (CANcoder), relative encoders
 * (integrated motor encoders), and other rotational position sensing devices.
 * 
 * @author Team 2813
 * @since 1.0
 */
public interface Encoder {

    /**
     * Gets the current position of the encoder as a raw double value.
     * 
     * <p><b>Warning:</b> This method returns position without specifying units,
     * making it unsafe for reliable position calculations. The actual units
     * depend on the specific encoder implementation and configuration.
     * 
     * @return the position of the encoder as an unspecified double value
     * @deprecated This method does not specify position in a specific measurement, so it is not safe
     *             to use. Use {@link #getPositionMeasure()} instead for type safety
     */
    @Deprecated(forRemoval = true)
    double position();

    /**
     * Gets the current position of the encoder using type-safe units.
     * 
     * <p>This method returns the encoder position as an {@link Angle} measurement,
     * providing type safety and explicit unit handling. The returned angle can be
     * easily converted to any angular unit (degrees, radians, rotations) using
     * the WPILib units system.
     * 
     * <p>Example usage:
     * <pre>{@code
     * Angle position = encoder.getPositionMeasure();
     * double degrees = position.in(Units.Degrees);
     * double rotations = position.in(Units.Rotations);
     * }</pre>
     * 
     * @return the current position of the encoder as an {@link Angle} measurement
     */
    Angle getPositionMeasure();

    /**
     * Sets the encoder position to the specified raw double value.
     * 
     * <p><b>Warning:</b> This method accepts position without specifying units,
     * making it unsafe and ambiguous. The interpretation of the position value
     * depends on the specific encoder implementation and configuration.
     * 
     * @param position the new position value as an unspecified double
     * @deprecated This method does not specify a unit, so it is not safe to use.
     *             Use {@link #setPosition(Angle)} instead for type safety
     */
    @Deprecated(forRemoval = true)
    void setPosition(double position);

    /**
     * Sets the encoder position using type-safe units.
     * 
     * <p>This method accepts any {@link Angle} measurement and converts it to
     * the encoder's native units for setting the position. The type-safe approach
     * eliminates unit confusion and provides clear, readable code.
     * 
     * <p>The default implementation converts the angle to radians and calls the
     * legacy {@link #setPosition(double)} method. Implementations should override
     * this method to provide direct unit-safe position setting when possible.
     * 
     * <p>Example usage:
     * <pre>{@code
     * encoder.setPosition(Units.Degrees.of(90));
     * encoder.setPosition(Units.Rotations.of(0.25));
     * }</pre>
     * 
     * @param position the new position as an {@link Angle} measurement
     */
    default void setPosition(Angle position) {
        setPosition(position.in(Units.Radians));
    }

    /**
     * Gets the current velocity of the encoder as a raw double value.
     * 
     * <p><b>Warning:</b> This method returns velocity without specifying units,
     * making it unsafe for reliable velocity calculations. The actual units
     * depend on the specific encoder implementation and configuration (could be
     * RPM, radians/sec, rotations/sec, etc.).
     * 
     * @return the velocity that the encoder perceives as an unspecified double value
     * @deprecated This method does not specify velocity in a specific measurement, so it is not safe
     *             to use. Use {@link #getVelocityMeasure()} instead for type safety
     */
    @Deprecated(forRemoval = true)
    double getVelocity();

    /**
     * Gets the current velocity of the encoder using type-safe units.
     * 
     * <p>This method returns the encoder velocity as an {@link AngularVelocity}
     * measurement, providing type safety and explicit unit handling. The returned
     * velocity can be easily converted to any angular velocity unit using the
     * WPILib units system.
     * 
     * <p>The default implementation assumes the legacy {@link #getVelocity()} method
     * returns radians per second and wraps it in a type-safe measurement.
     * Implementations should override this method to provide the correct units
     * for their specific hardware.
     * 
     * <p>Example usage:
     * <pre>{@code
     * AngularVelocity velocity = encoder.getVelocityMeasure();
     * double rpm = velocity.in(Units.RPM);
     * double radPerSec = velocity.in(Units.RadiansPerSecond);
     * }</pre>
     * 
     * @return the current velocity as an {@link AngularVelocity} measurement
     */
    default AngularVelocity getVelocityMeasure() {
        return Units.RadiansPerSecond.of(getVelocity());
    }
}