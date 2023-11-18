package com.team2813.lib2813.control.motors;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ctre.phoenix.motorcontrol.TalonFXInvertType;
import com.ctre.phoenix.motorcontrol.can.TalonFX;

import edu.wpi.first.hal.HAL;

public class TalonFXEqualsTest {
	@Test
	public void TalonEquals() {
		TalonFX a = new TalonFXWrapper.TalonMotor(1);
		TalonFX b = new TalonFXWrapper.TalonMotor(1);
		assertTrue(a.equals(b));
	}
	@Test
	public void IdentityTest() {
		TalonFXWrapper motor = new TalonFXWrapper(0, TalonFXInvertType.Clockwise);
		assertTrue(motor.equals(motor));
	}
}
