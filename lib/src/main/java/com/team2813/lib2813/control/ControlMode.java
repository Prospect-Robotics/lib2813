package com.team2813.lib2813.control;

public enum ControlMode {
  DUTY_CYCLE(false),
  VELOCITY(false),
  MOTION_MAGIC(true),
  VOLTAGE(false);

  private final boolean isPositionalControl;

  ControlMode(boolean isPositionalControl) {
    this.isPositionalControl = isPositionalControl;
  }

  public boolean isPositionalControl() {
    return isPositionalControl;
  }
}
