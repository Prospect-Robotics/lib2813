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
package com.team2813.lib2813.vision;

import org.photonvision.EstimatedRobotPose;

/**
 * Represents an operation that accepts estimated robot positions.
 *
 * @since 2.0.0
 */
@FunctionalInterface
public interface PoseEstimateConsumer {
  /**
   * Performs an operation on the given estimated robot positions.
   *
   * @param estimatedPose The estimated robot positions.
   */
  void addEstimatedRobotPose(EstimatedRobotPose estimatedPose);
}
