/*
Copyright 2025-2026 Prospect Robotics SWENext Club

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
