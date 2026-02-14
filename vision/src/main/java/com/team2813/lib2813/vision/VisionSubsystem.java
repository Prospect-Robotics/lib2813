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
;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import org.photonvision.EstimatedRobotPose;

/**
 * Defines a vision subsystem.
 *
 * @param <C> the type for the camera
 * @since 2.1.0
 */
public abstract class VisionSubsystem<C extends Camera> extends SubsystemBase {
  protected final MultiPhotonPoseEstimator<C> poseEstimator;

  /**
   * Constructor.
   *
   * @param poseEstimator The pose estimator to use.
   */
  protected VisionSubsystem(MultiPhotonPoseEstimator<C> poseEstimator) {
    this.poseEstimator = poseEstimator;
  }

  @Override
  public void periodic() {
    if (poseEstimator.poseStrategyRequiresHeadingData()) {
      poseEstimator.addHeadingData(Timer.getTimestamp(), getRotation3d());
    }
    poseEstimator.processAllUnreadResults(this::handleEstimatedPose, this::handleRejectedPose);
    poseEstimator.publishCameraPosesRelativeTo(getDrivetrainPose());
  }

  /**
   * Gets the current orientation of the robot as a {@link Rotation3d} from the Pigeon 2 quaternion
   * values.
   *
   * @return The robot orientation.
   */
  protected abstract Rotation3d getRotation3d();

  /**
   * Gets the current pose of the robot as a {@link Pose2d} from the drivetrain.
   *
   * @return The robot position.
   */
  protected abstract Pose2d getDrivetrainPose();

  /**
   * Called when new estimated robot positions are available.
   *
   * @param estimatedPose The estimated robot position.
   * @param camera The camera.
   */
  protected abstract void handleEstimatedPose(EstimatedRobotPose estimatedPose, C camera);

  /**
   * Called when invalid robot positions were computed by the estimator.
   *
   * @param rejectedPose The rejected estimated robot position.
   */
  protected void handleRejectedPose(EstimatedRobotPose rejectedPose) {}
}
