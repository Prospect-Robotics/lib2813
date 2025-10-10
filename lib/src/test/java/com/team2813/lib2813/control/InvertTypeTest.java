package com.team2813.lib2813.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.ctre.phoenix6.signals.InvertedValue;
import org.junit.Test;

/** Unit tests for {@link InvertType}. */
public class InvertTypeTest {

  /**
   * Ensures that all {@link InvertType#rotationValues} have a corresponding Phoenix invert value.
   */
  @Test
  public void phoenixInvertsExist() {
    for (InvertType v : InvertType.rotationValues) {
      assertTrue(
          String.format("No phoenix invert exists for InvertType %s.", v),
          v.phoenixInvert().isPresent());
    }
  }

  /**
   * Ensures that all {@link InvertType#rotationValues} have a corresponding Spark MAX invert value.
   *
   * <p>Note: This test currently checks phoenixInvert(). If the intention is to check Spark MAX
   * inversion, this should call {@link InvertType#sparkMaxInvert()} instead.
   */
  @Test
  public void sparkMaxInvertsExist() {
    for (InvertType v : InvertType.rotationValues) {
      assertTrue(
          String.format("No spark max invert exists for InvertType %s.", v),
          v.sparkMaxInvert().isPresent());
    }
  }

  /**
   * Verifies that {@link InvertType#fromPhoenixInvert(InvertedValue)} correctly maps Phoenix invert
   * values back to the original {@link InvertType}.
   */
  @Test
  public void fromPhoenixInvertTest() {
    for (InvertType v : InvertType.rotationValues) {
      InvertedValue val = v.phoenixInvert().orElseThrow();
      assertEquals(v, InvertType.fromPhoenixInvert(val).orElse(null));
    }
  }

  /**
   * Verifies that {@link InvertType#fromSparkMaxInvert(boolean)} correctly maps Spark MAX invert
   * values back to the original {@link InvertType}.
   */
  @Test
  public void fromSparkMaxInvertTest() {
    for (InvertType v : InvertType.rotationValues) {
      boolean val = v.sparkMaxInvert().orElseThrow();
      assertEquals(v, InvertType.fromSparkMaxInvert(val).orElse(null));
    }
  }
}
