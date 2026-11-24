/*
Copyright 2026 Prospect Robotics SWENext Club

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
package com.team2813.lib2813.robot;

import java.util.function.Consumer;

/**
 * Service for registering callbacks that need to run periodically.
 *
 * <p>>Subsystems can extend {@link com.team2813.lib2813.subsystems.ModularSubsystem} to get access
 * to an instance of {@link PeriodicRegistry}. This will ensure that time spent in the periodic
 * methods are associated with the subsystem.
 */
public interface PeriodicRegistry {

  /**
   * Add a callback to run for each iteration of the event loop.
   *
   * @param callback The callback to run.
   */
  void addPeriodic(Consumer<RobotState> callback);

  /**
   * Add a callback to run in simulation mode for each iteration of the event loop.
   *
   * @param callback The callback to run.
   */
  void addSimulationPeriodic(Consumer<RobotState> callback);
}
