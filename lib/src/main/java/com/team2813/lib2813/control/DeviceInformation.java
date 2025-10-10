package com.team2813.lib2813.control;

import com.team2813.lib2813.util.InputValidation;
import java.util.Optional;

/**
 * Immutable value class representing the identifying information for a CAN device.
 *
 * <p>This class encapsulates the essential information needed to uniquely identify a device on the
 * CAN bus network: the CAN ID and the specific CAN bus the device is connected to. It provides a
 * standardized way to represent device identity across the team's control system architecture.
 *
 * <p>The class distinguishes between devices on the RoboRIO's built-in CAN bus (represented by an
 * empty Optional for the canbus) and devices on named CAN buses such as CANivore or other CAN bus
 * interfaces.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Immutable design for thread safety and reliable identity
 *   <li>Input validation for CAN ID range [0, 62]
 * </ul>
 *
 * <p>This class is commonly used as a key in device registries and for comparing device instances
 * to determine if they represent the same physical hardware.
 *
 * @author Team 2813
 * @since 1.0
 */
public final class DeviceInformation {

  /** The CAN ID of the device, validated to be in range [0, 62] */
  private int id;

  /** The CAN bus name, empty if on the RoboRIO's default CAN bus */
  private Optional<String> canbus;

  /**
   * Creates DeviceInformation for a device on the RoboRIO's default CAN bus.
   *
   * <p>This constructor is used for devices connected directly to the RoboRIO's built-in CAN bus
   * interface. The canbus will be represented as an empty Optional to indicate the default bus.
   *
   * @param id the CAN ID of the device, must be in range [0, 62]
   * @throws com.team2813.lib2813.util.InvalidCanIdException if the CAN ID is outside the valid
   *     range
   */
  public DeviceInformation(int id) {
    this(id, null);
  }

  /**
   * Creates DeviceInformation with a specific CAN bus name.
   *
   * <p>This constructor supports devices on named CAN buses such as CANivore devices or other CAN
   * bus interfaces. If {@code canbus} is {@code null}, this method behaves identically to {@link
   * #DeviceInformation(int)}.
   *
   * @param id the CAN ID of the device, must be in range [0, 62]
   * @param canbus the CAN bus name, or {@code null} for the RoboRIO default bus
   * @throws com.team2813.lib2813.util.InvalidCanIdException if the CAN ID is outside the valid
   *     range
   */
  public DeviceInformation(int id, String canbus) {
    this.id = InputValidation.checkCanId(id);
    this.canbus = Optional.ofNullable(canbus);
  }

  /**
   * Gets the CAN ID of this device.
   *
   * <p>The CAN ID is guaranteed to be in the valid range [0, 62] due to validation performed during
   * construction.
   *
   * @return the CAN ID of the device
   */
  public int id() {
    return id;
  }

  /**
   * Returns the CAN bus that this device is connected to.
   *
   * <p>The return value interpretation:
   *
   * <ul>
   *   <li>{@link Optional#empty()} - Device is on the RoboRIO's default CAN bus
   *   <li>{@code Optional.of("busname")} - Device is on the named CAN bus
   * </ul>
   *
   * @return an Optional containing the CAN bus name, or empty if on the RoboRIO CAN bus
   */
  public Optional<String> canbus() {
    return canbus;
  }

  /**
   * Determines whether this DeviceInformation is equal to another object.
   *
   * <p>Two DeviceInformation instances are considered equal if and only if they have the same CAN
   * ID and are on the same CAN bus. This allows DeviceInformation objects to be used reliably as
   * keys in hash-based collections and for device identity comparisons.
   *
   * @param o the object to compare against
   * @return {@code true} if the objects represent the same device, {@code false} otherwise
   */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof DeviceInformation)) return false;
    DeviceInformation other = (DeviceInformation) o;
    return other.id == id && other.canbus.equals(canbus);
  }

  /**
   * Returns a hash code value for this DeviceInformation.
   *
   * @return a hash code value for this object
   */
  @Override
  public int hashCode() {
    return id * 31 + canbus.hashCode();
  }
}
