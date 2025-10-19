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

import com.ctre.phoenix6.hardware.Pigeon2;
import com.team2813.lib2813.control.DeviceInformation;
import com.team2813.lib2813.util.ConfigUtils;

public class Pigeon2Wrapper {

  private Pigeon2 pigeon;

  private double currentHeading = 0;
  private DeviceInformation info;

  /**
   * Constructor
   *
   * @param deviceNumber [0,62]
   * @param canbus Name of the CANbus; can be a SocketCAN interface (on Linux), or a CANivore device
   *     name or serial number
   */
  public Pigeon2Wrapper(int deviceNumber, String canbus) {
    info = new DeviceInformation(deviceNumber, canbus);
    pigeon = new Pigeon2(deviceNumber, canbus);
  }

  /**
   * Constructor
   *
   * @param deviceNumber [0,62]
   */
  public Pigeon2Wrapper(int deviceNumber) {
    info = new DeviceInformation(deviceNumber);
    pigeon = new Pigeon2(deviceNumber);
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

  public boolean equals(Object other) {
    if (!(other instanceof Pigeon2Wrapper)) {
      return false;
    }
    Pigeon2Wrapper o = (Pigeon2Wrapper) other;
    return o.info.equals(info);
  }
}
