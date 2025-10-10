package com.team2813.lib2813.limelight;

import edu.wpi.first.math.geometry.Pose2d;
import java.util.Set;

/**
 * Represents an estimated position of the robot, as reported by a Limelight or other vision system.
 *
 * <p>This record includes the robot's pose, the timestamp of the observation, and a set of
 * currently visible AprilTag IDs.
 *
 * @param pose The estimated position and orientation of the robot in 2D space.
 * @param timestampSeconds The timestamp of this estimate, in seconds, relative to the drivetrain
 *     clock.
 * @param visibleAprilTags The set of AprilTag IDs that were visible when this estimate was made.
 */
public record BotPoseEstimate(Pose2d pose, double timestampSeconds, Set<Integer> visibleAprilTags) {

  /**
   * @deprecated Use the constructor including {@code visibleAprilTags}. This constructor
   *     automatically assigns an empty set of visible AprilTags.
   */
  @Deprecated
  public BotPoseEstimate(Pose2d pose, double timestampSeconds) {
    this(pose, timestampSeconds, Set.of());
  }
}
