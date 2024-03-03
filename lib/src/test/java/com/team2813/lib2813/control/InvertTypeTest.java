package com.team2813.lib2813.control;

import org.junit.Test;

import com.ctre.phoenix6.signals.InvertedValue;

import static org.junit.Assert.*;

public class InvertTypeTest {
	@Test
	public void phoenixInvertsExist() {
		for (InvertType v : InvertType.rotationValues) {
			assertTrue(
				String.format("No phoenix invert eixsts for InvertType %s.", v),
				v.phoenixInvert().isPresent()
			);
		}
	}

	@Test
	public void sparkMaxInvertsExist() {
		for (InvertType v : InvertType.rotationValues) {
			assertTrue(
				String.format("No spark max invert exists for InvertType %s.", v),
				v.phoenixInvert().isPresent()
			);
		}
	}

	@Test
	public void fromPhoenixInvertTest() {
		for (InvertType v : InvertType.rotationValues) {
			InvertedValue val = v.phoenixInvert().get();
			assertEquals(v, InvertType.fromPhoenixInvert(val).orElse(null));
		}
	}

	@Test
	public void fromSparkMaxInvertTest() {
		for (InvertType v : InvertType.rotationValues) {
			boolean val = v.sparkMaxInvert().get();
			assertEquals(v, InvertType.fromSparkMaxInvert(val).orElse(null));
		}
	}
}
