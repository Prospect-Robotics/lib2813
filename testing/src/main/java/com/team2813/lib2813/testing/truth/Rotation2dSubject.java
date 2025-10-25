package com.team2813.lib2813.testing.truth;

import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.Truth.assertAbout;

import com.google.common.truth.DoubleSubject;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import edu.wpi.first.math.geometry.Rotation2d;
import javax.annotation.Nullable;

/**
 * A Truth {@link Subject} for making assertions about {@link Rotation2d} values.
 *
 * <p>This subject provides fluent assertions for comparing 2D rotations, including tolerance-based
 * comparisons and checks for zero rotation.
 *
 * <p>Rotations are compared in radians.
 */
public final class Rotation2dSubject extends Subject {

  /**
   * Entry point for {@link Rotation2d} assertions.
   *
   * <p>Usage:
   *
   * <pre>{@code
   * Rotation2d actual = new Rotation2d(Math.PI / 2);
   * Rotation2d expected = new Rotation2d(Math.PI / 2 + 1e-3);
   *
   * Rotation2dSubject.assertThat(actual)
   *     .isWithin(1e-2)
   *     .of(expected);
   * }</pre>
   *
   * @param rotation the rotation under test (may be {@code null})
   * @return a {@link Rotation2dSubject} for making assertions
   */
  public static Rotation2dSubject assertThat(@Nullable Rotation2d rotation) {
    return assertAbout(rotation2ds()).that(rotation);
  }

  /**
   * Factory for {@link Pose2dSubject}, for use with assertAbout().
   *
   * @return a factory for creating {@link Rotation2dSubject} instances
   */
  public static Factory<Rotation2dSubject, Rotation2d> rotation2ds() {
    return Rotation2dSubject::new;
  }

  private final Rotation2d actual;

  private Rotation2dSubject(FailureMetadata failureMetadata, @Nullable Rotation2d subject) {
    super(failureMetadata, subject);
    this.actual = subject;
  }

  /**
   * Returns a tolerant comparison assertion for this rotation.
   *
   * <p>The tolerance is expressed in radians.
   *
   * @param tolerance the maximum allowed difference in radians
   * @return a {@link TolerantComparison} for comparing rotations with a tolerance
   */
  public TolerantComparison<Rotation2d> isWithin(double tolerance) {
    return new TolerantComparison<Rotation2d>() {
      @Override
      public void of(Rotation2d expected) {
        getRadians().isWithin(tolerance).of(expected.getRadians());
      }
    };
  }

  /**
   * Asserts that this rotation is exactly equal to {@link Rotation2d#kZero}.
   *
   * <p>Fails if the rotation under test is {@code null} or not equal to zero.
   */
  public void isZero() {
    if (!Rotation2d.kZero.equals(actual)) {
      failWithActual(simpleFact("expected to be zero"));
    }
  }

  /**
   * Returns a {@link DoubleSubject} for making assertions about the rotation’s raw radians value.
   *
   * @return a {@link DoubleSubject} for the rotation’s radians
   */
  public DoubleSubject getRadians() {
    return check("getRadians()").that(nonNullActual().getRadians());
  }

  /**
   * Ensures that the actual rotation is not {@code null}.
   *
   * @return the non-null actual {@link Rotation2d}
   * @throws AssertionError if the rotation under test is {@code null}
   */
  private Rotation2d nonNullActual() {
    if (actual == null) {
      failWithActual(simpleFact("expected a non-null Rotation2d"));
    }
    return actual;
  }
}
