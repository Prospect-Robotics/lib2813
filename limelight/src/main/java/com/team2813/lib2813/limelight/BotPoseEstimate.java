package com.team2813.lib2813.limelight;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;

import java.util.Map;

/**
 * Represents an estimated position for the robot, obtained from the Limelight.
 *
 * @param pose The estimated position of the robot.
 * @param timestampSeconds The timestamp, in seconds, using the drivetrain clock
 * @param visibleAprilTagPoses All April Tags that are visible from the vision source.
 */
public record BotPoseEstimate(Pose2d pose, double timestampSeconds, Map<Integer, Pose3d> visibleAprilTagPoses) {

  @Deprecated
  public BotPoseEstimate(Pose2d pose, double timestampSeconds) {
    this(pose, timestampSeconds, Map.of());
  }
}
