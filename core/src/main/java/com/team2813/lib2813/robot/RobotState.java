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

/** Provides APIs for getting the current state of the robot. */
public interface RobotState {

  /**
   * Determines if the Robot is currently enabled.
   *
   * @return {@code True} if the Robot is currently enabled by the Driver Station.
   */
  boolean isEnabled();

  /**
   * Determines if the Robot is currently disabled.
   *
   * @return {@code True} if the Robot is currently disabled by the Driver Station.
   */
  default boolean isDisabled() {
    return !isEnabled();
  }

  /**
   * Determines if the robot is currently in Autonomous mode as determined by the Driver Station.
   *
   * @return {@code True} if the robot is currently operating autonomously.
   */
  boolean isAutonomous();

  /**
   * Determine sif the robot is currently in Test mode as determined by the Driver Station.
   *
   * @return {@code True} if the robot is currently operating in Test mode.
   */
  boolean isTest();

  /**
   * Determine if the robot is currently in Operator Control mode as determined by the Driver
   * Station.
   *
   * @return {@code True} if the robot is currently operating in Operator Control mode.
   */
  boolean isTeleop();
}
