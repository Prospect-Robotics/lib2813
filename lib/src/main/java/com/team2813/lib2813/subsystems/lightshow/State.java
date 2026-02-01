/*
Copyright 2024-2026 Prospect Robotics SWENext Club

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
package com.team2813.lib2813.subsystems.lightshow;

import edu.wpi.first.wpilibj.util.Color;

public interface State {
  /**
   * Gets the color of this State.
   *
   * @return the color of this State
   */
  Color color();

  /**
   * Checks if the current state is active.
   *
   * @return {@code true} if the state is active
   */
  boolean isActive();
}
