/*
Copyright 2023-2026 Prospect Robotics SWENext Club

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

import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;

/** Specifies a device that can perceive rotational positions. */
public interface Encoder {
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
   */
  void setPosition(Angle position);

  /**
   * Gets the velocity of the encoder
   *
   * @return The velocity as a measure
   */
  AngularVelocity getVelocityMeasure();
}
