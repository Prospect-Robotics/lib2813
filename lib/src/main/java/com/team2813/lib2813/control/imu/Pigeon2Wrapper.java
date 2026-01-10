/*
Copyright 2024-2025 Prospect Robotics SWENext Club

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
package com.team2813.lib2813.control.imu;

import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.hardware.Pigeon2;
import com.team2813.lib2813.control.DeviceInformation;
import com.team2813.lib2813.util.ConfigUtils;

public class Pigeon2Wrapper {
  private final Pigeon2 pigeon;
  private final DeviceInformation info;

  private double currentHeading = 0;

  /**
   * Creates an instance for a Pigeon 2 IMU sensor on the specified CAN bus.
   *
   * @param deviceId [0,62]
   * @param canbus Name of the CANbus; can be a SocketCAN interface (on Linux), or a CANivore device
   *     name or serial number
   */
  public Pigeon2Wrapper(int deviceId, CANBus canbus) {
    info = new DeviceInformation(deviceId, canbus);
    pigeon = new Pigeon2(deviceId, canbus);
  }

  /**
   * Constructor
   *
   * @param deviceId [0,62]
   * @param canbusName the name of the CAN bus the device is on; can be a SocketCAN interface (on
   *     Linux), or a CANivore device name or serial number
   * @deprecated Constructing {@code CancoderWrapper} with a CAN bus string is deprecated for
   *     removal in the 2027 season. Construct instances using a {@link CANBus} instance instead.
   */
  @Deprecated(forRemoval = true)
  public Pigeon2Wrapper(int deviceId, String canbusName) {
    @SuppressWarnings("removal")
    DeviceInformation deviceInformation = new DeviceInformation(deviceId, canbusName);
    info = deviceInformation;
    pigeon = new Pigeon2(deviceId, info.canBus());
  }

  /**
   * Constructor
   *
   * @param deviceId [0,62]
   */
  public Pigeon2Wrapper(int deviceId) {
    pigeon = new Pigeon2(deviceId);
    info = new DeviceInformation(deviceId, pigeon.getNetwork());
  }

  public Pigeon2 getPigeon() {
    return pigeon;
  }

  public double getHeading() {
    return pigeon.getYaw().getValueAsDouble();
  }

  public void setHeading(double angle) {
    ConfigUtils.phoenix6Config(() -> pigeon.setYaw(angle));
    currentHeading = angle;
  }

  /**
   * Checks if a reset has occurred and restores non-persistent settings if so. Implement
   * periodically (e.g. in a subsystem's periodic() method)
   */
  public void periodicResetCheck() {
    if (!pigeon.hasResetOccurred()) {
      currentHeading = getHeading();
    } else {
      setHeading(currentHeading);
    }
  }

  public int hashCode() {
    return info.hashCode();
  }

  public boolean equals(Object obj) {
    if (obj instanceof Pigeon2Wrapper other) {
      return info.equals(other.info);
    }
    return false;
  }
}
