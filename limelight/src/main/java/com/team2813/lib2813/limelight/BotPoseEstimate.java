package com.team2813.lib2813.limelight;

import edu.wpi.first.math.geometry.Pose2d;

/**
 * Represents an estimated position for the robot, obtained from the Limelight.
 *
 * @param pose The estimated position of the robot.
 * @param timestampSeconds The timestamp, in seconds, using the drivetrain clock
 */
public record BotPoseEstimate(Pose2d pose, double timestampSeconds) {
}
