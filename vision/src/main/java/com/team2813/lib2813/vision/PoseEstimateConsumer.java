package com.team2813.lib2813.vision;

import org.photonvision.EstimatedRobotPose;

/** Represents an operation that accepts estimated robot positions. */
@FunctionalInterface
public interface PoseEstimateConsumer {
  /**
   * Performs an operation on the given estimated robot positions.
   *
   * @param estimatedPose The estimated robot positions.
   */
  void addEstimatedRobotPose(EstimatedRobotPose estimatedPose);
}
