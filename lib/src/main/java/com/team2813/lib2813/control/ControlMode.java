package com.team2813.lib2813.control;

/**
 * Defines the control algorithms available for motor controllers.
 *
 * <p>The positional control flag is used to determine if a control mode requires position feedback
 * for proper operation. This distinction is important for:
 *
 * <ul>
 *   <li>Validating that position sensors are configured before use
 *   <li>Determining if setpoint wrapping/limits should apply
 *   <li>Selecting appropriate feed-forward models
 * </ul>
 */
public enum ControlMode {
  /** Open-loop output as percentage of max [-1.0, 1.0] */
  DUTY_CYCLE(false),

  /** Closed-loop velocity control (requires velocity feedback) */
  VELOCITY(false),

  /** Trapezoidal motion profile with velocity/acceleration limits (requires position feedback) */
  MOTION_MAGIC(true),

  /** Open-loop voltage output [-12V, 12V] */
  VOLTAGE(false);

  private final boolean isPositionalControl;

  /**
   * @param isPositionalControl true if this mode requires position feedback
   */
  ControlMode(boolean isPositionalControl) {
    this.isPositionalControl = isPositionalControl;
  }

  /**
   * @return true if this control mode requires position sensing
   */
  public boolean isPositionalControl() {
    return isPositionalControl;
  }
}
