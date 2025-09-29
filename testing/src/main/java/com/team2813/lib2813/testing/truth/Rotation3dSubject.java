package com.team2813.lib2813.testing.truth;

import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.Truth.assertAbout;

import com.google.common.truth.DoubleSubject;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import edu.wpi.first.math.geometry.Rotation3d;
import javax.annotation.Nullable;

/**
 * A Truth {@link Subject} for making assertions about {@link Rotation3d} values.
 *
 * <p>This subject provides fluent assertions for comparing 3D rotations,
 * including tolerance-based comparisons of roll, pitch, and yaw angles,
 * as well as checks for zero rotation.
 *
 * <p>Rotations are expressed in radians:
 * <ul>
 *   <li>{@link #x()} → roll (counterclockwise about X axis)</li>
 *   <li>{@link #y()} → pitch (counterclockwise about Y axis)</li>
 *   <li>{@link #z()} → yaw (counterclockwise about Z axis)</li>
 * </ul>
 */
public final class Rotation3dSubject extends Subject {

  /**
   * Entry point for {@link Rotation3d} assertions.
   *
   * <p>Usage:
   * <pre>{@code
   * Rotation3d actual = new Rotation3d(Math.PI / 2, 0, 0);
   * Rotation3d expected = new Rotation3d(Math.PI / 2 + 1e-3, 0, 0);
   *
   * Rotation3dSubject.assertThat(actual)
   *     .isWithin(1e-2)
   *     .of(expected);
   * }</pre>
   *
   * @param rotation the rotation under test (may be {@code null})
   * @return a {@link Rotation3dSubject} for making assertions
   */
  public static Rotation3dSubject assertThat(@Nullable Rotation3d rotation) {
    return assertAbout(rotation3ds()).that(rotation);
  }

  /**
   * Factory for {@link Rotation3dSubject}, used with {@link assertAbout}.
   *
   * @return a factory for creating {@link Rotation3dSubject} instances
   */
  public static Factory<Rotation3dSubject, Rotation3d> rotation3ds() {
    return Rotation3dSubject::new;
  }

  private final Rotation3d actual;

  private Rotation3dSubject(FailureMetadata failureMetadata, @Nullable Rotation3d subject) {
    super(failureMetadata, subject);
    this.actual = subject;
  }

  /**
   * Returns a tolerant comparison assertion for this rotation.
   *
   * <p>The tolerance is expressed in radians and applies to each of the
   * roll (X), pitch (Y), and yaw (Z) components independently.
   *
   * @param tolerance the maximum allowed difference in radians
   * @return a {@link TolerantComparison} for comparing rotations with a tolerance
   */
  public TolerantComparison<Rotation3d> isWithin(double tolerance) {
    return new TolerantComparison<Rotation3d>() {
      @Override
      public void of(Rotation3d expected) {
        x().isWithin(tolerance).of(expected.getX()); // roll
        y().isWithin(tolerance).of(expected.getY()); // pitch
        z().isWithin(tolerance).of(expected.getZ()); // yaw
      }
    };
  }

  /**
   * Asserts that this rotation is exactly equal to {@link Rotation3d#kZero}.
   *
   * <p>Fails if the rotation under test is {@code null} or not equal to zero.
   */
  public void isZero() {
    if (!Rotation3d.kZero.equals(actual)) {
      failWithActual(simpleFact("expected to be zero"));
    }
  }

  /**
   * Returns a subject for making assertions about the roll (X-axis rotation, radians).
   *
   * @return a {@link DoubleSubject} for the roll component in radians
   */
  public DoubleSubject x() {
    return check("getX()").that(nonNullActual().getX());
  }

  /**
   * Returns a subject for making assertions about the pitch (Y-axis rotation, radians).
   *
   * @return a {@link DoubleSubject} for the pitch component in radians
   */
  public DoubleSubject y() {
    return check("getY()").that(nonNullActual().getY());
  }

  /**
   * Returns a subject for making assertions about the yaw (Z-axis rotation, radians).
   *
   * @return a {@link DoubleSubject} for the yaw component in radians
   */
  public DoubleSubject z() {
    return check("getZ()").that(nonNullActual().getZ());
  }

  /**
   * Ensures that the actual rotation is not {@code null}.
   *
   * @return the non-null actual {@link Rotation3d}
   * @throws AssertionError if the rotation under test is {@code null}
   */
  private Rotation3d nonNullActual() {
    if (actual == null) {
      failWithActual(simpleFact("expected a non-null Rotation3d"));
    }
    return actual;
  }
}
