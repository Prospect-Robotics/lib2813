package com.team2813.lib2813.limelight;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.OptionalLong;

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

		assertEquals(Limelight.DEFAULT_TABLE, a.getName());
		assertEquals("Default limelight call returned different values", a, b);
		Limelight c = Limelight.getLimelight(Limelight.DEFAULT_TABLE);
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
	public void apriltagTest() {
		Limelight limelight = Limelight.getDefaultLimelight();
		NetworkTable table = instance.getTable(limelight.getName());

		table.getEntry("tv").setInteger(0);
		instance.flushLocal();

		assertFalse("Value of 0 not false", limelight.hasTarget());
		assertFalse("Tag ID is Present", limelight.getLocationalData().getTagID().isPresent());

		table.getEntry("tid").setInteger(1);
		table.getEntry("tv").setInteger(1);
		instance.flushLocal();
		
		assertTrue("Value of 1 not true", limelight.hasTarget());
		OptionalLong tagId = limelight.getLocationalData().getTagID();
		assertTrue("Tag ID not present", tagId.isPresent());
		assertEquals("Tag ID not 1", tagId.getAsLong(), 1);
	}
}
