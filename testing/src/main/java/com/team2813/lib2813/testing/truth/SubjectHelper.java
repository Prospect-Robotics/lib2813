/*
Copyright 2026 Prospect Robotics SWENext Club

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

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Double.doubleToLongBits;

class SubjectHelper {
  private static final long NEG_ZERO_BITS = doubleToLongBits(-0.0);

  /**
   * Ensures that the given tolerance is a non-negative finite value, i.e. not {@code Double.NaN},
   * {@code Double.POSITIVE_INFINITY}, or negative, including {@code -0.0}.
   */
  static void checkTolerance(double tolerance) {
    checkArgument(!Double.isNaN(tolerance), "tolerance cannot be NaN");
    checkArgument(tolerance >= 0.0, "tolerance (%s) cannot be negative", tolerance);
    checkArgument(
        doubleToLongBits(tolerance) != NEG_ZERO_BITS,
        "tolerance (%s) cannot be negative",
        tolerance);
    checkArgument(tolerance != Double.POSITIVE_INFINITY, "tolerance cannot be POSITIVE_INFINITY");
  }

  private SubjectHelper() {
    throw new AssertionError("Not instantiable");
  }
}
