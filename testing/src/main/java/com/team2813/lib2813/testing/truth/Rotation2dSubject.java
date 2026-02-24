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
import edu.wpi.first.math.geometry.Rotation2d;
import javax.annotation.Nullable;

/**
 * Truth Subject for making assertions about {@link Rotation2d} values.
 *
 * @since 2.0.0
 */
public final class Rotation2dSubject extends Subject {
  // User-defined entry point
  public static Rotation2dSubject assertThat(@Nullable Rotation2d rotation) {
    return assertAbout(rotation2ds()).that(rotation);
  }

  // Static method for getting the subject factory (for use with assertAbout())
  public static Factory<Rotation2dSubject, Rotation2d> rotation2ds() {
    return Rotation2dSubject::new;
  }

  private final Rotation2d actual;

  private Rotation2dSubject(FailureMetadata failureMetadata, @Nullable Rotation2d subject) {
    super(failureMetadata, subject);
    this.actual = subject;
  }

  // User-defined test assertion SPI below this point

  public TolerantComparison<Rotation2d> isWithin(double tolerance) {
    return new TolerantComparison<Rotation2d>() {
      @Override
      public void of(Rotation2d expected) {
        getRadians().isWithin(tolerance).of(expected.getRadians());
      }
    };
  }

  public TolerantComparison<Rotation2d> isNotWithin(double tolerance) {
    return new TolerantComparison<Rotation2d>() {
      @Override
      public void of(Rotation2d expected) {
        getRadians().isNotWithin(tolerance).of(expected.getRadians());
      }
    };
  }

  public void isZero() {
    if (!Rotation2d.kZero.equals(actual)) {
      failWithActual(simpleFact("expected to be zero"));
    }
  }

  // Chained subjects methods below this point

  public DoubleSubject getRadians() {
    return check("getRadians()").that(nonNullActual().getRadians());
  }

  // Helper methods below this point

  private Rotation2d nonNullActual() {
    if (actual == null) {
      failWithActual(simpleFact("expected a non-null Rotation2d"));
    }
    return actual;
  }
}
