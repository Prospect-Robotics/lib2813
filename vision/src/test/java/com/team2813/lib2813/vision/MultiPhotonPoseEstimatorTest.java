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

import static com.google.common.truth.Truth.assertThat;

import com.team2813.lib2813.testing.junit.jupiter.ProvideUniqueNetworkTableInstance;
import edu.wpi.first.apriltag.AprilTag;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Quaternion;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.networktables.NetworkTableInstance;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.photonvision.PhotonPoseEstimator.PoseStrategy;

/** Tests for {@link MultiPhotonPoseEstimator}. */
@ProvideUniqueNetworkTableInstance
class MultiPhotonPoseEstimatorTest {
  private static final double FIELD_LENGTH = 17.548;
  private static final double FIELD_WIDTH = 8.052;
  private static final int REEFSCAPE_APRIL_TAG_ID = 7;
  private static final Pose3d REEFSCAPE_APRIL_TAG_POSE =
      new Pose3d(
          new Translation3d(13.890498, 4.0259, 0.308102),
          new Rotation3d(new Quaternion(1.0, 0.0, 0.0, 0.0)));
  private static final Transform3d FRONT_CAMERA_TRANSFORM =
      new Transform3d(
          0.1688157406,
          0.2939800826,
          0.1708140348,
          new Rotation3d(0, -0.1745329252, -0.5235987756));

  private static final Camera FRONT_CAMERA = new Camera("front", FRONT_CAMERA_TRANSFORM);

  @ParameterizedTest
  @EnumSource(value = PoseStrategy.class)
  void getPrimaryStrategy(PoseStrategy poseStrategy, NetworkTableInstance ntInstance) {
    try (var estimator =
        MultiPhotonPoseEstimator.builder(ntInstance, createFieldLayout(), poseStrategy)
            .addCamera(FRONT_CAMERA)
            .build()) {
      assertThat(estimator.getPrimaryStrategy()).isEqualTo(poseStrategy);
    }
  }

  private static AprilTagFieldLayout createFieldLayout() {
    List<AprilTag> aprilTags =
        List.of(new AprilTag(REEFSCAPE_APRIL_TAG_ID, REEFSCAPE_APRIL_TAG_POSE));
    return new AprilTagFieldLayout(aprilTags, FIELD_LENGTH, FIELD_WIDTH);
  }
}
