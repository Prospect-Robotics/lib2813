package com.team2813.lib2813.control;

import static java.util.stream.Collectors.toUnmodifiableMap;

import com.ctre.phoenix6.signals.InvertedValue;
import java.util.*;
import java.util.stream.Stream;

/**
 * Enumeration defining motor inversion types for standardized motor control across different
 * vendors.
 *
 * <p>This enum provides a vendor-neutral abstraction for motor inversion settings while maintaining
 * compatibility with specific hardware implementations from CTRE Phoenix and REV Robotics SPARK
 * controllers. It handles both absolute rotation directions and relative following behaviors for
 * multi-motor systems.
 *
 * <p>The enum supports two categories of inversion:
 *
 * <ul>
 *   <li><b>Rotation Values:</b> Absolute directional settings (CLOCKWISE, COUNTER_CLOCKWISE) that
 *       define the positive direction of motor rotation
 *   <li><b>Following Values:</b> Relative settings (FOLLOW_MASTER, OPPOSE_MASTER) used for follower
 *       motors that reference a primary motor's direction
 * </ul>
 *
 * <p>Each rotation value includes mappings to vendor-specific inversion settings, enabling seamless
 * integration with different motor controller families without code changes.
 *
 * @author Team 2813
 * @since 1.0
 */
public enum InvertType {

  /**
   * Clockwise rotation is considered positive.
   *
   * <p>When this inversion type is applied, positive motor output values will cause the motor to
   * rotate in the clockwise direction when viewed from the output shaft end. This is the "normal"
   * or "non-inverted" direction for most motor controllers.
   *
   * <p>Vendor mappings:
   *
   * <ul>
   *   <li>Phoenix 6: {@code InvertedValue.Clockwise_Positive}
   *   <li>SPARK MAX: {@code true} (inverted = false)
   * </ul>
   */
  CLOCKWISE(InvertedValue.Clockwise_Positive, true),

  /**
   * Counter-clockwise rotation is considered positive.
   *
   * <p>When this inversion type is applied, positive motor output values will cause the motor to
   * rotate in the counter-clockwise direction when viewed from the output shaft end. This
   * effectively inverts the motor's output relative to the default.
   *
   * <p>Vendor mappings:
   *
   * <ul>
   *   <li>Phoenix 6: {@code InvertedValue.CounterClockwise_Positive}
   *   <li>SPARK MAX: {@code false} (inverted = true)
   * </ul>
   */
  COUNTER_CLOCKWISE(InvertedValue.CounterClockwise_Positive, false),

  /**
   * Follower motor matches the master motor's direction.
   *
   * <p>This inversion type is used for follower motors that should rotate in the same direction as
   * their master motor. The follower will mirror the master's output without any inversion applied.
   *
   * <p><b>Note:</b> This value has no direct vendor mapping as it represents a relationship rather
   * than an absolute direction. It is resolved at the follower configuration level.
   */
  FOLLOW_MASTER,

  /**
   * Follower motor opposes the master motor's direction.
   *
   * <p>This inversion type is used for follower motors that should rotate opposite to their master
   * motor. The follower will mirror the master's output but with inversion applied, creating
   * opposing rotation.
   *
   * <p>This is commonly used in differential drive systems where left and right motors need to
   * rotate in opposite directions for forward motion.
   *
   * <p><b>Note:</b> This value has no direct vendor mapping as it represents a relationship rather
   * than an absolute direction. It is resolved at the follower configuration level.
   */
  OPPOSE_MASTER;

  /**
   * A set of all {@link InvertType}s that have phoenix and spark max invert mappings.
   *
   * <p>This set contains only the rotation values (CLOCKWISE, COUNTER_CLOCKWISE) that can be
   * directly mapped to vendor-specific inversion settings. Any InvertType not in this set is
   * intended for motor following relationships and does not have direct vendor mappings.
   *
   * <p>This set is used for validation to ensure only appropriate inversion types are used in
   * contexts that require absolute rotation directions.
   */
  public static final Set<InvertType> rotationValues =
      Collections.unmodifiableSet(EnumSet.of(CLOCKWISE, COUNTER_CLOCKWISE));

  /** The corresponding Phoenix 6 InvertedValue, if applicable */
  private final Optional<InvertedValue> phoenixInvert;

  /** The corresponding SPARK MAX inversion boolean, if applicable */
  private final Optional<Boolean> sparkMaxInvert;

  /**
   * Creates an InvertType for following relationships (no vendor mappings).
   *
   * <p>This constructor is used for FOLLOW_MASTER and OPPOSE_MASTER values that represent
   * relationships rather than absolute directions.
   */
  InvertType() {
    phoenixInvert = Optional.empty();
    sparkMaxInvert = Optional.empty();
  }

