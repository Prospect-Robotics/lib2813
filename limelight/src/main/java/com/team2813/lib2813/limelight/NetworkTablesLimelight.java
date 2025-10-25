package com.team2813.lib2813.limelight;

import static java.util.Collections.unmodifiableMap;

import com.team2813.lib2813.limelight.LimelightHelpers.LimelightResults;
import com.team2813.lib2813.limelight.LimelightHelpers.PoseEstimate;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import org.json.JSONObject;

/**
 * Implementation of {@link Limelight} that retrieves data from the NetworkTables interface of a
 * Limelight camera.
 */
class NetworkTablesLimelight implements Limelight {

  /** Sentinel array of zeros used to detect uninitialized poses. */
  private static final double[] ZEROS = new double[6];

  private final String limelightName;
  private final AprilTagMapPoseHelper aprilTagMapPoseHelper;

  /**
   * Constructs a NetworkTablesLimelight instance.
   *
   * @param limelightName The hostname or NetworkTables name of the Limelight.
   */
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
    // For NT-based Limelight, we do not support uploading field maps over the network yet.
    aprilTagMapPoseHelper.setFieldMap(stream, false);
  }

  @Override
  public List<Pose3d> getLocatedAprilTags(Set<Integer> visibleTags) {
    return aprilTagMapPoseHelper.getVisibleTagPoses(visibleTags);
  }

  @Override
  public Optional<JSONObject> getJsonDump() {
    // This implementation does not provide raw JSON dumps
    return Optional.empty();
  }

  @Override
  public OptionalDouble getCaptureLatency() {
    return getLocationalData().getCaptureLatency();
  }

  @Override
  public LocationalData getLocationalData() {
    LimelightResults results = LimelightHelpers.getLatestResults(limelightName);

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

  /** Inner class implementing {@link LocationalData} backed by NetworkTables results. */
  private class NTLocationalData implements LocationalData {

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
    public OptionalDouble getTimestamp() {
      return OptionalDouble.of(results.timestamp_LIMELIGHT_publish);
    }

    @Override
    public Set<Integer> getVisibleTags() {
      return Arrays.stream(results.targets_Fiducials)
          .map(fiducial -> (int) fiducial.fiducialID)
          .collect(Collectors.toUnmodifiableSet());
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
