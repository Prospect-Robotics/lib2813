/*
Copyright 2025-2026 Prospect Robotics SWENext Club

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

import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.Truth.assertAbout;

import com.google.common.truth.DoubleSubject;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import edu.wpi.first.math.geometry.Translation2d;
import javax.annotation.Nullable;

/**
 * Truth Subject for making assertions about {@link Translation2d} values.
 *
 * @since 2.0.0
 */
public final class Translation2dSubject extends Subject {

  // User-defined entry point
  public static Translation2dSubject assertThat(@Nullable Translation2d translation) {
    return assertAbout(translation2ds()).that(translation);
  }

  // Static method for getting the subject factory (for use with assertAbout())
  public static Factory<Translation2dSubject, Translation2d> translation2ds() {
    return Translation2dSubject::new;
  }

  private final Translation2d actual;

  private Translation2dSubject(FailureMetadata failureMetadata, @Nullable Translation2d subject) {
    super(failureMetadata, subject);
    this.actual = subject;
  }

  // User-defined test assertion SPI below this point

  public TolerantComparison<Translation2d> isWithin(double tolerance) {
    return new TolerantComparison<Translation2d>() {
      @Override
      public void of(Translation2d expected) {
        x().isWithin(tolerance).of(expected.getX());
        y().isWithin(tolerance).of(expected.getY());
      }
    };
  }

  public TolerantComparison<Translation2d> isNotWithin(double tolerance) {
    return new TolerantComparison<Translation2d>() {
      @Override
      public void of(Translation2d expected) {
        x().isWithin(tolerance).of(expected.getX());
        y().isWithin(tolerance).of(expected.getY());
      }
    };
  }

  public void isZero() {
    if (!Translation2d.kZero.equals(actual)) {
      failWithActual(simpleFact("expected to be zero"));
    }
  }

  // Chained subjects methods below this point

  public DoubleSubject x() {
    return check("getX()").that(nonNullActual().getX());
  }

  public DoubleSubject y() {
    return check("getY()").that(nonNullActual().getY());
  }

  // Helper methods below this point

  private Translation2d nonNullActual() {
    if (actual == null) {
      failWithActual(simpleFact("expected a non-null Translation2d"));
    }
    return actual;
  }
}
