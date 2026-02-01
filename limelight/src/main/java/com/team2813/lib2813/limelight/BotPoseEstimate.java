/*
Copyright 2025-2026 Prospect Robotics SWENext Club

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

import edu.wpi.first.math.geometry.Pose2d;
import java.util.Set;

/**
 * Represents an estimated position for the robot, obtained from the Limelight.
 *
 * @param pose The estimated position of the robot.
 * @param timestampSeconds The timestamp, in seconds, using the drivetrain clock
 * @param visibleAprilTags All April Tags that are visible from the vision source.
 */
public record BotPoseEstimate(
    Pose2d pose, double timestampSeconds, Set<Integer> visibleAprilTags) {}
