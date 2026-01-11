/*
Copyright 2024-2025 Prospect Robotics SWENext Club

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
package com.team2813.lib2813.limelight;

import edu.wpi.first.math.geometry.Pose3d;
import java.util.Map;
import java.util.Optional;

/**
 * Get positional data from limelight
 *
 * @see Limelight
 */
public interface LocationalData {

  /** Returns {@code true} if the limelight has identified a target. */
  boolean hasTarget();

  /** Returns {@code true} if the limelight has provided a valid response. */
  boolean isValid();

  /**
   * Gets the position of the robot with the center of the field as the origin.
   *
   * @return The position of the robot
   */
  Optional<Pose3d> getBotpose();

  /** Gets the estimated position of the robot with the center of the field as the origin. */
  Optional<BotPoseEstimate> getBotPoseEstimate();

  /**
   * Gets the position of the robot with the blue driverstation as the origin
   *
   * @return The position of the robot
   */
  Optional<Pose3d> getBotposeBlue();

  /** Gets the estimated position of the robot with the blue driverstation as the origin. */
  Optional<BotPoseEstimate> getBotPoseEstimateBlue();

  /**
   * Gets the position of the robot with the red driverstation as the origin
   *
   * @return The position of the robot
   */
  Optional<Pose3d> getBotposeRed();

  /** Gets the estimated position of the robot with the red driverstation as the origin. */
  Optional<BotPoseEstimate> getBotPoseEstimateRed();

  /** Gets the visible AprilTags as a map from ID to position. */
  Map<Integer, Pose3d> getVisibleAprilTagPoses();
}
