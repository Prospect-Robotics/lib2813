package com.team2813.lib2813.util;

/**
 * Utility class for converting between different rotational units used in FRC robots.
 *
 * <p>This class provides static methods to convert between encoder ticks, motor revolutions, and
 * wheel revolutions, taking into account the counts per revolution (CPR) and gear ratios.
 */
public final class Units2813 {

  // Prevent instantiation
  private Units2813() {
    throw new AssertionError("Units2813 is non-instantiable");
  }

  /**
   * Converts encoder ticks to motor revolutions.
   *
   * @param ticks the number of encoder ticks
   * @param cpr the counts per revolution of the encoder
   * @return the equivalent motor revolutions
   */
  public static double ticksToMotorRevs(double ticks, int cpr) {
    return ticks / cpr;
  }

  /**
   * Converts motor revolutions to encoder ticks.
   *
   * @param revs the number of motor revolutions
   * @param cpr the counts per revolution of the encoder
   * @return the equivalent number of encoder ticks
   */
  public static int motorRevsToTicks(double revs, int cpr) {
    return (int) (revs * cpr);
  }

  /**
   * Converts motor revolutions to wheel revolutions using a gear ratio.
   *
   * @param revs the number of motor revolutions
   * @param gearRatio the ratio of motor revolutions to wheel revolutions
   * @return the equivalent wheel revolutions
   */
  public static double motorRevsToWheelRevs(double revs, double gearRatio) {
    return revs * gearRatio;
  }

  /**
   * Converts wheel revolutions to motor revolutions using a gear ratio.
   *
   * @param revs the number of wheel revolutions
   * @param gearRatio the ratio of motor revolutions to wheel revolutions
   * @return the equivalent motor revolutions
   */
  public static double wheelRevsToMotorRevs(double revs, double gearRatio) {
    return revs / gearRatio;
  }
}
