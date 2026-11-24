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

import java.util.Objects;
import java.util.Optional;
import org.photonvision.EstimatedRobotPose;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.targeting.PhotonPipelineResult;

class EstimateCoprocMultiTagPose implements PoseEstimationStrategy {
  private final PhotonPoseEstimator estimator;
  private PoseEstimationStrategy fallbackStrategy;

  EstimateCoprocMultiTagPose(PhotonPoseEstimator estimator, Factory fallbackStrategyFactory) {
    Objects.requireNonNull(fallbackStrategyFactory, "fallbackStrategyFactory cannot be null");
    this.estimator = estimator;
    this.fallbackStrategy = fallbackStrategyFactory.create(estimator);
  }

  void setFallbackStrategy(PoseEstimationStrategy fallbackStrategy) {
    this.fallbackStrategy = fallbackStrategy;
  }

  @Override
  public Optional<EstimatedRobotPose> estimate(PhotonPipelineResult cameraResult) {
    if (cameraResult.getTargets().size() < 2) {
      return fallbackStrategy.estimate(cameraResult);
    }
    return estimator.estimateCoprocMultiTagPose(cameraResult);
  }

  @Override
  public boolean requiresHeadingData() {
    return fallbackStrategy.requiresHeadingData();
  }
}
