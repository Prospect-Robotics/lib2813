package com.team2813.lib2813.control;

import com.revrobotics.CANSparkBase.ControlType;

public enum ControlMode {
	DUTY_CYCLE(ControlType.kDutyCycle),
	VELOCITY(ControlType.kVelocity),
	MOTION_MAGIC(ControlType.kPosition);

	private final ControlType sparkMode;

	ControlMode(ControlType sparkMode) {
		this.sparkMode = sparkMode;
	}

	public ControlType getSparkMode() {
		return sparkMode;
	}
}