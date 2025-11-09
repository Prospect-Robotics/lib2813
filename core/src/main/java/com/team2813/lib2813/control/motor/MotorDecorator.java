/*
Copyright 2026 Prospect Robotics SWENext Club

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
package com.team2813.lib2813.control.motor;

import com.team2813.lib2813.control.ControlMode;
import com.team2813.lib2813.control.Motor;
import edu.wpi.first.units.measure.Current;

/**
 * Base class for classes that decorate motors.
 *
 * <p>All methods in this class delegate to the passed-in motor.
 */
public abstract class MotorDecorator<M extends Motor> implements Motor {
  protected final M motor;

  protected MotorDecorator(M motor) {
    this.motor = motor;
  }

  @Override
  public void set(ControlMode mode, double demand) {
    motor.set(mode, demand);
  }

  @Override
  public void set(ControlMode mode, double demand, double feedForward) {
    motor.set(mode, demand, feedForward);
  }

  @Override
  public Current getAppliedCurrent() {
    return motor.getAppliedCurrent();
  }

  @Override
  public void stopMotor() {
    motor.stopMotor();
  }
}
