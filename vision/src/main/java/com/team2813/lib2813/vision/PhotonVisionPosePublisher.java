package com.team2813.lib2813.vision;

import static com.team2813.lib2813.vision.VisionNetworkTables.APRIL_TAG_POSE_TOPIC;
import static com.team2813.lib2813.vision.VisionNetworkTables.POSE_ESTIMATE_TOPIC;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.StructTopic;
import edu.wpi.first.units.Units;
import edu.wpi.first.wpilibj.Timer;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.photonvision.EstimatedRobotPose;
import org.photonvision.targeting.PhotonTrackedTarget;

/**
 * Publishes timestamped pose estimates from a camera.
 *
 * <p>This is useful for publishing {@link EstimatedRobotPose} values from a PhotonVision camera in
 * a way that can be visualized in tools like AdvantageScope without pose locations flickering.
 * Estimated robot positions are published to NetworkTables using the timestamp in the {@code
 * EstimatedRobotPose}. If no data is available, a position of (0, 0, 0) is published only when the
 * previous available value is older than the expected latency of producing vision estimates.
 */
public final class PhotonVisionPosePublisher {
  /**
   * How much time we expect to pass between receiving pose estimates from PhotonVision when an
   * AprilTag is visible. Empirically, this is 0.1 seconds.
   */
  private static final long EXPECTED_MILLIS_BETWEEN_POSE_ESTIMATES = 100;

  private final TimestampedStructPublisher<Pose3d> robotPosePublisher;
  private final TimestampedStructPublisher<Pose3d> aprilTagPosePublisher;
  private final AprilTagFieldLayout aprilTagFieldLayout;

  /**
   * Creates a publisher that publishes values under the given table.
   *
   * @param parentTable Parent table for all topics published by this publisher instance.
   * @param aprilTagFieldLayout Layout of AprilTags on the field.
   */
  public PhotonVisionPosePublisher(
      NetworkTable parentTable, AprilTagFieldLayout aprilTagFieldLayout) {
    this(parentTable, aprilTagFieldLayout, Timer::getFPGATimestamp);
  }

  /** Package-scoped constructor (for unit testing). */
  PhotonVisionPosePublisher(
      NetworkTable parentTable,
      AprilTagFieldLayout aprilTagFieldLayout,
      Supplier<Double> fpgaTimestampSupplier) {
    this.aprilTagFieldLayout = aprilTagFieldLayout;
    StructTopic<Pose3d> topic = parentTable.getStructTopic(POSE_ESTIMATE_TOPIC, Pose3d.struct);
    robotPosePublisher =
        new TimestampedStructPublisher<>(topic, Pose3d.kZero, fpgaTimestampSupplier);
    robotPosePublisher.setTimeUntilStale(
        EXPECTED_MILLIS_BETWEEN_POSE_ESTIMATES, Units.Milliseconds);
    topic = parentTable.getStructTopic(APRIL_TAG_POSE_TOPIC, Pose3d.struct);
    aprilTagPosePublisher =
        new TimestampedStructPublisher<>(topic, Pose3d.kZero, fpgaTimestampSupplier);
    aprilTagPosePublisher.setTimeUntilStale(
        EXPECTED_MILLIS_BETWEEN_POSE_ESTIMATES, Units.Milliseconds);
  }

  /**
   * Publishes the estimated positions to network tables.
   *
   * <p>This should be called in a <a
   * href="https://docs.wpilib.org/en/stable/docs/software/convenience-features/scheduling-functions.html">periodic
   * method</a> once per loop, even if no data is currently available.
   *
   * @param poseEstimates The estimated locations (with the blue driver station as the origin).
   */
  public void publish(List<EstimatedRobotPose> poseEstimates) {
    // Publish all the estimated robot positions.
    List<TimestampedValue<Pose3d>> robotPoses =
        poseEstimates.stream().map(this::getRobotPoseFromEstimatedRobotPose).toList();
    robotPosePublisher.publish(robotPoses);

    // Publish the location of the AprilTags used for the above estimated positions.
    List<TimestampedValue<Pose3d>> aprilTagPoses =
        poseEstimates.stream()
            .map(this::getBestVisibleAprilTag)
            .flatMap(Optional::stream) // Convert Stream<Optional<V>> -> Stream<V>
            .toList();
    aprilTagPosePublisher.publish(aprilTagPoses);
  }

  /** Gets the robot pose from the EstimatedRobotPose and converts it to a timestamped value. */
  private TimestampedValue<Pose3d> getRobotPoseFromEstimatedRobotPose(
      EstimatedRobotPose estimatedRobotPose) {
    return TimestampedValue.withFpgaTimestamp(
        estimatedRobotPose.timestampSeconds, Units.Seconds, estimatedRobotPose.estimatedPose);
  }

  /** Gets the highest-quality AprilTag used to estimate the position of the robot. */
  private Optional<TimestampedValue<Pose3d>> getBestVisibleAprilTag(
      EstimatedRobotPose estimatedRobotPose) {
    List<PhotonTrackedTarget> visibleAprilTags = estimatedRobotPose.targetsUsed;
    if (visibleAprilTags.isEmpty()) {
      // Not sure how we would have a pose without a target visible, but best to avoid the
      // IndexOutOfBoundsException that get(0) would throw.
      return Optional.empty();
    }

    Optional<Pose3d> poseOfBestAprilTag =
        aprilTagFieldLayout.getTagPose(visibleAprilTags.get(0).fiducialId);
    return poseOfBestAprilTag.map(
        aprilTagPose ->
            TimestampedValue.withFpgaTimestamp(
                estimatedRobotPose.timestampSeconds, Units.Seconds, aprilTagPose));
  }
}
