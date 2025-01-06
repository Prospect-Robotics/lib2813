package com.team2813.lib2813.control;

import com.revrobotics.spark.SparkBase.ControlType;

public enum ControlMode {
  DUTY_CYCLE(ControlType.kDutyCycle),
  VELOCITY(ControlType.kVelocity),
  MOTION_MAGIC(ControlType.kPosition),
  VOLTAGE(ControlType.kVoltage);

  private final ControlType sparkMode;

  ControlMode(ControlType sparkMode) {
    this.sparkMode = sparkMode;
  }

  public ControlType getSparkMode() {
    return sparkMode;
  }
}
