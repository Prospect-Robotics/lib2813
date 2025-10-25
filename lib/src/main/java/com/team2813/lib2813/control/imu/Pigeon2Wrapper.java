package com.team2813.lib2813.control.imu;

import com.ctre.phoenix6.hardware.Pigeon2;
import com.team2813.lib2813.control.DeviceInformation;
import com.team2813.lib2813.util.ConfigUtils;

/**
 * A wrapper class for CTRE's Pigeon2 Inertial Measurement Unit (IMU).
 *
 * <p>This wrapper simplifies the use of the Pigeon2 by providing easy access to common functions
 * and handling reset detection automatically.
 */
public class Pigeon2Wrapper {

  // The actual Pigeon2 IMU device object from CTRE's Phoenix 6 library
  private Pigeon2 pigeon;

  // Keeps track of the last known heading to restore after device resets
  private double currentHeading = 0;

  // Stores information about the device's ID and CAN bus
  private DeviceInformation info;

  /**
   * Creates a new Pigeon2 wrapper with a specified device number and CAN bus.
   *
   * @param deviceNumber The device ID of the Pigeon2 on the CAN bus (valid range: 0-62)
   * @param canbus Name of the CANbus; can be a SocketCAN interface (on Linux), or a CANivore device
   *     name or serial number. Used when multiple CAN buses are present on the robot.
   */
  public Pigeon2Wrapper(int deviceNumber, String canbus) {
    info = new DeviceInformation(deviceNumber, canbus);
    pigeon = new Pigeon2(deviceNumber, canbus);
  }

  /**
   * Creates a new Pigeon2 wrapper using just a device number. Uses the default CAN bus on the
   * robot.
   *
   * @param deviceNumber The device ID of the Pigeon2 on the CAN bus (valid range: 0-62)
   */
  public Pigeon2Wrapper(int deviceNumber) {
    info = new DeviceInformation(deviceNumber);
    pigeon = new Pigeon2(deviceNumber);
  }

  /**
   * Gets direct access to the underlying Pigeon2 object. Use this if you need access to advanced
   * features not exposed by this wrapper.
   *
   * @return The underlying Pigeon2 IMU object
   */
  public Pigeon2 getPigeon() {
    return pigeon;
  }

  /**
   * Gets the current heading (yaw) of the robot in degrees. This represents the direction the robot
   * is facing: - 0 degrees is typically the robot's starting direction - Positive values indicate
   * clockwise rotation - Negative values indicate counterclockwise rotation - Range is unlimited
   * (can go beyond -360 to +360)
   *
   * @return The current heading in degrees
   */
  public double getHeading() {
    return pigeon.getYaw().getValueAsDouble();
  }

  /**
   * Sets the current heading of the robot to a specified angle. Useful for: - Zeroing the heading
   * (setting it to 0 degrees) - Calibrating the heading to match a known direction - Resetting the
   * heading after a device reset
   *
   * @param angle The angle in degrees to set as the current heading
   */
  public void setHeading(double angle) {
    ConfigUtils.phoenix6Config(() -> pigeon.setYaw(angle));
    currentHeading = angle;
  }

  /**
   * Checks if the Pigeon2 has reset and restores its previous heading if needed. The Pigeon2 may
   * reset due to power cycles or other issues, which resets its heading to 0. This method detects
   * such resets and restores the last known heading automatically.
   *
   * <p>Call this method periodically (e.g., in a subsystem's periodic() method) to ensure the
   * heading stays consistent even after device resets.
   */
  public void periodicResetCheck() {
    if (!pigeon.hasResetOccurred()) {
      currentHeading = getHeading();
    } else {
      setHeading(currentHeading);
    }
  }

  /**
   * Generates a hash code for this Pigeon2Wrapper instance. Used for storing the wrapper in
   * hash-based collections.
   *
   * @return A hash code based on the device information
   */
  public int hashCode() {
    return info.hashCode();
  }

  /**
   * Compares this Pigeon2Wrapper with another object for equality. Two Pigeon2Wrappers are
   * considered equal if they have the same device information.
   *
   * @param other The object to compare with
   * @return true if the objects are equal, false otherwise
   */
  public boolean equals(Object other) {
    if (!(other instanceof Pigeon2Wrapper)) {
      return false;
    }
    Pigeon2Wrapper o = (Pigeon2Wrapper) other;
    return o.info.equals(info);
  }
}
