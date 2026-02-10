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
package com.team2813.lib2813.vendor.ctre;

import com.ctre.phoenix6.signals.InvertedValue;
import com.team2813.lib2813.control.InvertType;
import java.util.Objects;
import java.util.Optional;

/** Utility methods for working with {@link InvertType} for CTRE devices. */
public class InvertTypes {

  /**
   * Gets the {@link InvertedValue} for the given {@link InvertType}
   *
   * @return The {@code InvertedValue}, or {@link Optional#empty()} if the provided {@code
   *     invertType} is not a rotational value.
   */
  public static Optional<InvertedValue> toInvertValue(InvertType invertType) {
    return switch (Objects.requireNonNull(invertType, "invertType should not be null")) {
      case CLOCKWISE -> Optional.of(InvertedValue.Clockwise_Positive);
      case COUNTER_CLOCKWISE -> Optional.of(InvertedValue.CounterClockwise_Positive);
      default -> Optional.empty();
    };
  }

  /** Gets the {@link InvertType} for the given {@link InvertedValue} */
  public static InvertType toInvertType(InvertedValue invertedValue) {
    return switch (Objects.requireNonNull(invertedValue, "invertedValue should not be null")) {
      case Clockwise_Positive -> InvertType.CLOCKWISE;
      case CounterClockwise_Positive -> InvertType.COUNTER_CLOCKWISE;
    };
  }

  private InvertTypes() {
    throw new AssertionError("Not instantiable");
  }
}
