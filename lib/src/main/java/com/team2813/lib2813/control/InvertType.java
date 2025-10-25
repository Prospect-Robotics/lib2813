package com.team2813.lib2813.control;

import static java.util.stream.Collectors.toUnmodifiableMap;

import com.ctre.phoenix6.signals.InvertedValue;
import java.util.*;
import java.util.stream.Stream;

/**
 * Unified inversion semantics across different motor controller families.
 *
 * <p>This enum provides two distinct inversion modes:
 *
 * <ul>
 *   <li><b>Absolute direction:</b> CLOCKWISE/COUNTER_CLOCKWISE define motor direction independent
 *       of leader state. Used for standalone motors or when precise control over follower direction
 *       is needed.
 *   <li><b>Relative direction:</b> FOLLOW_MASTER/OPPOSE_MASTER define direction relative to the
 *       leader motor. Simplifies configuration when followers should mirror or oppose leader
 *       behavior regardless of leader inversion.
 * </ul>
 *
 * <p>The lazy-initialized Maps class uses a holder pattern to defer reverse-mapping construction
 * until first use, avoiding unnecessary computation if conversions are never needed.
 */
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

  /** Constructor for relative inversion types (no hardware mapping). */
  InvertType() {
    phoenixInvert = Optional.empty();
    sparkMaxInvert = Optional.empty();
  }

  /**
   * Constructor for absolute direction types.
   *
   * @param phoenixInvert CTRE Phoenix 6 inversion value
   * @param sparkMaxInvert REV Spark Max inversion boolean
   */
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

  /**
   * @return Phoenix inversion value if this is an absolute direction type
   */
  public Optional<InvertedValue> phoenixInvert() {
    return phoenixInvert;
  }

  /**
   * @return Phoenix inversion, throwing if not present (internal use only)
   */
  private InvertedValue forcePhoenixInvert() {
    return phoenixInvert.orElseThrow();
  }

  /**
   * @return Spark Max inversion value if this is an absolute direction type
   */
  public Optional<Boolean> sparkMaxInvert() {
    return sparkMaxInvert;
  }

  /**
   * @return Spark Max inversion, throwing if not present (internal use only)
   */
  private boolean forceSparkMaxInvert() {
    return sparkMaxInvert.orElseThrow();
  }

  /**
   * Lazy-initialized reverse lookup maps using the holder pattern.
   *
   * <p>Maps are constructed only when first accessed, avoiding overhead if
   * fromPhoenixInvert/fromSparkMaxInvert are never called. The merge function (a, b) -> null
   * handles the impossible case of duplicate keys, which cannot occur given the enum definition.
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
