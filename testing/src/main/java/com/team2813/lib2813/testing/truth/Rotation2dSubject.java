package com.team2813.lib2813.testing.truth;

import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.Truth.assertAbout;

import com.google.common.truth.DoubleSubject;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import edu.wpi.first.math.geometry.Rotation2d;
import javax.annotation.Nullable;

/** Truth Subject for making assertions about {@link Rotation2d} values. */
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
