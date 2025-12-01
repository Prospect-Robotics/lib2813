/*
Copyright 2025 Prospect Robotics SWENext Club

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
package com.team2813.lib2813.util;

import edu.wpi.first.math.MathUtil;

/** Utility class for comon control-related utility functions. */
public class ControlUtils {
  private ControlUtils() {
    throw new AssertionError(
        "Cannot create ControlUtils instance. Use its static methods directly.");
  }

  /**
   * Deadbands a value.
   *
   * <p>"A deadband or dead-band (also known as a dead zone or a neutral zone) is a band of input
   * values in the domain of a transfer function in a control system or signal processing system
   * where the output is zero (the output is 'dead' - no action occurs).". See <a href=
   * "https://en.wikipedia.org/wiki/Deadband">https://en.wikipedia.org/wiki/Deadband</a>.
   *
   * @see <a href="https://www.desmos.com/calculator/td9m7bff26">an interactive demo</a> of the
   *     deadband operation.
   * @param value The value to deadband, must be in [-1.0, 1.0].
   * @param deadband The deadband range value, must be in [0.0, 1.0).
   * @return The deadbanded value.
   * @throws IllegalArgumentException If the value or deadband is out of bounds.
   * @deprecated Use edu.wpi.first.math.MathUtil.applyDeadband instead.
   */
  @Deprecated(forRemoval = true)
  public static double deadband(double value, double deadband) {
    if (deadband < 0.0 || deadband >= 1.0) {
      throw new IllegalArgumentException(
          "Deadband must be in [0.0, 1.0). Instead, it was " + deadband);
    }
    if (value < -1.0 || value > 1.0) {
      throw new IllegalArgumentException("Value must be in [-1.0, 1.0]. Instead, it was " + value);
    }
    return MathUtil.applyDeadband(value, deadband);
  }
}
