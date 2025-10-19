/*
Copyright 2023-2025 Prospect Robotics SWENext Club

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
