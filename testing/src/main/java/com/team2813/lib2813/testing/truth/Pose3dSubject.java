package com.team2813.lib2813.testing.truth;

import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.Truth.assertAbout;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import edu.wpi.first.math.geometry.Pose3d;
import javax.annotation.Nullable;

/**
 * A Truth {@link Subject} for making assertions about {@link Pose3d} values.
 *
 * <p>This subject provides fluent assertions for comparing 3D poses, including tolerance-based
 * comparisons of translations and rotations.
 *
 * <p>See <a href="https://truth.dev/extension">Truth: Writing your own custom subject</a> for more
 * on extending Truth.
 */
public final class Pose3dSubject extends Subject {

  /**
   * Entry point for {@link Pose3d} assertions.
   *
   * <p>Usage:
   *
   * <pre>{@code
   * Pose3d actualPose = ...;
   * Pose3d expectedPose = ...;
   *
   * Pose3dSubject.assertThat(actualPose)
   *     .isWithin(0.01)
   *     .of(expectedPose);
   * }</pre>
   *
   * @param pose the pose under test (may be {@code null})
   * @return a {@link Pose3dSubject} for making assertions
   */
  public static Pose3dSubject assertThat(@Nullable Pose3d pose) {
    return assertAbout(pose3ds()).that(pose);
  }

  /**
   * Factory for {@link Pose2dSubject}, for use with assertAbout().
   *
   * @return a factory for creating {@link Pose3dSubject} instances
   */
  public static Subject.Factory<Pose3dSubject, Pose3d> pose3ds() {
    return Pose3dSubject::new;
  }

  private final Pose3d actual;

  private Pose3dSubject(FailureMetadata failureMetadata, @Nullable Pose3d subject) {
    super(failureMetadata, subject);
    this.actual = subject;
  }

  /**
   * Returns a tolerant comparison assertion for the current 3D pose.
   *
   * <p>The tolerance applies to both translation (x, y, z) and rotation (roll, pitch, yaw).
   *
   * @param tolerance the maximum allowed difference in meters (translation) or radians (rotation)
   * @return a {@link TolerantComparison} for comparing poses with a tolerance
   */
  public TolerantComparison<Pose3d> isWithin(double tolerance) {
    return new TolerantComparison<Pose3d>() {
      @Override
      public void of(Pose3d expected) {
        translation().isWithin(tolerance).of(expected.getTranslation());
        rotation().isWithin(tolerance).of(expected.getRotation());
      }
    };
  }

  /**
   * Returns a {@link Translation3dSubject} for making assertions about the translation component of
   * this pose.
   *
   * @return a subject for the pose's 3D translation
   */
  public Translation3dSubject translation() {
    return check("getTranslation()")
        .about(Translation3dSubject.translation3ds())
        .that(nonNullActualPose().getTranslation());
  }

  /**
   * Returns a {@link Rotation3dSubject} for making assertions about the rotation component of this
   * pose.
   *
   * @return a subject for the pose's 3D rotation
   */
  public Rotation3dSubject rotation() {
    return check("getRotation()")
        .about(Rotation3dSubject.rotation3ds())
        .that(nonNullActualPose().getRotation());
  }

  /**
   * Ensures that the actual pose is not {@code null}.
   *
   * @return the non-null actual {@link Pose3d}
   * @throws AssertionError if the pose under test is {@code null}
   */
  private Pose3d nonNullActualPose() {
    if (actual == null) {
      failWithActual(simpleFact("expected a non-null Pose3d"));
    }
    return actual;
  }
}
