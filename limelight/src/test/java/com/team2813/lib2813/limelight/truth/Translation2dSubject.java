package com.team2813.lib2813.limelight.truth;

import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.Truth.assertAbout;

import com.google.common.truth.DoubleSubject;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import edu.wpi.first.math.geometry.Translation2d;
import javax.annotation.Nullable;

/** Truth Subject for making assertions about {@link Translation2d} values. */
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
