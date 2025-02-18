package com.team2813.lib2813.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class ControlUtilsTest {
    @Test
    public void deadbandValuesWithinDeadbandAreZeroed() {
        // Keep test values in ascending order.
        assertEquals(0.0, ControlUtils.deadband(-0.5, 0.5), 1e-9);
        assertEquals(0.0, ControlUtils.deadband(-0.25, 0.5), 1e-9);
        assertEquals(0.0, ControlUtils.deadband(0.0, 0.5), 1e-9);
        assertEquals(0.0, ControlUtils.deadband(0.5, 0.5), 1e-9);
    }

    @Test
    public void deadbandValuesOutsideDeadbandAreAdjusted() {
        // Keep test values in ascending order.
        assertEquals(-1.0, ControlUtils.deadband(-1.0, 0.5), 1e-9);
        assertEquals(-0.5, ControlUtils.deadband(-0.75, 0.5), 1e-9);
        assertEquals(0.2, ControlUtils.deadband(0.6, 0.5), 1e-9);
        assertEquals(0.5, ControlUtils.deadband(0.75, 0.5), 1e-9);
        assertEquals(1.0, ControlUtils.deadband(1.0, 0.5), 1e-9);
    }

    @Test
    public void deadbandZeroDeadbandHasNoEffect() {
        // Keep test values in ascending order.
        assertEquals(-1.0, ControlUtils.deadband(-1.0, 0.0), 1e-9);
        assertEquals(-0.5, ControlUtils.deadband(-0.5, 0.0), 1e-9);
        assertEquals(-0.25, ControlUtils.deadband(-0.25, 0.0), 1e-9);
        assertEquals(0.0, ControlUtils.deadband(0.0, 0.0), 1e-9);
        assertEquals(0.5, ControlUtils.deadband(0.5, 0.0), 1e-9);
        assertEquals(1.0, ControlUtils.deadband(1.0, 0.0), 1e-9);
    }

    @Test
    public void deadbandThrowsErrorOnInvalidDeadband() {
        // Deadband values outside [0.0, 1.0) result in IllegalArgumentException.
        assertThrows(IllegalArgumentException.class, () -> ControlUtils.deadband(0.25, -1.5));
        assertThrows(IllegalArgumentException.class, () -> ControlUtils.deadband(0.25, -1.0));
        assertThrows(IllegalArgumentException.class, () -> ControlUtils.deadband(0.25, 1.0));
        assertThrows(IllegalArgumentException.class, () -> ControlUtils.deadband(0.25, 1.9));
    }

    @Test
    public void deadbandThrowsErrorOnValueOutOfRange() {
        // Values outside [-1.0, 1.0] result in IllegalArgumentException.
        assertThrows(IllegalArgumentException.class, () -> ControlUtils.deadband(-1.5, 0.5));
        assertThrows(IllegalArgumentException.class, () -> ControlUtils.deadband(-1.0001, 0.5));
        assertThrows(IllegalArgumentException.class, () -> ControlUtils.deadband(1.0001, 0.5));
        assertThrows(IllegalArgumentException.class, () -> ControlUtils.deadband(30.0, 0.5));
    }
}
