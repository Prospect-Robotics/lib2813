/*
Copyright 2024-2026 Prospect Robotics SWENext Club

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
package com.team2813.lib2813.control;

import static java.util.stream.Collectors.toUnmodifiableMap;

import java.util.*;
import java.util.stream.Stream;

public enum InvertType {
  CLOCKWISE(true),
  COUNTER_CLOCKWISE(false),
  FOLLOW_MASTER,
  OPPOSE_MASTER;

  /**
   * A set of all {@link InvertType}s that have a phoenix and spark max invert. Anything that isn't
   * in this set is for motor following
   */
  public static final Set<InvertType> rotationValues =
      Collections.unmodifiableSet(EnumSet.of(CLOCKWISE, COUNTER_CLOCKWISE));

  private final Optional<Boolean> sparkMaxInvert;

  InvertType() {
    sparkMaxInvert = Optional.empty();
  }

  InvertType(boolean sparkMaxInvert) {
    this.sparkMaxInvert = Optional.of(sparkMaxInvert);
  }

  /**
   * Gets an {@link InvertType} from a spark max invert
   *
   * @param v the value to search for
   * @return {@link Optional#empty()} if no {@link InvertType} is found, otherwise, an optional
   *     describing the {@link InvertType}
   */
  public static Optional<InvertType> fromSparkMaxInvert(boolean v) {
    return Optional.of(Maps.sparkMaxMap.get(v));
  }

  public Optional<Boolean> sparkMaxInvert() {
    return sparkMaxInvert;
  }

  private boolean forceSparkMaxInvert() {
    return sparkMaxInvert.orElseThrow();
  }

  /**
   * Contains the maps for {@link InvertType#fromSparkMaxInvert(boolean)}. In a static class so that
   * they will only be initialized if they are needed.
   */
  private static final class Maps {
    private static final Map<Boolean, InvertType> sparkMaxMap =
        Stream.of(InvertType.values())
            .filter((j) -> j.sparkMaxInvert.isPresent())
            .collect(toUnmodifiableMap(InvertType::forceSparkMaxInvert, (j) -> j, (a, b) -> null));
  }
}
