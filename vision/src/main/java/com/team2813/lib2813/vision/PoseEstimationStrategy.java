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

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import java.util.Optional;
import org.photonvision.EstimatedRobotPose;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.targeting.PhotonPipelineResult;

/**
 * A strategy for computing estimated positions.
 *
 * @since 2.1.1
 */
@FunctionalInterface
public interface PoseEstimationStrategy {

  /** Calculates an estimated position from the given pipeline result. */
  Optional<EstimatedRobotPose> estimate(PhotonPipelineResult cameraResult);

  /**
   * Updates the stored last pose. Useful for setting the initial estimate when using {@link
   * #estimateClosestToLastPose()}.
   *
   * @param lastPose the lastPose to set
   */
  default void setLastPose(Pose3d lastPose) {}

  /**
   * Determines if the estimation strategy requires {@link
   * MultiPhotonPoseEstimator#addHeadingData(double, Rotation2d)} to be called with every frame.
   *
   * @return {@code true} if the estimation strategy is documented to require heading data.
   */
  default boolean requiresHeadingData() {
    return false;
  }

  /** A factory for creating {@link PoseEstimationStrategy} instances. */
  @FunctionalInterface
  interface Factory {
    /** Creates a strategy tht uses the given estimator. */
    PoseEstimationStrategy create(PhotonPoseEstimator estimator);
  }

  /**
   * Creates a factory for a strategy that determines the estimated position of the robot using the
   * target with the lowest position ambiguity from a pipeline result,
   */
  static Factory estimateLowestAmbiguityPose() {
    return estimator -> estimator::estimateLowestAmbiguityPose;
  }

  static Factory estimateClosestToCameraHeightPose() {
    return estimator -> estimator::estimateClosestToCameraHeightPose;
  }

  static Factory estimateAverageBestTargetsPose() {
    return estimator -> estimator::estimateAverageBestTargetsPose;
  }

  /**
   * Creates a factory for a strategy that determines the estimated position of the robot using the
   * target with the lowest delta in the vector magnitude between it and the last pose calculated.
   */
  static Factory estimateClosestToLastPose() {
    return EstimateClosestToLastPose::new;
  }

  /**
   * Creates a factory for a strategy that returns the estimated position of the robot by using all
   * visible tags to compute a single pose estimate on coprocessor. This option needs to be enabled
   * on the PhotonVision web UI as well.
   *
   * @param fallbackStrategyFactory A factory for the strategy to use if fewer than two tags are
   *     visible.
   */
  static Factory estimateCoprocMultiTagPose(Factory fallbackStrategyFactory) {
    return estimator -> new EstimateCoprocMultiTagPose(estimator, fallbackStrategyFactory);
  }

  static Factory estimatePnpDistanceTrigSolvePose() {
    return estimator ->
        new PoseEstimationStrategy() {
          @Override
          public Optional<EstimatedRobotPose> estimate(PhotonPipelineResult cameraResult) {
            return estimator.estimatePnpDistanceTrigSolvePose(cameraResult);
          }

          @Override
          public boolean requiresHeadingData() {
            return true;
          }
        };
  }
}
