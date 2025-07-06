package com.team2813.lib2813.testing.truth;

import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.Truth.assertAbout;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import edu.wpi.first.math.geometry.Pose2d;
import javax.annotation.Nullable;

/**
 * Truth Subject for making assertions about {@link Pose2d} values.
 *
 * <p>See <a href="https://truth.dev/extension">Writing your own custom subject</a> to learn about
 * creating custom Truth subjects.
 */
public final class Pose2dSubject extends Subject {

  // User-defined entry point
  public static Pose2dSubject assertThat(@Nullable Pose2d pose) {
    return assertAbout(pose2ds()).that(pose);
  }

  // Static method for getting the subject factory (for use with assertAbout())
  public static Subject.Factory<Pose2dSubject, Pose2d> pose2ds() {
    return Pose2dSubject::new;
  }

  private final Pose2d actual;

  private Pose2dSubject(FailureMetadata failureMetadata, @Nullable Pose2d subject) {
    super(failureMetadata, subject);
    this.actual = subject;
  }

  // User-defined test assertion SPI below this point

  public TolerantComparison<Pose2d> isWithin(double tolerance) {
    return new TolerantComparison<Pose2d>() {
      @Override
      public void of(Pose2d expected) {
        translation().isWithin(tolerance).of(expected.getTranslation());
        rotation().isWithin(tolerance).of(expected.getRotation());
      }
    };
  }

  public Translation2dSubject translation() {
    return check("getTranslation()")
        .about(Translation2dSubject.translation2ds())
        .that(nonNullActualPose().getTranslation());
  }

  public Rotation2dSubject rotation() {
    return check("getRotation()")
        .about(Rotation2dSubject.rotation2ds())
        .that(nonNullActualPose().getRotation());
  }

  // Helper methods below this point

  private Pose2d nonNullActualPose() {
    if (actual == null) {
      failWithActual(simpleFact("expected a non-null Pose2d"));
    }
    return actual;
  }
}
