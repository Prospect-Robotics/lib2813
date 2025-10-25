package com.team2813.lib2813.control;

import com.team2813.lib2813.util.InputValidation;
import java.util.Optional;

/**
 * Immutable container for CAN device addressing information.
 *
 * <p>CAN devices can exist on either the RoboRIO's built-in CAN bus or on external CAN buses (e.g.,
 * CANivore). This class encapsulates both the device ID and optional bus name for proper device
 * identification.
 */
public final class DeviceInformation {
  private int id;
  private Optional<String> canbus;

  /**
   * Creates a DeviceInformation for a device on the RoboRIO can loop
   *
   * @param id the can ID
   */
  public DeviceInformation(int id) {
    this(id, null);
  }

  /**
   * Creates a DeviceInformation with a canbus string. If {@code canbus} is {@code null}, method
   * acts like {@link #DeviceInformation(int)} was called
   *
   * @param id the CAN id
   * @param canbus the canbus string
   */
  public DeviceInformation(int id, String canbus) {
    this.id = InputValidation.checkCanId(id);
    this.canbus = Optional.ofNullable(canbus);
  }

  /**
   * Gets the can id of this device
   *
   * @return the can id of the device
   */
  public int id() {
    return id;
  }

  /**
   * Returns the canbus that this device is on, or {@link Optional#empty()} if it is on the RoboRIO
   * can loop
   *
   * @return the canbus that the device is on
   */
  public Optional<String> canbus() {
    return canbus;
  }

  /**
   * Compares based on both ID and CAN bus.
   *
   * @param o object to compare
   * @return true if both ID and bus match
   */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof DeviceInformation)) return false;
    DeviceInformation other = (DeviceInformation) o;
    return other.id == id && other.canbus.equals(canbus);
  }

  /**
   * Hash combines ID and bus for proper hash-based collection behavior.
   *
   * @return combined hash of id and canbus
   */
  @Override
  public int hashCode() {
    return id * 31 + canbus.hashCode();
  }
}
