package com.team2813.lib2813.testing.truth;

import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.Truth.assertAbout;

import com.google.common.truth.DoubleSubject;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import edu.wpi.first.math.geometry.Translation3d;
import javax.annotation.Nullable;

/**
 * A Truth {@link Subject} for making fluent assertions about {@link Translation3d} values.
 *
 * <p>This class allows tests to verify the x, y, and z components of a {@code Translation3d},
 * perform approximate comparisons within a tolerance, and check special properties like being zero.
 *
 * <p>Example usage:
 * <pre>{@code
 * Translation3d translation = new Translation3d(1.0, 2.0, 3.0);
 * Translation3dSubject.assertThat(translation)
 *     .isWithin(0.01).of(new Translation3d(1.0, 2.0, 3.0));
 * Translation3dSubject.assertThat(translation).x().isEqualTo(1.0);
 * Translation3dSubject.assertThat(translation).y().isEqualTo(2.0);
 * Translation3dSubject.assertThat(translation).z().isEqualTo(3.0);
 * }</pre>
 */
public final class Translation3dSubject extends Subject {

  /**
   * Entry point for assertions about a {@link Translation3d} instance.
   *
   * @param translation the translation to assert about (nullable)
   * @return a {@link Translation3dSubject} for fluent assertions
   */
  public static Translation3dSubject assertThat(@Nullable Translation3d translation) {
    return assertAbout(translation3ds()).that(translation);
  }

  /**
   * Returns a Truth {@link Factory} for {@link Translation3dSubject}, used with
   * {@link com.google.common.truth.Truth#assertAbout(Subject.Factory)}.
   *
   * @return a factory for creating {@link Translation3dSubject} instances
   */
  public static Factory<Translation3dSubject, Translation3d> translation3ds() {
    return Translation3dSubject::new;
  }

  private final Translation3d actual;

  /**
   * Constructor used internally by Truth.
   *
   * @param failureMetadata metadata about the failure for reporting
   * @param subject the actual {@link Translation3d} value (nullable)
   */
  private Translation3dSubject(FailureMetadata failureMetadata, @Nullable Translation3d subject) {
    super(failureMetadata, subject);
    this.actual = subject;
  }

  /**
   * Returns a {@link TolerantComparison} to assert that this translation is within the given
   * {@code tolerance} of an expected {@link Translation3d}.
   *
   * @param tolerance the allowed absolute difference in x, y, and z components
   * @return a {@link TolerantComparison} for fluent approximate comparisons
   */
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

  /**
   * Fails if the {@link Translation3d} is not the zero vector.
   */
  public void isZero() {
    if (!Translation3d.kZero.equals(actual)) {
      failWithActual(simpleFact("expected to be zero"));
    }
  }

  /**
   * Returns a {@link DoubleSubject} for assertions on the x-component of the translation.
   *
   * @return a {@link DoubleSubject} for the x value
   */
  public DoubleSubject x() {
    return check("getX()").that(nonNullActual().getX());
  }

  /**
   * Returns a {@link DoubleSubject} for assertions on the y-component of the translation.
   *
   * @return a {@link DoubleSubject} for the y value
   */
  public DoubleSubject y() {
    return check("getY()").that(nonNullActual().getY());
  }

  /**
   * Returns a {@link DoubleSubject} for assertions on the z-component of the translation.
   *
   * @return a {@link DoubleSubject} for the z value
   */
  public DoubleSubject z() {
    return check("getZ()").that(nonNullActual().getZ());
  }

  /**
   * Ensures the actual translation is non-null, failing the assertion if it is null.
   *
   * @return the non-null {@link Translation3d} instance
   */
  private Translation3d nonNullActual() {
    if (actual == null) {
      failWithActual(simpleFact("expected a non-null Translation3d"));
    }
    return actual;
  }
}
