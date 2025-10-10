package com.team2813.lib2813.testing.truth;

import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.Truth.assertAbout;

import com.google.common.truth.DoubleSubject;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import edu.wpi.first.math.geometry.Translation2d;
import javax.annotation.Nullable;

/**
 * A Truth {@link Subject} for making fluent assertions about {@link Translation2d} values.
 *
 * <p>This class allows tests to verify the x and y components of a {@code Translation2d}, perform
 * approximate comparisons within a tolerance, and check special properties like being zero.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * Translation2d translation = new Translation2d(1.0, 2.0);
 * Translation2dSubject.assertThat(translation)
 *     .isWithin(0.01).of(new Translation2d(1.0, 2.0));
 * Translation2dSubject.assertThat(translation).x().isEqualTo(1.0);
 * Translation2dSubject.assertThat(translation).y().isEqualTo(2.0);
 * }</pre>
 */
public final class Translation2dSubject extends Subject {

  /**
   * Entry point for assertions about a {@link Translation2d} instance.
   *
   * @param translation the translation to assert about (nullable)
   * @return a {@link Translation2dSubject} for fluent assertions
   */
  public static Translation2dSubject assertThat(@Nullable Translation2d translation) {
    return assertAbout(translation2ds()).that(translation);
  }

  /**
   * Returns a Truth {@link Factory} for {@link Translation2dSubject}, used with {@link
   * com.google.common.truth.Truth#assertAbout(Subject.Factory)}.
   *
   * @return a factory for creating {@link Translation2dSubject} instances
   */
  public static Factory<Translation2dSubject, Translation2d> translation2ds() {
    return Translation2dSubject::new;
  }

  private final Translation2d actual;

  /**
   * Constructor used internally by Truth.
   *
   * @param failureMetadata metadata about the failure for reporting
   * @param subject the actual {@link Translation2d} value (nullable)
   */
  private Translation2dSubject(FailureMetadata failureMetadata, @Nullable Translation2d subject) {
    super(failureMetadata, subject);
    this.actual = subject;
  }

  /**
   * Returns a {@link TolerantComparison} to assert that this translation is within the given {@code
   * tolerance} of an expected {@link Translation2d}.
   *
   * @param tolerance the allowed absolute difference in both x and y components
   * @return a {@link TolerantComparison} for fluent approximate comparisons
   */
  public TolerantComparison<Translation2d> isWithin(double tolerance) {
    return new TolerantComparison<Translation2d>() {
      @Override
      public void of(Translation2d expected) {
        x().isWithin(tolerance).of(expected.getX());
        y().isWithin(tolerance).of(expected.getY());
      }
    };
  }

  /** Fails if the {@link Translation2d} is not the zero vector. */
  public void isZero() {
    if (!Translation2d.kZero.equals(actual)) {
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
   * Ensures the actual translation is non-null, failing the assertion if it is null.
   *
   * @return the non-null {@link Translation2d} instance
   */
  private Translation2d nonNullActual() {
    if (actual == null) {
      failWithActual(simpleFact("expected a non-null Translation2d"));
    }
    return actual;
  }
}
