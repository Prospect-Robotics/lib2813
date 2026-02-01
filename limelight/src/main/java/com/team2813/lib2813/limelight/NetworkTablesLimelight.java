/*
Copyright 2025 Prospect Robotics SWENext Club

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
package com.team2813.lib2813.limelight;

import static java.util.Collections.unmodifiableMap;

import com.team2813.lib2813.limelight.LimelightHelpers.LimelightResults;
import com.team2813.lib2813.limelight.LimelightHelpers.PoseEstimate;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;

class NetworkTablesLimelight implements Limelight {
  private static final double[] ZEROS = new double[6];
  private final String limelightName;
  private final AprilTagMapPoseHelper aprilTagMapPoseHelper;

  NetworkTablesLimelight(String limelightName) {
    this.limelightName = limelightName;
    aprilTagMapPoseHelper = new AprilTagMapPoseHelper(new LimelightClient(limelightName));
  }

  @Override
  public void setFieldMap(InputStream stream, boolean updateLimelight) throws IOException {
    // The updateLimelight assumes we have the hostname of the limelight, which we don't. For now,
    // this won't update the limelight.
    aprilTagMapPoseHelper.setFieldMap(stream, false);
  }

  @Override
  public LocationalData getLocationalData() {
    LimelightHelpers.LimelightResults results = LimelightHelpers.getLatestResults(limelightName);
    if (results.error == null && results.valid) {
      Map<Integer, Pose3d> aprilTags = getVisibleAprilTagPoses(results);
      var poseEstimate =
          toBotPoseEstimate(LimelightHelpers.getBotPoseEstimate(limelightName), aprilTags.keySet());
      var redPoseEstimate =
          toBotPoseEstimate(
              LimelightHelpers.getBotPoseEstimate_wpiRed(limelightName), aprilTags.keySet());
      var bluePoseEstimate =
          toBotPoseEstimate(
              LimelightHelpers.getBotPoseEstimate_wpiBlue(limelightName), aprilTags.keySet());
      return new NTLocationalData(
          results, poseEstimate, redPoseEstimate, bluePoseEstimate, aprilTags);
    }
    return StubLocationalData.INVALID;
  }

  private static Optional<BotPoseEstimate> toBotPoseEstimate(
      PoseEstimate estimate, Set<Integer> visibleAprilTags) {
    if (estimate == null || estimate.tagCount == 0 || Pose2d.kZero.equals(estimate.pose)) {
      return Optional.empty();
    }
    return Optional.of(
        new BotPoseEstimate(estimate.pose, estimate.timestampSeconds, visibleAprilTags));
  }

  private Map<Integer, Pose3d> getVisibleAprilTagPoses(LimelightResults results) {
    Map<Integer, Pose3d> map = new HashMap<>();
    for (var fiducial : results.targets_Fiducials) {
      int id = (int) fiducial.fiducialID;
      aprilTagMapPoseHelper.getTagPose(id).ifPresent(pose -> map.put(id, pose));
    }
    return unmodifiableMap(map);
  }

  private static class NTLocationalData implements LocationalData {
    private final LimelightResults results;
    private final Optional<BotPoseEstimate> poseEstimate;
    private final Optional<BotPoseEstimate> redPoseEstimate;
    private final Optional<BotPoseEstimate> bluePoseEstimate;
    private final Map<Integer, Pose3d> aprilTags;

    NTLocationalData(
        LimelightResults results,
        Optional<BotPoseEstimate> poseEstimate,
        Optional<BotPoseEstimate> redPoseEstimate,
        Optional<BotPoseEstimate> bluePoseEstimate,
        Map<Integer, Pose3d> aprilTags) {
      this.results = results;
      this.poseEstimate = poseEstimate;
      this.redPoseEstimate = redPoseEstimate;
      this.bluePoseEstimate = bluePoseEstimate;
      this.aprilTags = aprilTags;
    }

    @Override
    public boolean isValid() {
      return results.valid;
    }

    @Override
    public boolean hasTarget() {
      return results.targets_Fiducials.length > 0;
    }

    @Override
    public Optional<Pose3d> getBotpose() {
      return toPose3D(results.botpose);
    }

    @Override
    public Optional<BotPoseEstimate> getBotPoseEstimate() {
      return poseEstimate;
    }

    @Override
    public Optional<Pose3d> getBotposeBlue() {
      return toPose3D(results.botpose_wpiblue);
    }

    @Override
    public Optional<BotPoseEstimate> getBotPoseEstimateBlue() {
      return bluePoseEstimate;
    }

    @Override
    public Optional<Pose3d> getBotposeRed() {
      return toPose3D(results.botpose_wpired);
    }

    @Override
    public Optional<BotPoseEstimate> getBotPoseEstimateRed() {
      return redPoseEstimate;
    }

    @Override
    public OptionalDouble getCaptureLatency() {
      return OptionalDouble.of(results.latency_capture);
    }

    @Override
    public OptionalDouble getTargetingLatency() {
      return OptionalDouble.of(results.latency_pipeline);
    }

    @Override
    public Map<Integer, Pose3d> getVisibleAprilTagPoses() {
      return aprilTags;
    }

    private static Optional<Pose3d> toPose3D(double[] inData) {
      if (inData.length != 6 || Arrays.equals(ZEROS, inData)) {
        return Optional.empty();
      }
      return Optional.of(LimelightHelpers.toPose3D(inData));
    }
  }
}
