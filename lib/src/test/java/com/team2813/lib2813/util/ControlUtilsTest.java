/*
Copyright 2025 Prospect Robotics SWENext Club

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.team2813.lib2813.util;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class ControlUtilsTest {
  @Test
  public void deadbandValuesWithinDeadbandAreZeroed() {
    // Keep test values in ascending order.
    assertThat(ControlUtils.deadband(-0.5, 0.5)).isEqualTo(0.0);
    assertThat(ControlUtils.deadband(-0.5, 0.5)).isEqualTo(0.0);
    assertThat(ControlUtils.deadband(-0.25, 0.5)).isEqualTo(0.0);
    assertThat(ControlUtils.deadband(0.0, 0.5)).isEqualTo(0.0);
    assertThat(ControlUtils.deadband(0.5, 0.5)).isEqualTo(0.0);
  }

  @Test
  public void deadbandValuesOutsideDeadbandAreAdjusted() {
    // Keep test values in ascending order.
    assertThat(ControlUtils.deadband(-1.0, 0.5)).isWithin(1e-9).of(-1.0);
    assertThat(ControlUtils.deadband(-0.75, 0.5)).isWithin(1e-9).of(-0.5);
    assertThat(ControlUtils.deadband(0.6, 0.5)).isWithin(1e-9).of(0.2);
    assertThat(ControlUtils.deadband(0.75, 0.5)).isWithin(1e-9).of(0.5);
    assertThat(ControlUtils.deadband(1.0, 0.5)).isWithin(1e-9).of(1.0);
  }

  @Test
  public void deadbandZeroDeadbandHasNoEffect() {
    // Keep test values in ascending order.
    assertThat(ControlUtils.deadband(-1.0, 0.0)).isEqualTo(-1.0);
    assertThat(ControlUtils.deadband(-0.5, 0.0)).isEqualTo(-0.5);
    assertThat(ControlUtils.deadband(-0.25, 0.0)).isEqualTo(-0.25);
    assertThat(ControlUtils.deadband(0.0, 0.0)).isEqualTo(0.0);
    assertThat(ControlUtils.deadband(0.5, 0.0)).isEqualTo(0.5);
    assertThat(ControlUtils.deadband(1.0, 0.0)).isEqualTo(1.0);
  }

  /**
   * Asserts that {@code testExpression} is an expression that throws an exception of type {@code
   * IllegalArgumentException} with a message containing {@code expectedMessage}.
   *
   * @param testExpression An expression that is expected to throw an exception when executed.
   * @param expectedMessage (Part of an) error message expected in the exception.
   */
  private void assertIllegalArgumentExceptionIsThrownContainingMessage(
      Executable testExpression, String expectedMessage) {
    Exception exception = assertThrows(IllegalArgumentException.class, testExpression);
    assertThat(exception).hasMessageThat().contains(expectedMessage);
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
