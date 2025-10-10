package com.team2813.lib2813.limelight;

import edu.wpi.first.math.geometry.Pose3d;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;

/**
 * Interface for retrieving positional and vision data from a Limelight camera.
 *
 * <p>This includes robot pose estimates, visible AprilTags, and latency measurements.
 *
 * @see Limelight
 */
public interface LocationalData {

  /**
   * Returns {@code true} if the Limelight has identified at least one target in the current frame.
   *
   * @return {@code true} if a target is detected
   */
  boolean hasTarget();

  /**
   * Returns {@code true} if the Limelight has provided a valid response for this frame.
   *
   * @return {@code true} if the data is valid
   */
  boolean isValid();

  /**
   * Gets the robot's 3D position relative to the center of the field.
   *
   * @return the robot's pose, if available
   */
  Optional<Pose3d> getBotpose();

  /**
   * Gets an estimated robot pose relative to the center of the field, including visible AprilTags.
   *
   * @return the estimated robot pose
   */
  Optional<BotPoseEstimate> getBotPoseEstimate();

  /**
   * Gets the robot's 3D position relative to the blue driver station as origin.
   *
   * @return the robot's pose, if available
   */
  Optional<Pose3d> getBotposeBlue();

  /**
   * Gets an estimated robot pose relative to the blue driver station as origin.
   *
   * @return the estimated robot pose
   */
  Optional<BotPoseEstimate> getBotPoseEstimateBlue();

  /**
   * Gets the robot's 3D position relative to the red driver station as origin.
   *
   * @return the robot's pose, if available
   */
  Optional<Pose3d> getBotposeRed();

  /**
   * Gets an estimated robot pose relative to the red driver station as origin.
   *
   * @return the estimated robot pose
   */
  Optional<BotPoseEstimate> getBotPoseEstimateRed();

  /**
   * Capture latency in milliseconds.
   *
   * <p>This is the time between the end of the exposure of the middle row and the beginning of the
   * tracking loop.
   *
   * @deprecated Use {@link #getBotPoseEstimateBlue()} or {@link #getBotPoseEstimateRed()} instead
   */
  @Deprecated
  OptionalDouble getCaptureLatency();

  /**
   * Targeting latency in milliseconds.
   *
   * <p>This is the time taken by the tracking loop for this frame.
   *
   * @deprecated Use {@link #getBotPoseEstimateBlue()} or {@link #getBotPoseEstimateRed()} instead
   */
  @Deprecated
  OptionalDouble getTargetingLatency();

  /**
   * @deprecated Use methods that return a {@link BotPoseEstimate} instead
   */
  @Deprecated
  OptionalDouble getTimestamp();

  /**
   * Returns the sum of capture latency and targeting latency in milliseconds, if both are
   * available.
   *
   * @return the total latency in milliseconds
   * @deprecated Use {@link #getBotPoseEstimateBlue()} or {@link #getBotPoseEstimateRed()} instead
   */
  @Deprecated
  default OptionalDouble lastMSDelay() {
    OptionalDouble capture = getCaptureLatency();
    OptionalDouble targeting = getTargetingLatency();
    if (capture.isPresent() && targeting.isPresent()) {
      return OptionalDouble.of(capture.getAsDouble() + targeting.getAsDouble());
    }
    return OptionalDouble.empty();
  }

  /**
   * Gets the IDs of all visible AprilTags in the current frame.
   *
   * @return the set of visible tag IDs
   * @deprecated use {@link #getVisibleAprilTagPoses()} instead
   */
  @Deprecated
  Set<Integer> getVisibleTags();

  /**
   * Returns a map of visible AprilTags, keyed by their ID, with values as their 3D positions.
   *
   * @return a map of visible AprilTags
   */
  Map<Integer, Pose3d> getVisibleAprilTagPoses();
}
