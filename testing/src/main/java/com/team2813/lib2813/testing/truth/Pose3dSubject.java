package com.team2813.lib2813.testing.truth;

import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.Truth.assertAbout;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import edu.wpi.first.math.geometry.Pose3d;
import javax.annotation.Nullable;

/**
 * Truth Subject for making assertions about {@link Pose3d} values.
 *
 * <p>See <a href="https://truth.dev/extension">Writing your own custom subject</a> to learn about
 * creating custom Truth subjects.
 */
public final class Pose3dSubject extends Subject {

  // User-defined entry point
  public static Pose3dSubject assertThat(@Nullable Pose3d pose) {
    return assertAbout(pose3ds()).that(pose);
  }

  // Static method for getting the subject factory (for use with assertAbout())
  public static Subject.Factory<Pose3dSubject, Pose3d> pose3ds() {
    return Pose3dSubject::new;
  }

  private final Pose3d actual;

  private Pose3dSubject(FailureMetadata failureMetadata, @Nullable Pose3d subject) {
    super(failureMetadata, subject);
    this.actual = subject;
  }

  // User-defined test assertion SPI below this point

  public TolerantComparison<Pose3d> isWithin(double tolerance) {
    return new TolerantComparison<Pose3d>() {
      @Override
      public void of(Pose3d expected) {
        translation().isWithin(tolerance).of(expected.getTranslation());
        rotation().isWithin(tolerance).of(expected.getRotation());
      }
    };
  }

  // Chained subjects methods below this point

  public Translation3dSubject translation() {
    return check("getTranslation()")
        .about(Translation3dSubject.translation3ds())
        .that(nonNullActualPose().getTranslation());
  }

  public Rotation3dSubject rotation() {
    return check("getRotation()")
        .about(Rotation3dSubject.rotation3ds())
        .that(nonNullActualPose().getRotation());
  }

  // Helper methods below this point

  private Pose3d nonNullActualPose() {
    if (actual == null) {
      failWithActual(simpleFact("expected a non-null Pose3d"));
    }
    return actual;
  }
}
