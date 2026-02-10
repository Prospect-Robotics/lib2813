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
package com.team2813.lib2813.vendor.ctre;

import com.ctre.phoenix6.StatusCode;
import edu.wpi.first.wpilibj.DriverStation;
import java.util.function.Supplier;

public class PhoenixUtils {
  private static final int ATTEMPTS = 10;

  public static void phoenix6Config(Supplier<StatusCode> configMethod) {
    StatusCode errorCode = configMethod.get();
    for (int i = 1; i <= ATTEMPTS && errorCode != StatusCode.OK; i++) {
      DriverStation.reportError(
          String.format("%s: Config Attempt %d Failed", errorCode.toString(), i), false);
      errorCode = configMethod.get();
    }
    if (errorCode != StatusCode.OK) {
      DriverStation.reportError(String.format("%s: Config Failed", errorCode.toString()), false);
    }
  }

  private PhoenixUtils() {
    throw new AssertionError("Not instantiable");
  }
}
