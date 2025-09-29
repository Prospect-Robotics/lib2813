package com.team2813.lib2813.control.encoders;

import com.ctre.phoenix6.hardware.CANcoder;
import com.team2813.lib2813.control.DeviceInformation;
import com.team2813.lib2813.control.Encoder;
import com.team2813.lib2813.util.ConfigUtils;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;

/**
 * Wrapper class for a CTRE Phoenix 6 CANcoder absolute encoder.
 * 
 * <p>This class provides a standardized interface for interacting with CANcoder devices
 * while maintaining compatibility with the team's control system architecture. It implements
 * the {@link Encoder} interface and supports both legacy double-based methods and modern
 * type-safe unit-based methods.
 * 
 * <p>The class includes deprecated methods for backward compatibility, but new code should
 * use the type-safe methods that work with WPILib's units system.
 * 
 * @author Team 2813
 * @since 1.0
 */
public class CancoderWrapper implements Encoder {
    
    /** The underlying CTRE CANcoder hardware object */
    private CANcoder cancoder;
    
    /** Device information containing ID and CAN bus details */
    private DeviceInformation info;

    /**
     * Creates a new CancoderWrapper with the specified CAN ID and bus name.
     * 
     * @param id the CAN ID of the CANcoder device
     * @param canbus the name of the CAN bus the device is connected to (e.g., "rio", "canivore")
     */
    public CancoderWrapper(int id, String canbus) {
        cancoder = new CANcoder(id, canbus);
        info = new DeviceInformation(id, canbus);
    }

    /**
     * Creates a new CancoderWrapper with the specified CAN ID on the default CAN bus.
     * 
     * @param id the CAN ID of the CANcoder device
     */
    public CancoderWrapper(int id) {
        cancoder = new CANcoder(id);
        info = new DeviceInformation(id);
    }

    /**
     * Gets the current position of the encoder as a raw double value.
     * 
     * @deprecated Use {@link #getPositionMeasure()} instead for type safety with units
     * @return the current position in rotations as a double
     */
    @Deprecated
    @Override
    public double position() {
        return cancoder.getPosition().getValueAsDouble();
    }

    /**
     * Gets the current position of the encoder using type-safe units.
     * 
     * <p>This method returns the position as an {@link Angle} measurement in rotations,
     * which can be converted to other angular units as needed.
     * 
     * @return the current position as an {@link Angle} measurement
     */
    @Override
    public Angle getPositionMeasure() {
        return Units.Rotations.of(cancoder.getPosition().getValueAsDouble());
    }

    /**
     * Sets the position of the encoder using a raw double value.
     * 
     * <p>This method uses {@link ConfigUtils#phoenix6Config(Runnable)} to ensure
     * reliable configuration of the Phoenix 6 device.
     * 
     * @deprecated Use {@link #setPosition(Angle)} instead for type safety with units
     * @param position the new position in rotations
     */
    @Deprecated
    @Override
    public void setPosition(double position) {
        ConfigUtils.phoenix6Config(() -> cancoder.setPosition(position));
    }

    /**
     * Sets the position of the encoder using type-safe units.
     * 
     * <p>This method accepts any {@link Angle} measurement and converts it to rotations
     * internally. It uses {@link ConfigUtils#phoenix6Config(Runnable)} to ensure
     * reliable configuration of the Phoenix 6 device.
     * 
     * @param position the new position as an {@link Angle} measurement
     */
    @Override
    public void setPosition(Angle position) {
        ConfigUtils.phoenix6Config(() -> cancoder.setPosition(position.in(Units.Rotations)));
    }

    /**
     * Provides direct access to the underlying CANcoder hardware object.
     * 
     * <p>This method allows access to Phoenix-specific configuration and methods
     * that are not exposed through the {@link Encoder} interface.
     * 
     * @return the underlying {@link CANcoder} hardware object
     */
    public CANcoder encoder() {
        return cancoder;
    }

    /**
     * Gets the current velocity of the encoder as a raw double value.
     * 
     * @deprecated Use {@link #getVelocityMeasure()} instead for type safety with units
     * @return the current velocity in rotations per second as a double
     */
    @Deprecated
    @Override
    public double getVelocity() {
        return cancoder.getVelocity().getValueAsDouble();
    }

    /**
     * Gets the current velocity of the encoder using type-safe units.
     * 
     * <p>This method returns the velocity as an {@link AngularVelocity} measurement,
     * which can be converted to other angular velocity units as needed.
     * 
     * @return the current velocity as an {@link AngularVelocity} measurement
     */
    @Override
    public AngularVelocity getVelocityMeasure() {
        return cancoder.getVelocity().getValue();
    }

    /**
     * Determines whether this CancoderWrapper is equal to another object.
     * 
     * <p>Two CancoderWrapper instances are considered equal if they represent
     * the same physical CANcoder device (same CAN ID and CAN bus).
     * 
     * @param obj the object to compare against
     * @return {@code true} if the objects represent the same CANcoder device,
     *         {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CancoderWrapper)) return false;
        CancoderWrapper other = (CancoderWrapper) obj;
        return info.equals(other.info);
    }
}