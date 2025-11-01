/*
Copyright 2025 Prospect Robotics SWENext Club

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
