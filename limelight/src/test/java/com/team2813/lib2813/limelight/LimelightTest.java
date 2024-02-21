package com.team2813.lib2813.limelight;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

public class LimelightTest {
	NetworkTableInstance instance;
	@Before
	public void setup() {
		instance = NetworkTableInstance.create();
		instance.startLocal();
		Limelight.setTableInstance(instance);
		instance.flushLocal();
	}

	@After
	public void cleanup() {
		instance.close();
		Limelight.eraseInstances();
	}

	@Test
	public void equality() {
		Limelight a = Limelight.getDefaultLimelight();
		Limelight b = Limelight.getDefaultLimelight();
		assertEquals("Default limelight call returned different values", a, b);
		Limelight c = Limelight.getLimelight("limelight");
		assertEquals(
			"Default limelights not equal to limelights named \"limelight\" (default)",
			a, c
		);
	}

	@Test
	public void emptyValues() {
		Limelight limelight = Limelight.getDefaultLimelight();
		assertFalse("NetworkTables should be empty", limelight.getPipeline().isPresent());
	}

	@Test
	public void changingTables() {
		Limelight limelight = Limelight.getDefaultLimelight();
		NetworkTable table = instance.getTable("limelight");
		table.getEntry("tv").setInteger(0);
		instance.flushLocal();
		assertFalse("Value of 0 not false", limelight.hasTarget());
		table.getEntry("tv").setInteger(1);
		instance.flushLocal();
		assertTrue("Value of 1 not true", limelight.hasTarget());
	}
}
