package com.team2813.lib2813.limelight.truth;

import com.google.common.truth.DoubleSubject;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import edu.wpi.first.math.geometry.Rotation3d;

import javax.annotation.Nullable;

import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.Truth.assertAbout;

/** Truth Subject for making assertions about {@link Rotation3d} values. */
public final class Rotation3dSubject extends Subject {
  // User-defined entry point
  public static Rotation3dSubject assertThat(@Nullable Rotation3d rotation) {
    return assertAbout(rotation3ds()).that(rotation);
  }

  // Static method for getting the subject factory (for use with assertAbout())
  public static Factory<Rotation3dSubject, Rotation3d> rotation3ds() {
    return Rotation3dSubject::new;
  }

  private final Rotation3d actual;

  private Rotation3dSubject(FailureMetadata failureMetadata, @Nullable Rotation3d subject) {
    super(failureMetadata, subject);
    this.actual = subject;
  }

  // User-defined test assertion SPI below this point

  public TolerantComparison<Rotation3d> isWithin(double tolerance) {
    return new TolerantComparison<Rotation3d>() {
      @Override
      public void of(Rotation3d expected) {
        x().isWithin(tolerance).of(expected.getX()); // roll, in radians
        y().isWithin(tolerance).of(expected.getY()); // pitch, in radians
        z().isWithin(tolerance).of(expected.getZ()); // yaw, in radians
      }
    };
  }

  public void isZero() {
    if (!Rotation3d.kZero.equals(actual)) {
      failWithActual(simpleFact("expected to be zero"));
    }
  }

  // Chained subjects methods below this point

  /**
   * Returns a subject that can be used to make assertions about the
   * counterclockwise rotation angle around the X axis (roll) in radians.
   */
  public DoubleSubject x() {
    return check("getX()").that(nonNullActual().getX());
  }

  /**
   * Returns a subject that can be used to make assertions about the
   * counterclockwise rotation angle around the Y axis (pitch) in radians.
   */
  public DoubleSubject y() {
    return check("getY()").that(nonNullActual().getY());
  }

  /**
   * Returns a subject that can be used to make assertions about the
   * counterclockwise rotation angle around the Z axis (yaw) in radians.
   */
  public DoubleSubject z() {
    return check("getZ()").that(nonNullActual().getZ());
  }

  // Helper methods below this point

  private Rotation3d nonNullActual() {
    if (actual == null) {
      failWithActual(simpleFact("expected a non-null Rotation3d"));
    }
    return actual;
  }
}
