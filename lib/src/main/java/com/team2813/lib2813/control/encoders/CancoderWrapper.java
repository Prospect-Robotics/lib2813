/*
Copyright 2024-2026 Prospect Robotics SWENext Club

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
package com.team2813.lib2813.control.encoders;

import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.hardware.CANcoder;
import com.team2813.lib2813.control.DeviceInformation;
import com.team2813.lib2813.control.Encoder;
import com.team2813.lib2813.util.ConfigUtils;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;

/**
 * A wrapper for {@link CANcoder} that implements {@link Encoder}.
 *
 * <p>Represents a CAN-based magnetic encoder that provides absolute and relative position along
 * with filtered velocity.
 */
public class CancoderWrapper implements Encoder {
  private final CANcoder cancoder;
  private final DeviceInformation info;

  /**
   * Creates an instance for a CANcoder on the specified CAN bus.
   *
   * @param deviceId the configured ID of the CANcoder
   * @param canbus the CAN bus the device is on
   */
  public CancoderWrapper(int deviceId, CANBus canbus) {
    info = new DeviceInformation(deviceId, canbus);
    cancoder = new CANcoder(deviceId, canbus);
  }

  /**
   * Creates an instance for a CANcoder on the specified CAN bus name.
   *
   * @param deviceId the configured ID of the CANcoder
   * @param canbusName the name of the CAN bus the device is on
   * @deprecated Constructing {@code CancoderWrapper} with a CAN bus string is deprecated for
   *     removal in the 2027 season. Construct instances using a {@link CANBus} instance instead.
   */
  @Deprecated(forRemoval = true)
  public CancoderWrapper(int deviceId, String canbusName) {
    @SuppressWarnings("removal")
    DeviceInformation deviceInformation = new DeviceInformation(deviceId, canbusName);
    info = deviceInformation;
    cancoder = new CANcoder(deviceId, info.canBus());
  }

  /**
   * Creates an instance for a CANcoder on the default CAN bus.
   *
   * @param deviceId the configured ID of the CANcoder
   */
  public CancoderWrapper(int deviceId) {
    cancoder = new CANcoder(deviceId);
    info = new DeviceInformation(deviceId, cancoder.getNetwork());
  }

  @Override
  public Angle getPositionMeasure() {
    return Units.Rotations.of(cancoder.getPosition().getValueAsDouble());
  }

  @Override
  public void setPosition(Angle position) {
    ConfigUtils.phoenix6Config(() -> cancoder.setPosition(position.in(Units.Rotations)));
  }

  public CANcoder encoder() {
    return cancoder;
  }

  @Override
  public AngularVelocity getVelocityMeasure() {
    return cancoder.getVelocity().getValue();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof CancoderWrapper other) {
      return info.equals(other.info);
    }
    return false;
  }
}
