package com.team2813.lib2813.limelight;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalDouble;

import com.team2813.lib2813.limelight.LimelightHelpers.LimelightResults;
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
  public LocationalData getLocationalData() {
    Optional<LimelightHelpers.LimelightResults> results = getResults();
    if (results.isEmpty()) {
      return StubLocationalData.INSTANCE;
    }
    return new NTLocationalData(results.get());
  }

  @Override
  public Optional<JSONObject> getJsonDump() {
    return Optional.empty();
  }

  @Override
  public OptionalDouble getCaptureLatency() {
    return getLocationalData().getCaptureLatency();
  }

  private Optional<LimelightResults> getResults() {
    LimelightHelpers.LimelightResults results = LimelightHelpers.getLatestResults(limelightName);
    if (results.error == null) {
      return Optional.of(results);
    }
    return Optional.empty();
  }

  private static class NTLocationalData implements LocationalData {
    private final LimelightResults results;

    NTLocationalData(LimelightHelpers.LimelightResults results) {
      this.results = results;
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
      return toPose3D(results.botpose_wpiblue);
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
