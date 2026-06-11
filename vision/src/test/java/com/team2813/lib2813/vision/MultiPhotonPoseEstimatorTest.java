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
import static com.team2813.lib2813.testing.truth.Pose3dSubject.assertThat;
import static edu.wpi.first.units.Units.Meters;

import com.team2813.lib2813.testing.junit.jupiter.InitWPILib;
import com.team2813.lib2813.testing.junit.jupiter.ProvideUniqueNetworkTableInstance;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.units.measure.Distance;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.photonvision.EstimatedRobotPose;
import org.photonvision.PhotonPoseEstimator.PoseStrategy;
import org.photonvision.simulation.SimCameraProperties;
import org.photonvision.simulation.VisionSystemSim;
import org.photonvision.targeting.PhotonTrackedTarget;

/** Tests for {@link MultiPhotonPoseEstimator}. */
@ProvideUniqueNetworkTableInstance
@InitWPILib
class MultiPhotonPoseEstimatorTest {
  // Place the camera in the center of the robot, ~17.1cm up, facing forward and up.
  private static final Transform3d FRONT_CAMERA_TRANSFORM =
      new Transform3d(0, 0, 0.1708140348, new Rotation3d(0, -0.1745329252, 0));

  private static final Camera FRONT_CAMERA =
      new Camera("front", FRONT_CAMERA_TRANSFORM, SimCameraProperties::PERFECT_90DEG);

  @ParameterizedTest
  @EnumSource(value = PoseStrategy.class)
  void getPrimaryStrategy(PoseStrategy poseStrategy, NetworkTableInstance ntInstance) {
    try (var estimator =
        MultiPhotonPoseEstimator.builder(
                ntInstance, ReefscapeAprilTag.createFieldLayout(), poseStrategy)
            .addCamera(FRONT_CAMERA)
            .build()) {
      assertThat(estimator.getPrimaryStrategy()).isEqualTo(poseStrategy);
    }
  }

  private record PoseTestData(Pose3d robotPose, ReefscapeAprilTag aprilTag) {}

