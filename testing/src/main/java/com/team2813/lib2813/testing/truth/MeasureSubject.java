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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.truth.Fact.fact;
import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.Truth.assertAbout;

import com.google.common.primitives.Doubles;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import edu.wpi.first.units.Measure;
import edu.wpi.first.units.Unit;
import javax.annotation.Nullable;

/**
 * Truth subject for making assertions about {@link Measure} values.
 *
 * <p>See <a href="https://truth.dev/extension">Writing your own custom subject</a> to learn about
 * creating custom Truth subjects.
 *
 * @param <U> The WPILib Unit type of the {@link Measure}
 * @since 2.1.0
 */
public class MeasureSubject<U extends Unit> extends Subject {
  public static <U extends Unit> MeasureSubject<U> assertThat(@Nullable Measure<U> measure) {
    return assertAbout(MeasureSubject.<U>measures()).that(measure);
  }

  public static <U extends Unit> Subject.Factory<MeasureSubject<U>, Measure<U>> measures() {
    return MeasureSubject::new;
  }

  private final Measure<U> actual;

  private MeasureSubject(FailureMetadata failureMetadata, @Nullable Measure<U> subject) {
    super(failureMetadata, subject);
    this.actual = subject;
  }

  public TolerantComparison<Measure<U>> isWithin(Measure<U> tolerance) {
    return new TolerantComparison<Measure<U>>() {
      @Override
      public void of(Measure<U> expected) {
        Measure<U> actual = nonNullActual();
        checkTolerance(tolerance);
        if (!equalWithinTolerance(actual, expected, tolerance)) {
          failWithoutActual(
              fact("expected", formatUnit(expected)),
              fact("but was", formatUnit(actual)),
              fact("outside tolerance", formatUnit(tolerance)));
        }
      }
    };
  }

  public TolerantComparison<Measure<U>> isNotWithin(Measure<U> tolerance) {
    return new TolerantComparison<Measure<U>>() {
      @Override
      public void of(Measure<U> expected) {
        Measure<U> actual = nonNullActual();
        checkTolerance(tolerance);
        if (!notEqualWithinTolerance(actual, expected, tolerance)) {
          failWithoutActual(
              fact("expected not to be", formatUnit(expected)),
              fact("but was", formatUnit(actual)),
              fact("within tolerance", formatUnit(tolerance)));
        }
      }
    };
  }

  private static <U extends Unit> boolean equalWithinTolerance(
      Measure<U> left, Measure<U> right, Measure<U> tolerance) {
    return Math.abs(left.baseUnitMagnitude() - right.baseUnitMagnitude())
        <= Math.abs(tolerance.baseUnitMagnitude());
  }

  private static <U extends Unit> boolean notEqualWithinTolerance(
      Measure<U> left, Measure<U> right, Measure<U> tolerance) {
    double leftD = left.baseUnitMagnitude();
    double rightD = right.baseUnitMagnitude();
    if (Doubles.isFinite(leftD) && Doubles.isFinite(rightD)) {
      return Math.abs(leftD - rightD) > Math.abs(tolerance.baseUnitMagnitude());
    } else {
      return false;
    }
  }

  private static <U extends Unit> String formatUnit(Measure<U> measure) {
    return String.format("%g %s", measure.magnitude(), measure.unit().name());
  }

  private void checkTolerance(Measure<U> tolerance) {
    double mag = tolerance.baseUnitMagnitude();
    checkArgument(!Double.isNaN(mag), "tolerance cannot be NaN");
    checkArgument(mag >= 0, "tolerance (%s) cannot be negative", tolerance);
    checkArgument(
        mag != Double.POSITIVE_INFINITY, "tolerance cannot be POSITIVE_INFINITY", tolerance);
  }

  private Measure<U> nonNullActual() {
    if (actual == null) {
      failWithActual(simpleFact("expected a non-null Measure"));
    }
    return actual;
  }
}
