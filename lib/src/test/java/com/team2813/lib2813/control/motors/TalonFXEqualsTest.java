package com.team2813.lib2813.control.motors;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.team2813.lib2813.control.InvertType;

public class TalonFXEqualsTest {
	@Test
	public void IdentityTest() {
		TalonFXWrapper motor = new TalonFXWrapper(0, InvertType.CLOCKWISE);
		assertTrue(motor.equals(motor));
	}
}