  private static Stream<Arguments> posesInField() {
    return Stream.of(
        facingAprilTag(ReefscapeAprilTag.RED_REEF_CENTER, Meters.of(1)),
        facingAprilTag(ReefscapeAprilTag.BLUE_REEF_CENTER, Meters.of(1)),
        facingAprilTag(ReefscapeAprilTag.RED_PROCESSOR, Meters.of(0.5)),
        facingAprilTag(ReefscapeAprilTag.BLUE_PROCESSOR, Meters.of(0.5)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("posesInField")
  void processAllUnreadResults_estimatedPoseInField(
      String testName, PoseTestData testData, NetworkTableInstance ntInstance) {
    AprilTagFieldLayout fieldLayout = ReefscapeAprilTag.createFieldLayout(testData.aprilTag);
    VisionSystemSim visionSystemSim = new VisionSystemSim("test");
    visionSystemSim.addAprilTags(fieldLayout);

    double z = testData.aprilTag.toAprilTag().pose.getZ();
    Camera camera =
        new Camera(
            "front",
            new Transform3d(0, 0, z, FRONT_CAMERA_TRANSFORM.getRotation()),
            SimCameraProperties::PERFECT_90DEG);

    try (var estimator =
        MultiPhotonPoseEstimator.builder(ntInstance, fieldLayout, PoseStrategy.LOWEST_AMBIGUITY)
            .addCamera(camera)
            .build()) {
      estimator.addCamerasToSimulator(
          visionSystemSim,
          (c, simCamera) -> {
            simCamera.enableRawStream(false);
            simCamera.enableProcessedStream(false);
          });
      visionSystemSim.update(testData.robotPose);

      var estimateCollector = new EstimateCollector();
      var rejectedPoseCollector = new RejectedPoseCollector();

      // Call the method under test
      estimator.processAllUnreadResults(estimateCollector, rejectedPoseCollector);

      assertThat(estimateCollector.estimates).hasSize(1);
      assertThat(rejectedPoseCollector.rejectedPoses).isEmpty();
      assertThat(estimateCollector.estimates.get(0).estimatedPose)
          .isWithin(0.01)
          .of(testData.robotPose);
    }
  }

  private static Stream<Arguments> posesOutOfField() {
    return Stream.of(
        facingAprilTag(ReefscapeAprilTag.RED_REEF_CENTER, Meters.of(4)),
        facingAprilTag(ReefscapeAprilTag.BLUE_REEF_CENTER, Meters.of(4)),
        facingAprilTag(ReefscapeAprilTag.RED_PROCESSOR, Meters.of(8.1)),
        facingAprilTag(ReefscapeAprilTag.BLUE_PROCESSOR, Meters.of(8.1)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("posesOutOfField")
  void processAllUnreadResults_estimatedPoseOutsideField(
      String testName, PoseTestData testData, NetworkTableInstance ntInstance) {
    AprilTagFieldLayout fieldLayout = ReefscapeAprilTag.createFieldLayout(testData.aprilTag);
    VisionSystemSim visionSystemSim = new VisionSystemSim("test");
    visionSystemSim.addAprilTags(fieldLayout);

    double z = testData.aprilTag.toAprilTag().pose.getZ();
    Camera camera =
        new Camera(
            "front",
            new Transform3d(0, 0, z, FRONT_CAMERA_TRANSFORM.getRotation()),
            SimCameraProperties::PERFECT_90DEG);

    try (var estimator =
        MultiPhotonPoseEstimator.builder(ntInstance, fieldLayout, PoseStrategy.LOWEST_AMBIGUITY)
            .addCamera(camera)
            .build()) {
      estimator.addCamerasToSimulator(
          visionSystemSim,
          (c, simCamera) -> {
            simCamera.enableRawStream(false);
            simCamera.enableProcessedStream(false);
          });
      visionSystemSim.update(testData.robotPose);

      var estimateCollector = new EstimateCollector();
      var rejectedPoseCollector = new RejectedPoseCollector();

      // Call the method under test
      estimator.processAllUnreadResults(estimateCollector, rejectedPoseCollector);

      assertThat(estimateCollector.estimates).isEmpty();
      assertThat(rejectedPoseCollector.rejectedPoses).hasSize(1);
      assertThat(targetsUsed(rejectedPoseCollector.rejectedPoses.get(0)))
          .containsExactly(testData.aprilTag.id());
    }
  }

  private static List<Integer> targetsUsed(EstimatedRobotPose estimatedPose) {
    return estimatedPose.targetsUsed.stream().map(PhotonTrackedTarget::getFiducialId).toList();
  }

  /** Creates test data with a position that is the given distance away from the given AprilTag. */
  private static Arguments facingAprilTag(ReefscapeAprilTag tag, Distance distanceFromTag) {
    Pose2d closestTagPose = tag.toAprilTag().pose.toPose2d();
    Rotation2d tagRotation = closestTagPose.getRotation();
    double distance = distanceFromTag.in(Meters);
    Translation2d translation =
        new Translation2d(distance * tagRotation.getCos(), distance * tagRotation.getSin());

    Pose2d robotPose =
        new Pose2d(
            closestTagPose.getTranslation().plus(translation),
            tagRotation.rotateBy(Rotation2d.k180deg));

    var testName = String.format("%.1fmFrom%s", distance, toCamelCase(tag.name()));
    return Arguments.of(testName, new PoseTestData(new Pose3d(robotPose), tag));
  }

  private static String toCamelCase(String s) {
    StringBuilder camelCaseString = new StringBuilder();
    for (String part : s.split("_")) {
      camelCaseString.append(part.substring(0, 1).toUpperCase());
      camelCaseString.append(part.substring(1).toLowerCase());
    }
    return camelCaseString.toString();
  }

  private static class EstimateCollector implements PoseEstimateConsumer<Camera> {
    final List<EstimatedRobotPose> estimates = new ArrayList<>();

    @Override
    public void addEstimatedRobotPose(EstimatedRobotPose estimatedPose, Camera camera) {
      assertThat(camera.name()).isEqualTo(FRONT_CAMERA.name());
      estimates.add(estimatedPose);
    }
  }

  private static class RejectedPoseCollector implements Consumer<EstimatedRobotPose> {
    final List<EstimatedRobotPose> rejectedPoses = new ArrayList<>();

    @Override
    public void accept(EstimatedRobotPose estimatedRobotPose) {
      rejectedPoses.add(estimatedRobotPose);
    }
  }
}
