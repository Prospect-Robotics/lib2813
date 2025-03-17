package com.team2813.lib2813.limelight;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalDouble;

import com.team2813.lib2813.limelight.LimelightHelpers.PoseEstimate;
import com.team2813.lib2813.limelight.LimelightHelpers.LimelightResults;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import org.json.JSONObject;

class NetworkTablesLimelight implements Limelight {
  private static final double[] ZEROS = new double[6];
  private final String limelightName;
  private final AprilTagMapPoseHelper aprilTagMapPoseHelper;

  NetworkTablesLimelight(String limelightName) {
    this.limelightName = limelightName;
    aprilTagMapPoseHelper = new AprilTagMapPoseHelper(new LimelightClient(limelightName));
  }

  @Override
  public OptionalDouble getTimestamp() {
    return getLocationalData().getTimestamp();
  }

  @Override
  public boolean hasTarget() {
    return getLocationalData().hasTarget();
  }

  @Override
  public void setFieldMap(InputStream stream, boolean updateLimelight) throws IOException {
    // The updateLimelight assumes we have the hostname of the limelight, which we don't. For now, this won't update the limelight.
    aprilTagMapPoseHelper.setFieldMap(stream, false);
  }

  @Override
  public List<Pose3d> getLocatedAprilTags(Set<Integer> visibleTags) {
    return aprilTagMapPoseHelper.getVisibleTagPoses(visibleTags);
  }

  @Override
  public Optional<JSONObject> getJsonDump() {
    return Optional.empty();
  }

  @Override
  public OptionalDouble getCaptureLatency() {
    return getLocationalData().getCaptureLatency();
  }

  @Override
  public LocationalData getLocationalData() {
    LimelightHelpers.LimelightResults results = LimelightHelpers.getLatestResults(limelightName);
    if (results.error == null && results.valid) {
      var poseEstimate = toBotPoseEstimate(LimelightHelpers.getBotPoseEstimate(limelightName));
      var redPoseEstimate = toBotPoseEstimate(LimelightHelpers.getBotPoseEstimate_wpiRed(limelightName));
      var bluePoseEstimate = toBotPoseEstimate(LimelightHelpers.getBotPoseEstimate_wpiBlue(limelightName));
      return new NTLocationalData(results, poseEstimate, redPoseEstimate, bluePoseEstimate);
    }
    return StubLocationalData.INVALID;
  }

  private static Optional<BotPoseEstimate> toBotPoseEstimate(PoseEstimate estimate) {
    if (estimate == null || estimate.tagCount == 0 || Pose2d.kZero.equals(estimate.pose)) {
      return Optional.empty();
    }
    return Optional.of(new BotPoseEstimate(estimate.pose, estimate.timestampSeconds));
  }

  private static class NTLocationalData implements LocationalData {
    private final LimelightResults results;
    private final Optional<BotPoseEstimate> poseEstimate;
    private final Optional<BotPoseEstimate> redPoseEstimate;
    private final Optional<BotPoseEstimate> bluePoseEstimate;

    NTLocationalData(LimelightHelpers.LimelightResults results, Optional<BotPoseEstimate> poseEstimate, Optional<BotPoseEstimate> redPoseEstimate, Optional<BotPoseEstimate> bluePoseEstimate) {
      this.results = results;
      this.poseEstimate = poseEstimate;
      this.redPoseEstimate = redPoseEstimate;
      this.bluePoseEstimate = bluePoseEstimate;
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
    public OptionalDouble getTimestamp() {
      return OptionalDouble.of(results.timestamp_LIMELIGHT_publish);
    }

    @Override
    public Set<Integer> getVisibleTags() {
      return Arrays.stream(results.targets_Fiducials).map(fiducial -> (int) fiducial.fiducialID).collect(Collectors.toUnmodifiableSet());
    }

    private static Optional<Pose3d> toPose3D(double[] inData) {
      if (inData.length != 6 || Arrays.equals(ZEROS, inData)) {
        return Optional.empty();
      }
      return Optional.of(LimelightHelpers.toPose3D(inData));
    }
  }
}
