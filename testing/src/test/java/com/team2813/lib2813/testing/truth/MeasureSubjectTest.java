/*
Copyright 2026 Prospect Robotics SWENext Club

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
package com.team2813.lib2813.testing.truth;

import static com.google.common.truth.ExpectFailure.assertThat;
import static com.google.common.truth.Truth.assertThat;
import static edu.wpi.first.units.Units.Volts;
import static org.junit.jupiter.api.Assertions.*;

import com.google.common.truth.ExpectFailure;
import edu.wpi.first.units.measure.Voltage;
import org.junit.jupiter.api.Test;

/** Tests for {@link MeasureSubject}. */
class MeasureSubjectTest {

  @Test
  public void isWithin_toleranceIsNegative_throwsIllegalArgumentException() {
    Voltage expected = Volts.of(12);
    Voltage actual = Volts.of(12.001);
    Voltage tolerance = Volts.of(-0.01);

    IllegalArgumentException e =
        assertThrows(
            IllegalArgumentException.class,
            () -> MeasureSubject.assertThat(actual).isWithin(tolerance).of(expected));
    assertThat(e).hasMessageThat().contains("negative");
  }

  @Test
  public void isWithin_toleranceIsNan_throwsIllegalArgumentException() {
    Voltage expected = Volts.of(12);
    Voltage actual = Volts.of(12.001);
    Voltage tolerance = Volts.of(Double.NaN);

    IllegalArgumentException e =
        assertThrows(
            IllegalArgumentException.class,
            () -> MeasureSubject.assertThat(actual).isWithin(tolerance).of(expected));
    assertThat(e).hasMessageThat().contains("NaN");
  }

  @Test
  public void isWithin_toleranceIsInfinity_throwsIllegalArgumentException() {
    Voltage expected = Volts.of(12);
    Voltage actual = Volts.of(12.001);
    Voltage tolerance = Volts.of(Double.POSITIVE_INFINITY);

    IllegalArgumentException e =
        assertThrows(
            IllegalArgumentException.class,
            () -> MeasureSubject.assertThat(actual).isWithin(tolerance).of(expected));
    assertThat(e).hasMessageThat().contains("POSITIVE_INFINITY");
  }

  @Test
  public void isWithin_nullActual_throws() {
    Voltage expected = Volts.of(12);
    Voltage actual = null;
    Voltage tolerance = Volts.of(0.01);

    AssertionError e =
        assertThrows(
            AssertionError.class,
            () -> MeasureSubject.assertThat(actual).isWithin(tolerance).of(expected));
    assertThat(e).hasMessageThat().contains("non-null");
  }

  @Test
  public void isWithin_valueWithinTolerance_doesNotThrow() {
    Voltage expected = Volts.of(12);
    Voltage actual = Volts.of(12.001);
    Voltage tolerance = Volts.of(0.01);

    MeasureSubject.assertThat(actual).isWithin(tolerance).of(expected);
  }

  @Test
  public void isWithin_valueNotWithinTolerance_throws() {
    Voltage expected = Volts.of(12.1);
    Voltage actual = Volts.of(12.2);
    Voltage tolerance = Volts.of(0.001);

    AssertionError e =
        assertThrows(
            AssertionError.class,
            () -> MeasureSubject.assertThat(actual).isWithin(tolerance).of(expected));
    assertThat(e).factKeys().containsExactly("expected", "but was", "outside tolerance");
    assertThat(e).factValue("expected").matches("12\\.1.*Volt");
    assertThat(e).factValue("but was").matches("12\\.2.*Volt");
    assertThat(e).factValue("outside tolerance").matches("0\\.001.*Volt");
  }

