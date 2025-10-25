package com.team2813.lib2813.util;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * Unit tests for {@link ControlUtils}.
 *
 * <p>Specifically tests the {@link ControlUtils#deadband(double, double)} method, which applies a
 * deadband to a joystick input or control value. A deadband zeroes out values within a threshold
 * and scales values outside the threshold appropriately.
 */
public class ControlUtilsTest {

  /**
   * Tests that input values within the deadband are zeroed.
   *
   * <p>For example, if the deadband is 0.5, any input between -0.5 and 0.5 should be treated as 0.
   */
  @Test
  public void deadbandValuesWithinDeadbandAreZeroed() {
    assertThat(ControlUtils.deadband(-0.5, 0.5)).isEqualTo(0.0);
    assertThat(ControlUtils.deadband(-0.25, 0.5)).isEqualTo(0.0);
    assertThat(ControlUtils.deadband(0.0, 0.5)).isEqualTo(0.0);
    assertThat(ControlUtils.deadband(0.5, 0.5)).isEqualTo(0.0);
  }

  /**
   * Tests that input values outside the deadband are scaled/adjusted correctly.
   *
   * <p>The method should maintain the sign of the input and reduce it proportionally to account for
   * the deadband offset.
   */
  @Test
  public void deadbandValuesOutsideDeadbandAreAdjusted() {
    assertThat(ControlUtils.deadband(-1.0, 0.5)).isWithin(1e-9).of(-1.0);
    assertThat(ControlUtils.deadband(-0.75, 0.5)).isWithin(1e-9).of(-0.5);
    assertThat(ControlUtils.deadband(0.6, 0.5)).isWithin(1e-9).of(0.2);
    assertThat(ControlUtils.deadband(0.75, 0.5)).isWithin(1e-9).of(0.5);
    assertThat(ControlUtils.deadband(1.0, 0.5)).isWithin(1e-9).of(1.0);
  }

  /**
   * Tests that a zero deadband leaves values unchanged.
   *
   * <p>When deadband = 0.0, all input values are passed through as-is.
   */
  @Test
  public void deadbandZeroDeadbandHasNoEffect() {
    assertThat(ControlUtils.deadband(-1.0, 0.0)).isEqualTo(-1.0);
    assertThat(ControlUtils.deadband(-0.5, 0.0)).isEqualTo(-0.5);
    assertThat(ControlUtils.deadband(-0.25, 0.0)).isEqualTo(-0.25);
    assertThat(ControlUtils.deadband(0.0, 0.0)).isEqualTo(0.0);
    assertThat(ControlUtils.deadband(0.5, 0.0)).isEqualTo(0.5);
    assertThat(ControlUtils.deadband(1.0, 0.0)).isEqualTo(1.0);
  }

  /**
   * Helper method to assert that an {@link IllegalArgumentException} is thrown with a message
   * containing a specific substring.
   *
   * @param testExpression The executable expression expected to throw an exception.
   * @param expectedMessage Part of the message expected in the exception.
   */
  private void assertIllegalArgumentExceptionIsThrownContainingMessage(
      Executable testExpression, String expectedMessage) {
    Exception exception = assertThrows(IllegalArgumentException.class, testExpression);
    assertThat(exception).hasMessageThat().contains(expectedMessage);
  }

  /**
   * Tests that invalid deadband values (< 0 or ≥ 1) throw an exception.
   *
   * <p>Deadband values must be in the range [0, 1) for valid control scaling.
   */
  @Test
  public void deadbandThrowsErrorOnInvalidDeadband() {
    assertIllegalArgumentExceptionIsThrownContainingMessage(
        () -> ControlUtils.deadband(0.25, -1.5), "Instead, it was -1.5");
    assertIllegalArgumentExceptionIsThrownContainingMessage(
        () -> ControlUtils.deadband(0.25, -1.0), "Instead, it was -1.0");
    assertIllegalArgumentExceptionIsThrownContainingMessage(
        () -> ControlUtils.deadband(0.25, 1.0), "Instead, it was 1.0");
    assertIllegalArgumentExceptionIsThrownContainingMessage(
        () -> ControlUtils.deadband(0.25, 1.9), "Instead, it was 1.9");
  }

  /**
   * Tests that input values outside the valid range [-1, 1] throw an exception.
   *
   * <p>Control inputs must remain in [-1, 1]; any value outside this range is invalid.
   */
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
