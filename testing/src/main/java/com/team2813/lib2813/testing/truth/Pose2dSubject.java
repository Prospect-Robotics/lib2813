package com.team2813.lib2813.testing.truth;

import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.Truth.assertAbout;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import edu.wpi.first.math.geometry.Pose2d;
import javax.annotation.Nullable;

/**
 * A Truth {@link Subject} for making assertions about {@link Pose2d} values.
 *
 * <p>This subject provides fluent assertions for comparing poses, including tolerance-based
 * comparisons of translations and rotations.
 *
 * <p>See <a href="https://truth.dev/extension">Truth: Writing your own custom subject</a> for more
 * on extending Truth.
 */
public final class Pose2dSubject extends Subject {

  /**
   * Entry point for {@link Pose2d} assertions.
   *
   * <p>Usage:
   *
   * <pre>{@code
   * Pose2d actualPose = ...;
   * Pose2d expectedPose = ...;
   *
   * Pose2dSubject.assertThat(actualPose)
   *     .isWithin(0.01)
   *     .of(expectedPose);
   * }</pre>
   *
   * @param pose the pose under test (may be {@code null})
   * @return a {@link Pose2dSubject} for making assertions
   */
  public static Pose2dSubject assertThat(@Nullable Pose2d pose) {
    return assertAbout(pose2ds()).that(pose);
  }

  /**
   * Factory for {@link Pose2dSubject}, for use with assertAbout().
   *
   * @return a factory for creating {@link Pose2dSubject} instances
   */
  public static Subject.Factory<Pose2dSubject, Pose2d> pose2ds() {
    return Pose2dSubject::new;
  }

  private final Pose2d actual;

  private Pose2dSubject(FailureMetadata failureMetadata, @Nullable Pose2d subject) {
    super(failureMetadata, subject);
    this.actual = subject;
  }

  /**
   * Returns a tolerant comparison assertion for the current pose.
   *
   * <p>The tolerance applies to both translation (x, y) and rotation (θ).
   *
   * @param tolerance the maximum allowed difference in meters (translation) or radians (rotation)
   * @return a {@link TolerantComparison} for comparing poses with a tolerance
   */
  public TolerantComparison<Pose2d> isWithin(double tolerance) {
    return new TolerantComparison<Pose2d>() {
      @Override
      public void of(Pose2d expected) {
        translation().isWithin(tolerance).of(expected.getTranslation());
        rotation().isWithin(tolerance).of(expected.getRotation());
      }
    };
  }

  /**
   * Returns a {@link Translation2dSubject} for making assertions about the translation component of
   * this pose.
   *
   * @return a subject for the pose's translation
   */
  public Translation2dSubject translation() {
    return check("getTranslation()")
        .about(Translation2dSubject.translation2ds())
        .that(nonNullActualPose().getTranslation());
  }

  /**
   * Returns a {@link Rotation2dSubject} for making assertions about the rotation component of this
   * pose.
   *
   * @return a subject for the pose's rotation
   */
  public Rotation2dSubject rotation() {
    return check("getRotation()")
        .about(Rotation2dSubject.rotation2ds())
        .that(nonNullActualPose().getRotation());
  }

  /**
   * Ensures that the actual pose is not {@code null}.
   *
   * @return the non-null actual {@link Pose2d}
   * @throws AssertionError if the pose under test is {@code null}
   */
  private Pose2d nonNullActualPose() {
    if (actual == null) {
      failWithActual(simpleFact("expected a non-null Pose2d"));
    }
    return actual;
  }
}
