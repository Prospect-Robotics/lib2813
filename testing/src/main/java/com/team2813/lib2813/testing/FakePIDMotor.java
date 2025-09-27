package com.team2813.lib2813.testing;

import com.team2813.lib2813.control.PIDMotor;
import edu.wpi.first.units.Units;

public abstract class FakePIDMotor extends FakeMotor implements PIDMotor {

  @Deprecated
  public double getVoltage() {
    return getMotorVoltage().in(Units.Volts);
  }

  @Override
  public double getVelocity() {
    throw new AssertionError("Called deprecated method");
  }
}