  /**
   * Creates an InvertType with vendor-specific mappings.
   *
   * <p>This constructor is used for rotation values (CLOCKWISE, COUNTER_CLOCKWISE) that have direct
   * mappings to vendor-specific inversion settings.
   *
   * @param phoenixInvert the corresponding Phoenix 6 InvertedValue
   * @param sparkMaxInvert the corresponding SPARK MAX inversion setting
   */
  InvertType(InvertedValue phoenixInvert, boolean sparkMaxInvert) {
    this.phoenixInvert = Optional.of(phoenixInvert);
    this.sparkMaxInvert = Optional.of(sparkMaxInvert);
  }

  /**
   * Gets an {@link InvertType} from a Phoenix 6 {@link InvertedValue}.
   *
   * <p>This method provides reverse lookup from vendor-specific Phoenix settings to the
   * vendor-neutral InvertType representation. It enables conversion from Phoenix-specific code to
   * the standardized enum.
   *
   * @param v the {@link InvertedValue} to search for
   * @return an Optional containing the corresponding {@link InvertType}, or {@link
   *     Optional#empty()} if no mapping exists
   */
  public static Optional<InvertType> fromPhoenixInvert(InvertedValue v) {
    return Optional.ofNullable(Maps.phoenixMap.get(v));
  }

  /**
   * Gets an {@link InvertType} from a SPARK MAX inversion boolean.
   *
   * <p>This method provides reverse lookup from vendor-specific SPARK MAX settings to the
   * vendor-neutral InvertType representation. It enables conversion from SPARK MAX-specific code to
   * the standardized enum.
   *
   * @param v the SPARK MAX inversion boolean to search for
   * @return an Optional containing the corresponding {@link InvertType}, or {@link
   *     Optional#empty()} if no mapping exists
   */
  public static Optional<InvertType> fromSparkMaxInvert(boolean v) {
    return Optional.ofNullable(Maps.sparkMaxMap.get(v));
  }

  /**
   * Gets the Phoenix 6 InvertedValue for this InvertType.
   *
   * <p>This method returns the vendor-specific Phoenix mapping for rotation values. Following
   * values (FOLLOW_MASTER, OPPOSE_MASTER) will return an empty Optional as they don't have direct
   * Phoenix mappings.
   *
   * @return an Optional containing the Phoenix InvertedValue, or empty if not applicable
   */
  public Optional<InvertedValue> phoenixInvert() {
    return phoenixInvert;
  }

  /**
   * Forces retrieval of the Phoenix 6 InvertedValue.
   *
   * <p>This internal method assumes the Phoenix mapping exists and throws if it doesn't. It's used
   * internally for map construction and should not be called on following values.
   *
   * @return the Phoenix InvertedValue
   * @throws NoSuchElementException if no Phoenix mapping exists
   */
  private InvertedValue forcePhoenixInvert() {
    return phoenixInvert.orElseThrow();
  }

  /**
   * Gets the SPARK MAX inversion boolean for this InvertType.
   *
   * <p>This method returns the vendor-specific SPARK MAX mapping for rotation values. Following
   * values (FOLLOW_MASTER, OPPOSE_MASTER) will return an empty Optional as they don't have direct
   * SPARK MAX mappings.
   *
   * @return an Optional containing the SPARK MAX inversion boolean, or empty if not applicable
   */
  public Optional<Boolean> sparkMaxInvert() {
    return sparkMaxInvert;
  }

  /**
   * Forces retrieval of the SPARK MAX inversion boolean.
   *
   * <p>This internal method assumes the SPARK MAX mapping exists and throws if it doesn't. It's
   * used internally for map construction and should not be called on following values.
   *
   * @return the SPARK MAX inversion boolean
   * @throws NoSuchElementException if no SPARK MAX mapping exists
   */
  private boolean forceSparkMaxInvert() {
    return sparkMaxInvert.orElseThrow();
  }

  /**
   * Contains the reverse lookup maps for vendor-specific inversion values.
   *
   * <p>This static nested class holds the maps used by {@link #fromPhoenixInvert(InvertedValue)}
   * and {@link #fromSparkMaxInvert(boolean)}. The maps are constructed lazily in a static context
   * to avoid initialization overhead when reverse lookup is not needed.
   *
   * <p>The maps are built by filtering enum values to include only those with vendor mappings and
   * creating reverse mappings from vendor values to InvertType.
   */
  private static final class Maps {

    /** Reverse lookup map from Phoenix InvertedValue to InvertType */
    private static final Map<InvertedValue, InvertType> phoenixMap =
        Stream.of(InvertType.values())
            .filter((j) -> j.phoenixInvert.isPresent())
            .collect(toUnmodifiableMap(InvertType::forcePhoenixInvert, (j) -> j, (a, b) -> null));

    /** Reverse lookup map from SPARK MAX boolean to InvertType */
    private static final Map<Boolean, InvertType> sparkMaxMap =
        Stream.of(InvertType.values())
            .filter((j) -> j.sparkMaxInvert.isPresent())
            .collect(toUnmodifiableMap(InvertType::forceSparkMaxInvert, (j) -> j, (a, b) -> null));
  }
}
