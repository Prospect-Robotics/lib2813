/*
Copyright 2023-2026 Prospect Robotics SWENext Club

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.team2813.lib2813.control;

import com.ctre.phoenix6.CANBus;
import com.team2813.lib2813.util.InputValidation;
import java.util.Objects;
import java.util.Optional;

public final class DeviceInformation {
  private static final String DEFAULT_CANBUS_NAME = new CANBus().getName();
  private final int id;
  private final CANBus canbus;

  /**
   * Creates a DeviceInformation for a device on the default CAN bus.
   *
   * @param deviceId the configured ID of the device
   */
  public DeviceInformation(int deviceId) {
    this.id = InputValidation.checkCanId(deviceId);
    this.canbus = new CANBus();
  }

  /**
   * Creates a DeviceInformation with a canbus.
   *
   * @param deviceId the configured ID of the device
   * @param canbus the CAN bus the device is on
   */
  public DeviceInformation(int deviceId, CANBus canbus) {
    Objects.requireNonNull(canbus, "canbus");
    Objects.requireNonNull(canbus.getName(), "canbus name");
    this.id = InputValidation.checkCanId(deviceId);
    this.canbus = canbus;
  }

  /**
   * Creates a DeviceInformation with a canbus string. If {@code canbus} is {@code null}, method
   * acts like {@link #DeviceInformation(int)} was called
   *
   * @param id the CAN id
   * @param canbus the name of the CAN bus the device is on
   * @deprecated Constructing {@code DeviceInformation} with a CAN bus string is deprecated for
   *     removal in the 2027 season. Construct instances using a {@link CANBus} instance instead.
   */
  @Deprecated(forRemoval = true)
  public DeviceInformation(int id, String canbus) {
    this.id = InputValidation.checkCanId(id);
    this.canbus = canbus == null ? new CANBus() : new CANBus(canbus);
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
   * @deprecated use {@link #canBus()}}
   */
  @Deprecated(forRemoval = true)
  public Optional<String> canbus() {
    String canbusName = canbus.getName();
    if (canbusName.equals(DEFAULT_CANBUS_NAME)) {
      return Optional.empty();
    }
    return Optional.of(canbusName);
  }

  /**
   * Returns the canbus that this device is on.
   *
   * @return the canbus that the device is on
   */
  public CANBus canBus() {
    return canbus;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof DeviceInformation other) {
      return other.id == id && other.canbus.getName().equals(canbus.getName());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return id * 31 + canbus.getName().hashCode();
  }
}
