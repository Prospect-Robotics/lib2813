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
   * @since 1.3.0
   * @return The current applied current
   */
  Current getAppliedCurrent();
}
