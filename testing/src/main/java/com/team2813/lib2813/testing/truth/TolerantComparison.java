package com.team2813.lib2813.testing.truth;

import org.jspecify.annotations.Nullable;

/**
 * Represents a partially specified check on a subject of type {@code T} for approximate equality
 * within a given tolerance. This class is intended to be used as part of a fluent assertion API,
 * where the subject and tolerance are defined earlier in the chain, and the expected value is
 * provided via {@link #of(Object)}.
 *
 * <p>For example, in a fluent assertion style:
 * <pre>{@code
 * assertThat(actualValue).isWithin(tolerance).of(expectedValue);
 * }</pre>
 *
 * <p>Subclasses of this class implement the specific logic for comparing the subject with the
 * expected value considering the tolerance.
 *
 * @param <T> the type of the value being compared (typically {@link Double})
 */
public abstract class TolerantComparison<T> {

  /**
   * Package-private constructor to prevent subclassing outside of this package.
   */
  TolerantComparison() {}

  /**
   * Fails the assertion if the subject is not within the tolerance of the given expected value.
   * The subject and tolerance must have been specified earlier in the fluent call chain.
   *
   * @param expected the value the subject is expected to be approximately equal to
   */
  public abstract void of(T expected);

  /**
   * {@inheritDoc}
   *
   * <p>This method is unsupported for {@code TolerantComparison}. Equality comparisons should be
   * performed via {@link #of(Object)} rather than {@link Object#equals(Object)}.
   *
   * @throws UnsupportedOperationException always
   * @deprecated Use {@link #of(Object)} to compare values. {@link Object#equals(Object)} is not
   *     supported.
   */
  @Deprecated
  @Override
  public final boolean equals(@Nullable Object o) {
    throw new UnsupportedOperationException(
        "If you meant to compare values, use TolerantComparison.of() instead.");
  }

  /**
   * {@inheritDoc}
   *
   * <p>This method is unsupported for {@code TolerantComparison}.
   *
   * @throws UnsupportedOperationException always
   * @deprecated {@link Object#hashCode()} is not supported on {@code TolerantComparison}.
   */
  @Deprecated
  @Override
  public final int hashCode() {
    throw new UnsupportedOperationException("Subject.hashCode() is not supported.");
  }
}