  @Test
  public void isWithin_actualPositiveInfinity_throws() {
    Voltage expected = Volts.of(12.1);
    Voltage actual = Volts.of(Double.POSITIVE_INFINITY);
    Voltage tolerance = Volts.of(0.1);

    AssertionError e =
        assertThrows(
            AssertionError.class,
            () -> MeasureSubject.assertThat(actual).isWithin(tolerance).of(expected));
    ExpectFailure.assertThat(e)
        .factKeys()
        .containsExactly("expected", "but was", "outside tolerance");
    ExpectFailure.assertThat(e).factValue("expected").matches("12\\.1.*Volt");
    ExpectFailure.assertThat(e).factValue("but was").matches("Infinity Volt");
    ExpectFailure.assertThat(e).factValue("outside tolerance").matches("0\\.1.*Volt");
  }

  @Test
  public void isWithin_expectedPositiveInfinity_throws() {
    Voltage expected = Volts.of(Double.POSITIVE_INFINITY);
    Voltage actual = Volts.of(12.1);
    Voltage tolerance = Volts.of(0.1);

    AssertionError e =
        assertThrows(
            AssertionError.class,
            () -> MeasureSubject.assertThat(actual).isWithin(tolerance).of(expected));
    ExpectFailure.assertThat(e)
        .factKeys()
        .containsExactly("expected", "but was", "outside tolerance");
    ExpectFailure.assertThat(e).factValue("expected").matches("Infinity Volt");
    ExpectFailure.assertThat(e).factValue("but was").matches("12\\.1.*Volt");
    ExpectFailure.assertThat(e).factValue("outside tolerance").matches("0\\.1.*Volt");
  }

  @Test
  public void isWithin_bothPositiveInfinity_throws() {
    Voltage expected = Volts.of(Double.POSITIVE_INFINITY);
    Voltage actual = Volts.of(Double.POSITIVE_INFINITY);
    Voltage tolerance = Volts.of(0.1);

    AssertionError e =
        assertThrows(
            AssertionError.class,
            () -> MeasureSubject.assertThat(actual).isWithin(tolerance).of(expected));
    ExpectFailure.assertThat(e)
        .factKeys()
        .containsExactly("expected", "but was", "outside tolerance");
    ExpectFailure.assertThat(e).factValue("expected").matches("Infinity Volt");
    ExpectFailure.assertThat(e).factValue("but was").matches("Infinity Volt");
    ExpectFailure.assertThat(e).factValue("outside tolerance").matches("0\\.1.*Volt");
  }

  @Test
  public void isNotWithin_toleranceIsNegative_throwsIllegalArgumentException() {
    Voltage expected = Volts.of(12);
    Voltage actual = Volts.of(12.001);
    Voltage tolerance = Volts.of(-0.01);

    IllegalArgumentException e =
        assertThrows(
            IllegalArgumentException.class,
            () -> MeasureSubject.assertThat(actual).isNotWithin(tolerance).of(expected));
    assertThat(e).hasMessageThat().contains("negative");
  }

  @Test
  public void isNotWithin_toleranceIsNan_throwsIllegalArgumentException() {
    Voltage expected = Volts.of(12);
    Voltage actual = Volts.of(12.001);
    Voltage tolerance = Volts.of(Double.NaN);

    IllegalArgumentException e =
        assertThrows(
            IllegalArgumentException.class,
            () -> MeasureSubject.assertThat(actual).isNotWithin(tolerance).of(expected));
    assertThat(e).hasMessageThat().contains("NaN");
  }

  @Test
  public void isNotWithin_toleranceIsInfinity_throwsIllegalArgumentException() {
    Voltage expected = Volts.of(12);
    Voltage actual = Volts.of(12.001);
    Voltage tolerance = Volts.of(Double.POSITIVE_INFINITY);

    IllegalArgumentException e =
        assertThrows(
            IllegalArgumentException.class,
            () -> MeasureSubject.assertThat(actual).isNotWithin(tolerance).of(expected));
    assertThat(e).hasMessageThat().contains("POSITIVE_INFINITY");
  }

