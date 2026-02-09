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
package com.team2813.lib2813.vendor.rev;

import com.team2813.lib2813.control.InvertType;
import java.util.Objects;
import java.util.Optional;

/** Utility methods for working with {@link InvertType} for REV Robotics motors. */
public class InvertTypes {

  /**
   * Gets the SPARK MAX invert value for the given {@link InvertType}
   *
   * @return The invert, or {@link Optional#empty()} if the provided {@code invertType} is not a
   *     rotational value.
   */
  public static Optional<Boolean> toSparkMaxInvert(InvertType invertType) {
    return switch (Objects.requireNonNull(invertType, "invertType should not be null")) {
      case CLOCKWISE -> Optional.of(Boolean.TRUE);
      case COUNTER_CLOCKWISE -> Optional.of(Boolean.FALSE);
      default -> Optional.empty();
    };
  }

  /** Gets the {@link InvertType} for the given SPARK MAX invert value. */
  public static InvertType toInvertType(boolean sparkMaxInvert) {
    return sparkMaxInvert ? InvertType.CLOCKWISE : InvertType.COUNTER_CLOCKWISE;
  }

  private InvertTypes() {
    throw new AssertionError("Not instantiable");
  }
}
