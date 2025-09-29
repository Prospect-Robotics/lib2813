package com.team2813.lib2813.control.imu;

import com.ctre.phoenix6.hardware.Pigeon2;
import com.team2813.lib2813.control.DeviceInformation;
import com.team2813.lib2813.util.ConfigUtils;

/**
 * Wrapper class for a CTRE Phoenix 6 Pigeon2 IMU (Inertial Measurement Unit).
 * 
 * <p>This class provides a standardized interface for interacting with Pigeon2 devices
 * while maintaining compatibility with the team's control system architecture. It includes
 * automatic reset detection and recovery functionality to handle power cycles and device
 * resets that can occur during competition.
 * 
 * <p>The wrapper maintains a cached heading value that is restored after device resets,
 * ensuring consistent heading readings across power cycles. The {@link #periodicResetCheck()}
 * method should be called periodically (typically in a subsystem's periodic method) to
 * monitor for and handle resets.
 * 
 * @author Team 2813
 * @since 1.0
 */
public class Pigeon2Wrapper {
    
    /** The underlying CTRE Pigeon2 hardware object */
    private Pigeon2 pigeon;
    
    /** Cached heading value used for reset recovery */
    private double currentHeading = 0;
    
    /** Device information containing ID and CAN bus details */
    private DeviceInformation info;

    /**
     * Creates a new Pigeon2Wrapper with the specified device number and CAN bus.
     * 
     * @param deviceNumber the CAN ID of the Pigeon2 device, must be in range [0,62]
     * @param canbus the name of the CAN bus the device is connected to. This can be a 
     *               SocketCAN interface (on Linux), a CANivore device name, or serial number
     */
    public Pigeon2Wrapper(int deviceNumber, String canbus) {
        info = new DeviceInformation(deviceNumber, canbus);
        pigeon = new Pigeon2(deviceNumber, canbus);
    }

    /**
     * Creates a new Pigeon2Wrapper with the specified device number on the default CAN bus.
     * 
     * @param deviceNumber the CAN ID of the Pigeon2 device, must be in range [0,62]
     */
    public Pigeon2Wrapper(int deviceNumber) {
        info = new DeviceInformation(deviceNumber);
        pigeon = new Pigeon2(deviceNumber);
    }

    /**
     * Provides direct access to the underlying Pigeon2 hardware object.
     * 
     * <p>This method allows access to Phoenix-specific configuration and methods
     * that are not exposed through this wrapper class.
     * 
     * @return the underlying {@link Pigeon2} hardware object
     */
    public Pigeon2 getPigeon() {
        return pigeon;
    }

    /**
     * Gets the current heading (yaw angle) of the IMU.
     * 
     * <p>This method returns the raw yaw value from the Pigeon2. The heading
     * is typically measured in degrees, with the range and direction depending
     * on the device configuration.
     * 
     * @return the current heading in degrees
     */
    public double getHeading() {
        return pigeon.getYaw().getValueAsDouble();
    }

    /**
     * Sets the current heading (yaw angle) of the IMU.
     * 
     * <p>This method updates both the hardware device and the cached heading value
     * used for reset recovery. It uses {@link ConfigUtils#phoenix6Config(Runnable)}
     * to ensure reliable configuration of the Phoenix 6 device.
     * 
     * @param angle the new heading angle in degrees
     */
    public void setHeading(double angle) {
        ConfigUtils.phoenix6Config(() -> pigeon.setYaw(angle));
        currentHeading = angle;
    }

    /**
     * Checks if a device reset has occurred and restores non-persistent settings if needed.
     * 
     * <p>This method should be called periodically (e.g., in a subsystem's periodic() method)
     * to monitor for device resets that can occur due to power cycles, brownouts, or other
     * electrical issues. When a reset is detected, the method automatically restores the
     * cached heading value to maintain consistency.
     * 
     * <p>If no reset has occurred, the method updates the cached heading with the current
     * hardware reading to ensure the cache stays synchronized.
     * 
     * <p><b>Implementation Note:</b> This method should be called at least once per robot
     * loop iteration for proper reset detection and recovery.
     */
    public void periodicResetCheck() {
        if (!pigeon.hasResetOccurred()) {
            currentHeading = getHeading();
        } else {
            setHeading(currentHeading);
        }
    }

    /**
     * Returns a hash code value for this Pigeon2Wrapper.
     * 
     * <p>The hash code is based on the device information (CAN ID and bus),
     * ensuring that wrappers for the same physical device have the same hash code.
     * 
     * @return a hash code value for this object
     */
    public int hashCode() {
        return info.hashCode();
    }

    /**
     * Determines whether this Pigeon2Wrapper is equal to another object.
     * 
     * <p>Two Pigeon2Wrapper instances are considered equal if they represent
     * the same physical Pigeon2 device (same CAN ID and CAN bus).
     * 
     * @param other the object to compare against
     * @return {@code true} if the objects represent the same Pigeon2 device,
     *         {@code false} otherwise
     */
    public boolean equals(Object other) {
        if (!(other instanceof Pigeon2Wrapper)) {
            return false;
        }
        Pigeon2Wrapper o = (Pigeon2Wrapper) other;
        return o.info.equals(info);
    }
}