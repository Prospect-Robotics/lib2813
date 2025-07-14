package com.team2813.lib2813.testing.truth;

import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.Truth.assertAbout;

import com.google.common.truth.DoubleSubject;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import edu.wpi.first.math.geometry.Translation3d;
import javax.annotation.Nullable;

/** Truth Subject for making assertions about {@link Translation3d} values. */
public final class Translation3dSubject extends Subject {

  // User-defined entry point
  public static Translation3dSubject assertThat(@Nullable Translation3d translation) {
    return assertAbout(translation3ds()).that(translation);
  }

  // Static method for getting the subject factory (for use with assertAbout())
  public static Factory<Translation3dSubject, Translation3d> translation3ds() {
    return Translation3dSubject::new;
  }

  private final Translation3d actual;

  private Translation3dSubject(FailureMetadata failureMetadata, @Nullable Translation3d subject) {
    super(failureMetadata, subject);
    this.actual = subject;
  }

  // User-defined test assertion SPI below this point

  public TolerantComparison<Translation3d> isWithin(double tolerance) {
    return new TolerantComparison<Translation3d>() {
      @Override
      public void of(Translation3d expected) {
        x().isWithin(tolerance).of(expected.getX());
        y().isWithin(tolerance).of(expected.getY());
        z().isWithin(tolerance).of(expected.getZ());
      }
    };
  }

  public void isZero() {
    if (!Translation3d.kZero.equals(actual)) {
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

  public DoubleSubject z() {
    return check("getZ()").that(nonNullActual().getZ());
  }

  // Helper methods below this point

  private Translation3d nonNullActual() {
    if (actual == null) {
      failWithActual(simpleFact("expected a non-null Translation3d"));
    }
    return actual;
  }
}
