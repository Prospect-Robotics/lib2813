package com.team2813.lib2813.vision;

import static com.team2813.lib2813.vision.VisionNetworkTables.APRIL_TAG_POSE_TOPIC;
import static com.team2813.lib2813.vision.VisionNetworkTables.POSE_ESTIMATE_TOPIC;
import static com.team2813.lib2813.vision.VisionNetworkTables.getTableForCamera;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.StructTopic;
import edu.wpi.first.units.Units;
import edu.wpi.first.wpilibj.Timer;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.photonvision.EstimatedRobotPose;
import org.photonvision.PhotonCamera;

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
  private final TimestampedStructPublisher<Pose3d> publisher;
  private final TimestampedStructPublisher<Pose3d> tagPublisher;
  private final AprilTagFieldLayout aprilTagFieldLayout;

  /**
   * Creates a publisher for the provided camera and field layout.
   *
   * @param camera Camera to use to get the Network Tables name to publish to.
   * @param aprilTagFieldLayout Layout of AprilTags on a field.
   */
  public PhotonVisionPosePublisher(PhotonCamera camera, AprilTagFieldLayout aprilTagFieldLayout) {
    this(camera, aprilTagFieldLayout, Timer::getFPGATimestamp);
  }

  /** Package-scoped constructor (for unit testing). */
  PhotonVisionPosePublisher(
      PhotonCamera camera,
      AprilTagFieldLayout aprilTagFieldLayout,
      Supplier<Double> fpgaTimestampSupplier) {
    this.aprilTagFieldLayout = aprilTagFieldLayout;
    NetworkTable table = getTableForCamera(camera);
    StructTopic<Pose3d> topic = table.getStructTopic(POSE_ESTIMATE_TOPIC, Pose3d.struct);
    publisher = new TimestampedStructPublisher<>(topic, Pose3d.kZero, fpgaTimestampSupplier);
    topic = table.getStructTopic(APRIL_TAG_POSE_TOPIC, Pose3d.struct);
    tagPublisher = new TimestampedStructPublisher<>(topic, Pose3d.kZero, fpgaTimestampSupplier);
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
    publisher.publish(
        poseEstimates.stream()
            .map(PhotonVisionPosePublisher::toRobotPoseTimestampedValue)
            .toList());
    tagPublisher.publish(
        poseEstimates.stream().flatMap(this::toAprilTagPoseTimestampedValue).toList());
  }

  private static TimestampedValue<Pose3d> toRobotPoseTimestampedValue(EstimatedRobotPose pose) {
    return TimestampedValue.withFpgaTimestamp(
        pose.timestampSeconds, Units.Seconds, pose.estimatedPose);
  }

  private Stream<TimestampedValue<Pose3d>> toAprilTagPoseTimestampedValue(EstimatedRobotPose pose) {
    if (pose.targetsUsed.isEmpty()) {
      return Stream.empty();
    }
    return aprilTagFieldLayout
        .getTagPose(pose.targetsUsed.get(0).fiducialId)
        .map(
            tagPose ->
                TimestampedValue.withFpgaTimestamp(pose.timestampSeconds, Units.Seconds, tagPose))
        .stream();
  }
}
