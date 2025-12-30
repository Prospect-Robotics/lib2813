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

import edu.wpi.first.units.measure.Current;

public interface Motor {
  // motor control

  /**
   * Sets the motor to run with a specified mode of control.
   *
   * @param mode The mode to control the motor with
   * @param demand The demand of the motor. differentiating meaning with each control mode
   */
  void set(ControlMode mode, double demand);

  /**
   * Sets the motor to run with a specified mode of control, and feedForward.
   *
   * @param mode The mode to control the motor with
   * @param demand The demand of the motor. differentiating meaning with each control mode
   * @param feedForward The feedForward to apply to the motor
   */
  void set(ControlMode mode, double demand, double feedForward);

  /**
   * Gets the current that is being applied onto the motor
   *
   * @since 2.0.0
   * @return The current applied current
   */
  Current getAppliedCurrent();

  /**
   * Stops the motor.
   *
   * @deprecated Use {@link #stopMotor()}.
   */
  @Deprecated
  default void disable() {
    stopMotor();
  }

  /**
   * Stops the motor.
   *
   * @since 2.0.0
   */
  default void stopMotor() {
    set(ControlMode.VOLTAGE, 0);
  }
}
