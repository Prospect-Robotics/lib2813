package com.team2813.lib2813.control;

import static java.util.stream.Collectors.toUnmodifiableMap;

import com.ctre.phoenix6.signals.InvertedValue;
import java.util.*;
import java.util.stream.Stream;

public enum InvertType {
  CLOCKWISE(InvertedValue.Clockwise_Positive, true),
  COUNTER_CLOCKWISE(InvertedValue.CounterClockwise_Positive, false),
  FOLLOW_MASTER,
  OPPOSE_MASTER;

  /**
   * A set of all {@link InvertType}s that have a phoenix and spark max invert. Anything that isn't
   * in this set is for motor following
   */
  public static final Set<InvertType> rotationValues =
      Collections.unmodifiableSet(EnumSet.of(CLOCKWISE, COUNTER_CLOCKWISE));

  private final Optional<InvertedValue> phoenixInvert;
  private final Optional<Boolean> sparkMaxInvert;

  InvertType() {
    phoenixInvert = Optional.empty();
    sparkMaxInvert = Optional.empty();
  }

  InvertType(InvertedValue phoenixInvert, boolean sparkMaxInvert) {
    this.phoenixInvert = Optional.of(phoenixInvert);
    this.sparkMaxInvert = Optional.of(sparkMaxInvert);
  }

  /**
   * Gets an {@link InvertType} from a phoenix {@link InvertedValue}.
   *
   * @param v the {@link InvertedValue} to search for
   * @return {@link Optional#empty()} if no {@link InvertType} is found, otherwise, an optional
   *     describing the {@link InvertType}
   */
  public static Optional<InvertType> fromPhoenixInvert(InvertedValue v) {
    return Optional.of(Maps.phoenixMap.get(v));
  }

  /**
   * Gets an {@link InvertType} from a spark max invert
   *
   * @param v the {@link InvertedValue} to search for
   * @return {@link Optional#empty()} if no {@link InvertType} is found, otherwise, an optional
   *     describing the {@link InvertType}
   */
  public static Optional<InvertType> fromSparkMaxInvert(boolean v) {
    return Optional.of(Maps.sparkMaxMap.get(v));
  }

  public Optional<InvertedValue> phoenixInvert() {
    return phoenixInvert;
  }

  private InvertedValue forcePhoenixInvert() {
    return phoenixInvert.orElseThrow();
  }

  public Optional<Boolean> sparkMaxInvert() {
    return sparkMaxInvert;
  }

  private boolean forceSparkMaxInvert() {
    return sparkMaxInvert.orElseThrow();
  }

  /**
   * Contains the maps for {@link InvertType#fromPhoenixInvert(InvertedValue)} and {@link
   * InvertType#fromSparkMaxInvert(boolean)}. In a static class so that they will only be
   * initialized if they are needed.
   */
  private static final class Maps {
    private static final Map<InvertedValue, InvertType> phoenixMap =
        Stream.of(InvertType.values())
            .filter((j) -> j.phoenixInvert.isPresent())
            .collect(toUnmodifiableMap(InvertType::forcePhoenixInvert, (j) -> j, (a, b) -> null));
    private static final Map<Boolean, InvertType> sparkMaxMap =
        Stream.of(InvertType.values())
            .filter((j) -> j.sparkMaxInvert.isPresent())
            .collect(toUnmodifiableMap(InvertType::forceSparkMaxInvert, (j) -> j, (a, b) -> null));
  }
}
