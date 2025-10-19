package com.team2813.lib2813.vision;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import org.photonvision.PhotonCamera;

/**
 * Contains methods and constants for publishing data from robot vision systems to network tables.
 */
final class VisionNetworkTables {
  /** Topic name to use when publishing the estimated robot position as a Pose2d value */
  static final String POSE_ESTIMATE_TOPIC = "poseEstimate";

  /** Topic name to use when publishing the position of the detected AprilTag as a Pose2d value. */
  static final String APRIL_TAG_POSE_TOPIC = "aprilTagPose";

  private static final String TABLE_NAME = "Vision";

  /**
   * Gets the network table for the provided photon vision camera to use for publishing data.
   *
   * <p>The key of the network table will be `Vision/[cameraName]`.
   */
  public static NetworkTable getTableForCamera(PhotonCamera camera) {
    NetworkTableInstance ntInstance = camera.getCameraTable().getInstance();
    return ntInstance.getTable(TABLE_NAME).getSubTable(camera.getName());
  }

  private VisionNetworkTables() {
    throw new AssertionError("Not instantiable");
  }
}
