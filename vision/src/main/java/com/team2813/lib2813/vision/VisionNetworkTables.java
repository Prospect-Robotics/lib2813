package com.team2813.lib2813.vision;

import edu.wpi.first.networktables.NetworkTable;
import org.photonvision.PhotonCamera;

/**
 * Contains methods and constants for publishing data from robot vision systems to network tables.
 */
final class VisionNetworkTables {
  /** Topic name to use when publishing the Pose3d position of a camera. */
  static final String CAMERA_POSE_TOPIC = "cameraPose";

  /** Topic name to use when publishing the estimated robot position as a Pose2d value */
  static final String POSE_ESTIMATE_TOPIC = "poseEstimate";

  /** Topic name to use when publishing the position of the detected AprilTag as a Pose2d value. */
  static final String APRIL_TAG_POSE_TOPIC = "aprilTagPose";

  /** Name of the subtable under `photonvision/[cameraName]/' where topics are added. */
  private static final String SUBTABLE_NAME = "LatestPose";

  /**
   * Gets the network table for the provided photon vision camera to use for publishing data.
   *
   * <p>The key of the network table will be `photonvision/[cameraName]/LatestPose`.
   */
  public static NetworkTable getTableForCamera(PhotonCamera camera) {
    return camera.getCameraTable().getSubTable(SUBTABLE_NAME);
  }

  private VisionNetworkTables() {
    throw new AssertionError("Not instantiable");
  }
}
