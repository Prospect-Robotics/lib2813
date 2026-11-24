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
import java.util.Optional;
import org.photonvision.EstimatedRobotPose;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.targeting.PhotonPipelineResult;

class EstimateClosestToLastPose implements PoseEstimationStrategy {
  private final PhotonPoseEstimator estimator;
  private Pose3d lastPose;

  EstimateClosestToLastPose(PhotonPoseEstimator estimator) {
    this.estimator = estimator;
  }

  @Override
  public Optional<EstimatedRobotPose> estimate(PhotonPipelineResult cameraResult) {
    var estimate = estimator.estimateClosestToReferencePose(cameraResult, lastPose);
    estimate.ifPresent(pose -> setLastPose(pose.estimatedPose));
    return estimate;
  }

  @Override
  public void setLastPose(Pose3d lastPose) {
    this.lastPose = lastPose;
  }
}
