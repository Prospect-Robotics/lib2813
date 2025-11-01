/*
Copyright 2025 Prospect Robotics SWENext Club

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.team2813.lib2813.testing.truth;

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
   * @deprecated {@link Object#equals(Object)} is not supported on TolerantComparison. If you meant
   *     to compare doubles, use {@link #of(T)} instead.
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
