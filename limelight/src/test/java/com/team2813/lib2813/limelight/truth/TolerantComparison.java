package com.team2813.lib2813.limelight.truth;

import org.jspecify.annotations.Nullable;

/**
 * A partially specified check about an approximate relationship to a {@code double} subject using a
 * tolerance.
 */
public abstract class TolerantComparison<T> {

  // Prevent subclassing outside of this package
  TolerantComparison() {}

  /**
   * Fails if the subject was expected to be within the tolerance of the given value but was not.
   * The subject and tolerance are specified earlier in the fluent call chain.
   */
  public abstract void of(T expected);

  // Remaining code copied from DoubleSubject.TolerantDoubleComparison

  /**
   * @throws UnsupportedOperationException always
   * @deprecated {@link Object#equals(Object)} is not supported on TolerantComparison. If
   *     you meant to compare doubles, use {@link #of(T)} instead.
   */
  @Deprecated
  @Override
  public final boolean equals(@Nullable Object o) {
    throw new UnsupportedOperationException(
            "If you meant to compare values, use TolerantComparison.of() instead.");
  }

  /**
   * @throws UnsupportedOperationException always
   * @deprecated {@link Object#hashCode()} is not supported on TolerantComparison
   */
  @Deprecated
  @Override
  public final int hashCode() {
    throw new UnsupportedOperationException("Subject.hashCode() is not supported.");
  }
}
