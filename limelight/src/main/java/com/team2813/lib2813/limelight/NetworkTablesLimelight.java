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
import edu.wpi.first.math.geometry.Pose3d;
import org.json.JSONObject;

import static com.ctre.phoenix6.Utils.getCurrentTimeSeconds;

class NetworkTablesLimelight implements Limelight {
  private static final double[] ZEROS = new double[6];
  private final String limelightName;
  private final AprilTagMapPoseHelper aprilTagMapPoseHelper;

  NetworkTablesLimelight(String limelightName) {
    this.limelightName = limelightName;
    aprilTagMapPoseHelper = new AprilTagMapPoseHelper(new LimelightClient(limelightName));
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
  public LocationalData getLocationalData() {
    return getResults().orElse(StubLocationalData.INSTANCE);
  }

  @Override
  public Optional<JSONObject> getJsonDump() {
    return Optional.empty();
  }

  @Override
  public OptionalDouble getCaptureLatency() {
    return getLocationalData().getCaptureLatency();
  }

  private Optional<LocationalData> getResults() {
    LimelightHelpers.LimelightResults results = LimelightHelpers.getLatestResults(limelightName);
    if (results.error == null) {
      var bluePoseEstimate = LimelightHelpers.getBotPoseEstimate_wpiBlue_MegaTag2(limelightName);
      return Optional.of(new NTLocationalData(results, Optional.ofNullable(bluePoseEstimate)));
    }
    return Optional.empty();
  }

  private static class NTLocationalData implements LocationalData {
    private final double timestamp = getCurrentTimeSeconds();
    private final LimelightResults results;
    private final Optional<PoseEstimate> bluePoseEstimate;

    NTLocationalData(LimelightHelpers.LimelightResults results, Optional<PoseEstimate> bluePoseEstimate) {
      this.results = results;
      this.bluePoseEstimate = bluePoseEstimate;
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
    public Optional<Pose3d> getBotposeBlue() {
      return bluePoseEstimate.map(estimate -> new Pose3d(estimate.pose))
              .or(() -> toPose3D(results.botpose_wpiblue));
    }

    @Override
    public Optional<Pose3d> getBotposeRed() {
      return toPose3D(results.botpose_wpired);
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
    public double getTimestamp() {
      // The timestamp in "bluePoseEstimate" already has the latency removed.
      return bluePoseEstimate
          .map(estimate -> estimate.timestampSeconds)
          .orElseGet(
              () -> {
                // timestamp does not include latency.
                double latencyMillis =
                    results.latency_capture + results.latency_pipeline + results.latency_jsonParse;
                return timestamp - (latencyMillis / 1000);
              });
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
