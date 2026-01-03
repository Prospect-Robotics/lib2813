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
package com.team2813.lib2813.control.encoders;

import com.ctre.phoenix6.hardware.CANcoder;
import com.team2813.lib2813.control.DeviceInformation;
import com.team2813.lib2813.control.Encoder;
import com.team2813.lib2813.util.ConfigUtils;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;

public class CancoderWrapper implements Encoder {
  private CANcoder cancoder;
  private DeviceInformation info;

  public CancoderWrapper(int id, String canbus) {
    cancoder = new CANcoder(id, canbus);
    info = new DeviceInformation(id, canbus);
  }

  public CancoderWrapper(int id) {
    cancoder = new CANcoder(id);
    info = new DeviceInformation(id);
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
    if (!(obj instanceof CancoderWrapper)) return false;
    CancoderWrapper other = (CancoderWrapper) obj;
    return info.equals(other.info);
  }
}
