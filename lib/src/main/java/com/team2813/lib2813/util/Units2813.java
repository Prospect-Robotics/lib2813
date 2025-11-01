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
package com.team2813.lib2813.util;

public class Units2813 {
  private Units2813() {
    throw new AssertionError("non-instantiable");
  }

  public static double ticksToMotorRevs(double ticks, int cpr) {
    return ticks / cpr;
  }

  public static int motorRevsToTicks(double revs, int cpr) {
    return (int) (revs * cpr);
  }

  public static double motorRevsToWheelRevs(double revs, double gearRatio) {
    return revs * gearRatio;
  }

  public static double wheelRevsToMotorRevs(double revs, double gearRatio) {
    return revs / gearRatio;
  }
}
