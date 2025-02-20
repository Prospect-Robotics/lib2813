package com.team2813.lib2813.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

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

  /**
   * Asserts that {@code testExpression} is an expression that throws an exception of type {@code
   * IllegalArgumentException} with a message containing {@code expectedMessage}.
   *
   * @param testExpression An expression that is expected to throw an exception when executed.
   * @param expectedMessage (Part of an) error message expected in the exception.
   */
  private void assertIllegalArgumentExceptionIsThrownContainingMessage(
      ThrowingRunnable testExpression, String expectedMessage) {
    Exception exception = assertThrows(IllegalArgumentException.class, testExpression);
    assertThat(exception.getMessage(), CoreMatchers.containsString(expectedMessage));
  }

  @Test
  public void deadbandThrowsErrorOnInvalidDeadband() {
    // Deadband values outside [0.0, 1.0) result in IllegalArgumentException.
    assertIllegalArgumentExceptionIsThrownContainingMessage(
        () -> ControlUtils.deadband(0.25, -1.5), "Instead, it was -1.5");
    assertIllegalArgumentExceptionIsThrownContainingMessage(
        () -> ControlUtils.deadband(0.25, -1.0), "Instead, it was -1.0");
    assertIllegalArgumentExceptionIsThrownContainingMessage(
        () -> ControlUtils.deadband(0.25, 1.0), "Instead, it was 1.0");
    assertIllegalArgumentExceptionIsThrownContainingMessage(
        () -> ControlUtils.deadband(0.25, 1.9), "Instead, it was 1.9");
  }

  @Test
  public void deadbandThrowsErrorOnValueOutOfRange() {
    assertIllegalArgumentExceptionIsThrownContainingMessage(
        () -> ControlUtils.deadband(-1.5, 0.5), "Instead, it was -1.5");
    assertIllegalArgumentExceptionIsThrownContainingMessage(
        () -> ControlUtils.deadband(-1.0001, 0.5), "Instead, it was -1.0001");
    assertIllegalArgumentExceptionIsThrownContainingMessage(
        () -> ControlUtils.deadband(1.0001, 0.5), "Instead, it was 1.0001");
    assertIllegalArgumentExceptionIsThrownContainingMessage(
        () -> ControlUtils.deadband(30.0, 0.5), "Instead, it was 30.0");
  }
}
