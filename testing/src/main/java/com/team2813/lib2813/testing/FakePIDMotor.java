package com.team2813.lib2813.testing;

import com.team2813.lib2813.control.PIDMotor;
import edu.wpi.first.units.Units;

/**
 * A fake implementation of {@link PIDMotor}; used for testing.
 *
 * <p>This class simulates motor behavior by storing the most recent control mode and demand value.
 * It also includes methods that make it easier to verify the current state of the motor.
 */
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
