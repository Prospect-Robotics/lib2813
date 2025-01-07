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

  @Deprecated
  @Override
  public double position() {
    return cancoder.getPosition().getValueAsDouble();
  }

  @Override
  public Angle getPositionMeasure() {
    return Units.Rotations.of(cancoder.getPosition().getValueAsDouble());
  }

  @Deprecated
  @Override
  public void setPosition(double position) {
    ConfigUtils.phoenix6Config(() -> cancoder.setPosition(position));
  }

  @Override
  public void setPosition(Angle position) {
    ConfigUtils.phoenix6Config(() -> cancoder.setPosition(position.in(Units.Rotations)));
  }

  public CANcoder encoder() {
    return cancoder;
  }

  @Deprecated
  @Override
  public double getVelocity() {
    return cancoder.getVelocity().getValueAsDouble();
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
