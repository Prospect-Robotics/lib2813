package com.team2813.lib2813.control;

import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;

/** Specifies a device that can perceive rotational positions. */
public interface Encoder {
  /**
   * Gets the position of the encoder
   *
   * @return the position of the encoder
   * @deprecated This method does not specify position in a specific measurement, so it is not safe
   *     to use. Use {@link #getPositionMeasure()} instead
   */
  @Deprecated(forRemoval = true)
  double position();

  /**
   * Gets the position of the encoder
   *
   * @return the position of the encoder as a measure
   */
  Angle getPositionMeasure();

  /**
   * Sets the position of the encoder
   *
   * @param position the position of the encoder
   * @deprecated This method does not specify a unit, so it is not safe to use. Use {@link
   *     #setPosition(Angle)} instead.
   */
  @Deprecated(forRemoval = true)
  void setPosition(double position);

  default void setPosition(Angle position) {
    setPosition(position.in(Units.Radians));
  }

  /**
   * Gets the velocity of the encoder
   *
   * @return the velocity that the encoder perceives
   * @deprecated This method does not specify velocity in a specific measurement, so it is not safe
   *     to use. Use {@link #getVelocityMeasure()} instead
   */
  @Deprecated(forRemoval = true)
  double getVelocity();

  /**
   * Gets the velocity of the encoder
   *
   * @return The velocity as a measure
   */
  default AngularVelocity getVelocityMeasure() {
    return Units.RadiansPerSecond.of(getVelocity());
  }
}
