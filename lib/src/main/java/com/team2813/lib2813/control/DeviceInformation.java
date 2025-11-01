/*
Copyright 2023-2025 Prospect Robotics SWENext Club

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

import com.team2813.lib2813.util.InputValidation;
import java.util.Optional;

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

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof DeviceInformation)) return false;
    DeviceInformation other = (DeviceInformation) o;
    return other.id == id && other.canbus.equals(canbus);
  }

  @Override
  public int hashCode() {
    return id * 31 + canbus.hashCode();
  }
}
