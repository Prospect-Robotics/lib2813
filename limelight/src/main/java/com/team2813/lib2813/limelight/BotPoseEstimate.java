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
public record BotPoseEstimate(Pose2d pose, double timestampSeconds, Set<Integer> visibleAprilTags) {

  @Deprecated
  public BotPoseEstimate(Pose2d pose, double timestampSeconds) {
    this(pose, timestampSeconds, Set.of());
  }
}