  @Test
  public void isNotWithin_nullActual_throws() {
    Voltage expected = Volts.of(12);
    Voltage actual = null;
    Voltage tolerance = Volts.of(0.01);

    AssertionError e =
        assertThrows(
            AssertionError.class,
            () -> MeasureSubject.assertThat(actual).isNotWithin(tolerance).of(expected));
    assertThat(e).hasMessageThat().contains("non-null");
  }

  @Test
  public void isNotWithin_valueNotWithinTolerance_doesNotThrow() {
    Voltage expected = Volts.of(12);
    Voltage actual = Volts.of(12.1);
    Voltage tolerance = Volts.of(0.001);

    MeasureSubject.assertThat(actual).isNotWithin(tolerance).of(expected);
  }

  @Test
  public void isNotWithin_valueWithinTolerance_throws() {
    Voltage expected = Volts.of(12.1);
    Voltage actual = Volts.of(12.01);
    Voltage tolerance = Volts.of(0.1);

    AssertionError e =
        assertThrows(
            AssertionError.class,
            () -> MeasureSubject.assertThat(actual).isNotWithin(tolerance).of(expected));
    ExpectFailure.assertThat(e)
        .factKeys()
        .containsExactly("expected not to be", "but was", "within tolerance");
    ExpectFailure.assertThat(e).factValue("expected not to be").matches("12\\.1.*Volt");
    ExpectFailure.assertThat(e).factValue("but was").matches("12\\.01.*Volt");
    ExpectFailure.assertThat(e).factValue("within tolerance").matches("0\\.1.*Volt");
  }

  @Test
  public void isNotWithin_actualPositiveInfinity_throws() {
    Voltage expected = Volts.of(12.1);
    Voltage actual = Volts.of(Double.POSITIVE_INFINITY);
    Voltage tolerance = Volts.of(0.1);

    AssertionError e =
        assertThrows(
            AssertionError.class,
            () -> MeasureSubject.assertThat(actual).isNotWithin(tolerance).of(expected));
    ExpectFailure.assertThat(e)
        .factKeys()
        .containsExactly("expected not to be", "but was", "within tolerance");
    ExpectFailure.assertThat(e).factValue("expected not to be").matches("12\\.1.*Volt");
    ExpectFailure.assertThat(e).factValue("but was").matches("Infinity Volt");
    ExpectFailure.assertThat(e).factValue("within tolerance").matches("0\\.1.*Volt");
  }

  @Test
  public void isNotWithin_expectedPositiveInfinity_throws() {
    Voltage expected = Volts.of(Double.POSITIVE_INFINITY);
    Voltage actual = Volts.of(12.1);
    Voltage tolerance = Volts.of(0.1);

    AssertionError e =
        assertThrows(
            AssertionError.class,
            () -> MeasureSubject.assertThat(actual).isNotWithin(tolerance).of(expected));
    ExpectFailure.assertThat(e)
        .factKeys()
        .containsExactly("expected not to be", "but was", "within tolerance");
    ExpectFailure.assertThat(e).factValue("expected not to be").matches("Infinity Volt");
    ExpectFailure.assertThat(e).factValue("but was").matches("12\\.1.*Volt");
    ExpectFailure.assertThat(e).factValue("within tolerance").matches("0\\.1.*Volt");
  }

  @Test
  public void isNotWithin_bothPositiveInfinity_throws() {
    Voltage expected = Volts.of(Double.POSITIVE_INFINITY);
    Voltage actual = Volts.of(Double.POSITIVE_INFINITY);
    Voltage tolerance = Volts.of(0.1);

    AssertionError e =
        assertThrows(
            AssertionError.class,
            () -> MeasureSubject.assertThat(actual).isNotWithin(tolerance).of(expected));
    ExpectFailure.assertThat(e)
        .factKeys()
        .containsExactly("expected not to be", "but was", "within tolerance");
    ExpectFailure.assertThat(e).factValue("expected not to be").matches("Infinity Volt");
    ExpectFailure.assertThat(e).factValue("but was").matches("Infinity Volt");
    ExpectFailure.assertThat(e).factValue("within tolerance").matches("0\\.1.*Volt");
  }
}
