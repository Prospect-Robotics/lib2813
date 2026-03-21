/*
Copyright 2026 Prospect Robotics SWENext Club

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

import static com.team2813.lib2813.vision.VisionNetworkTables.CAMERA_POSE_TOPIC;
import static com.team2813.lib2813.vision.VisionNetworkTables.getTableForCamera;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StructPublisher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.photonvision.EstimatedRobotPose;
import org.photonvision.PhotonCamera;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.simulation.PhotonCameraSim;
import org.photonvision.simulation.SimCameraProperties;
import org.photonvision.simulation.VisionSystemSim;

/**
 * Provides estimated robot positions, in field pose, from multiple PhotonVision cameras.
 *
 * <p>This class manages one or more PhotonVision cameras, and provides an API ({@link
 * #processAllUnreadResults(PoseEstimateConsumer)}) to provide an updated estimated robot pose by
 * combining readings from AprilTags visible from the cameras. It also supports adding the cameras
 * to PhotonVision's simulated vision system.
 *
 * <p>Note that, when we are dealing with 2D and 3D poses, we follow <a
 * href="https://docs.photonvision.org/en/latest/docs/apriltag-pipelines/coordinate-systems.html"
 * target="_top">the transformation conventions</a> established by WPILib and PhotonVision.
 *
 * <p>Furthermore note that the global robot pose or any of the camera global poses are also
 * referred to as "field-centric pose". In our libraries, field-centric poses are <a
 * href="https://docs.wpilib.org/en/stable/docs/software/basic-programming/coordinate-system.html#always-blue-origin"
 * target="_top">always specified relative to the blue origin</a>.
 *
 * @param <C> the type for the camera
 * @since 2.0.0
 */
public class MultiPhotonPoseEstimator<C extends Camera> implements AutoCloseable {
  private final List<PhotonCameraWrapper<C>> cameraWrappers;
  private PhotonPoseEstimator.PoseStrategy poseEstimatorStrategy;

  /** A builder for {@code MultiPhotonPoseEstimator}. */
  public static final class Builder<C extends Camera> {
    private final Map<String, C> cameras = new HashMap<>();
    private final AprilTagFieldLayout aprilTagFieldLayout;
    private final NetworkTableInstance ntInstance;
    private final PhotonPoseEstimator.PoseStrategy poseEstimatorStrategy;

    Builder(
        NetworkTableInstance ntInstance,
        AprilTagFieldLayout aprilTagFieldLayout,
        PhotonPoseEstimator.PoseStrategy poseEstimatorStrategy) {
      this.ntInstance = Objects.requireNonNull(ntInstance, "ntInstance cannot be null");
      this.aprilTagFieldLayout =
          Objects.requireNonNull(aprilTagFieldLayout, "aprilTagFieldLayout cannot be null");
      this.poseEstimatorStrategy =
          Objects.requireNonNull(poseEstimatorStrategy, "poseEstimatorStrategy cannot be null");
    }

    /**
     * Adds a camera to the multi pose estimator.
     *
     * @param camera The camera. Must have a unique name.
     * @return Builder instance.
     */
    public Builder<C> addCamera(C camera) {
      Objects.requireNonNull(camera, "camera cannot be null");
      if (cameras.put(camera.name(), camera) != null) {
        throw new IllegalArgumentException(
            String.format("Already a camera with name '%s'", camera.name()));
      }
      return this;
    }

    /** Builds a configured MultiPhotonPoseEstimator. */
    public MultiPhotonPoseEstimator<C> build() {
      return new MultiPhotonPoseEstimator<>(this);
    }
  }

  /**
   * Creates a builder for building {@link MultiPhotonPoseEstimator} instances.
   *
   * @param ntInstance Network table instance used to log the pose of AprilTag detections as well as
   *     pose estimates.
   * @param aprilTagFieldLayout WPILib field description (dimensions) including AprilTag 3D
   *     locations.
   * @param poseEstimatorStrategy Posing strategy (for instance, multi tag PnP, closest to camera
   *     tag, etc.)
   * @param cameraType The type for the camera.
   */
  public static <C extends Camera> Builder<C> builder(
      NetworkTableInstance ntInstance,
      AprilTagFieldLayout aprilTagFieldLayout,
      PhotonPoseEstimator.PoseStrategy poseEstimatorStrategy,
      Class<C> cameraType) {
    return new Builder<>(ntInstance, aprilTagFieldLayout, poseEstimatorStrategy);
  }

  /**
   * Creates a builder for building {@link MultiPhotonPoseEstimator} instances with a custom Camera
   * type,
   *
   * @param ntInstance Network table instance used to log the pose of AprilTag detections as well as
   *     pose estimates.
   * @param aprilTagFieldLayout WPILib field description (dimensions) including AprilTag 3D
   *     locations.
   * @param poseEstimatorStrategy Posing strategy (for instance, multi tag PnP, closest to camera
   *     tag, etc.)
   */
  public static Builder<Camera> builder(
      NetworkTableInstance ntInstance,
      AprilTagFieldLayout aprilTagFieldLayout,
      PhotonPoseEstimator.PoseStrategy poseEstimatorStrategy) {
    return builder(ntInstance, aprilTagFieldLayout, poseEstimatorStrategy, Camera.class);
  }

  /**
   * Adds all cameras to a simulated vision system.
   *
   * <p>Note that the robot code is responsible for calling {@link VisionSystemSim#update(Pose2d)}
   * or {@link VisionSystemSim#update(Pose3d)} in {@code simulationPeriodic()}.
   *
   * @param simVisionSystem The simulated visual system.
   */
  public void addCamerasToSimulator(VisionSystemSim simVisionSystem) {
    // Validate all inputs and create SimCameraProperties for each camera.
    Map<String, SimCameraProperties> cameraNameToSimProperties =
        cameraWrappers.stream()
            .collect(
                toMap(wrapper -> wrapper.camera.name(), PhotonCameraWrapper::createSimProperties));

    // Add cameras to the simulated vision system
    cameraWrappers.forEach(
        wrapper -> {
          SimCameraProperties cameraProps = cameraNameToSimProperties.get(wrapper.camera.name());
          PhotonCameraSim simCamera = new PhotonCameraSim(wrapper.photonCamera, cameraProps);
          simVisionSystem.addCamera(simCamera, wrapper.estimator.getRobotToCameraTransform());
        });
  }

  /**
   * Wrapper containing a PhotonVision camera, pose estimator and publishers.
   *
   * @param camera The camera.
   * @param photonCamera A camera connected to PhotonVision.
   * @param estimator A pose estimator configured for this camera.
   * @param robotPosePublisher A publisher reporting PhotonVision pose detections to NetworkTables
   *     during the robot runtime.
   * @param cameraPosePublisher A publisher reporting the position of the camera in field-centric
   *     coordinates. In other words, this is the pose most recently set by {@link @setDrivePose}
   *     with the camera's own robotToCamera pose appended to it.
   */
  private record PhotonCameraWrapper<C extends Camera>(
      C camera,
      PhotonCamera photonCamera,
      PhotonPoseEstimator estimator,
      PhotonVisionPosePublisher robotPosePublisher,
      StructPublisher<Pose3d> cameraPosePublisher)
      implements AutoCloseable {

    /**
     * Publishes the position of this camera.
     *
     * @param robotPose 3D field-centric (relative to blue origin) pose of the drive train.
     */
    void publishCameraPose(Pose3d robotPose) {
      cameraPosePublisher.set(robotPose.plus(camera.robotToCamera()));
    }

    /**
     * Create calibration and performance values for this camera using the caller-provided supplier.
     *
     * @throws IllegalStateException if the caller did not provide a supplier.
     * @throws NullPointerException if the caller-provided supplier returns {@code null}.
     */
    private SimCameraProperties createSimProperties() {
      SimCameraProperties simProperties =
          camera
              .simPropertiesSupplier
              .orElseThrow(
                  () ->
                      new IllegalStateException(
                          String.format(
                              "Must pass Supplier<SimCameraProperties> to addCamera() to use camera"
                                  + " %s in simulation",
                              camera().name())))
              .get();
      if (simProperties == null) {
        throw new NullPointerException(
            String.format(
                "Supplier<SimCameraProperties> passed to addCamera(\"%s\", ...) cannot provide null"
                    + " values",
                camera().name()));
      }
      return simProperties;
    }

    @Override
    public void close() {
      photonCamera.close();
      cameraPosePublisher.close();
      robotPosePublisher.close();
    }
  }

  /** Creates an instance using values from a {@code Builder}. */
  private MultiPhotonPoseEstimator(Builder<C> builder) {
    poseEstimatorStrategy = builder.poseEstimatorStrategy;
    cameraWrappers =
        builder.cameras.values().stream()
            .map(camera -> createCameraWrapper(builder, camera))
            .collect(toCollection(ArrayList::new));
  }

  /**
   * Creates a {@link PhotonCameraWrapper} instance for a camera with the given name and camera
   * configuration.
   *
   * <p>The returned value is used to get pose estimates from the camera.
   */
  private static <C extends Camera> PhotonCameraWrapper<C> createCameraWrapper(
      Builder<C> builder, C camera) {
    PhotonCamera photonCamera = new PhotonCamera(builder.ntInstance, camera.name());
    PhotonPoseEstimator estimator =
        new PhotonPoseEstimator(
            builder.aprilTagFieldLayout, builder.poseEstimatorStrategy, camera.robotToCamera());

    // Create NetworkTables publishers for 1) the position of the camera relative to the robot and
    // 2) the estimated position provided by the camera.
    NetworkTable parentTable = getTableForCamera(photonCamera);
    StructPublisher<Pose3d> cameraPosePublisher =
        parentTable.getStructTopic(CAMERA_POSE_TOPIC, Pose3d.struct).publish();
    var estimatedPosePublisher =
        new PhotonVisionPosePublisher(parentTable, builder.aprilTagFieldLayout);

    return new PhotonCameraWrapper<>(
        camera, photonCamera, estimator, estimatedPosePublisher, cameraPosePublisher);
  }

  /**
   * Gets the Position Estimation Strategy being used by the Position Estimators.
   *
   * @return the strategy
   */
  public PhotonPoseEstimator.PoseStrategy getPrimaryStrategy() {
    return poseEstimatorStrategy;
  }

  /**
   * Sets the Position Estimation Strategy used by the Position Estimators.
   *
   * @param poseStrategy the strategy to set
   */
  public void setPrimaryStrategy(PhotonPoseEstimator.PoseStrategy poseStrategy) {
    Objects.requireNonNull(poseStrategy, "poseStrategy cannot be null");
    if (!poseStrategy.equals(poseEstimatorStrategy)) {
      cameraWrappers.forEach(wrapper -> wrapper.estimator.setPrimaryStrategy(poseStrategy));
      poseEstimatorStrategy = poseStrategy;
    }
  }

  /**
   * Determines if the pose strategy requires addHeadingData() to be called with every frame.
   *
   * @return {@code true} if the pose strategy is documented to require addHeadingData().
   */
  public boolean poseStrategyRequiresHeadingData() {
    return switch (poseEstimatorStrategy) {
      case PNP_DISTANCE_TRIG_SOLVE, CONSTRAINED_SOLVEPNP -> true;
      default -> false;
    };
  }

  /**
   * Publishes the position of all the cameras, relative to the given position.
   *
   * <p>Callers will typically pass a field-centric drive train pose.
   *
   * @param pose 2D field-centric (relative to blue origin) pose.
   */
  public void publishCameraPosesRelativeTo(Pose2d pose) {
    Pose3d pose3d = new Pose3d(pose);
    for (PhotonCameraWrapper<C> cameraWrapper : cameraWrappers) {
      cameraWrapper.publishCameraPose(pose3d);
    }
  }

  /**
   * Add robot heading data to buffer. Must be called periodically for the
   * <b>PNP_DISTANCE_TRIG_SOLVE</b> strategy.
   *
   * @param timestampSeconds timestamp of the robot heading data.
   * @param heading Field-relative robot heading at given timestamp. Standard WPILIB field
   *     coordinates.
   */
  public void addHeadingData(double timestampSeconds, Rotation2d heading) {
    for (PhotonCameraWrapper<C> cameraWrapper : cameraWrappers) {
      cameraWrapper.estimator.addHeadingData(timestampSeconds, heading);
    }
  }

  /**
   * Add robot heading data to buffer. Must be called periodically for the
   * <b>PNP_DISTANCE_TRIG_SOLVE</b> strategy.
   *
   * @param timestampSeconds timestamp of the robot heading data.
   * @param heading Field-relative robot heading at given timestamp. Standard WPILIB field
   *     coordinates.
   */
  public void addHeadingData(double timestampSeconds, Rotation3d heading) {
    for (PhotonCameraWrapper<C> cameraWrapper : cameraWrappers) {
      cameraWrapper.estimator.addHeadingData(timestampSeconds, heading);
    }
  }

  /**
   * Clears all heading data in the buffer, and adds a new seed. Useful for preventing estimates
   * from utilizing heading data provided prior to a pose or rotation reset.
   *
   * @param timestampSeconds timestamp of the robot heading data.
   * @param heading Field-relative robot heading at given timestamp. Standard WPILIB field
   *     coordinates.
   */
  public void resetHeadingData(double timestampSeconds, Rotation2d heading) {
    for (PhotonCameraWrapper<C> cameraWrapper : cameraWrappers) {
      cameraWrapper.estimator.resetHeadingData(timestampSeconds, heading);
    }
  }

  public void resetHeadingData(double timestampSeconds, Rotation3d heading) {
    for (PhotonCameraWrapper<C> cameraWrapper : cameraWrappers) {
      cameraWrapper.estimator.resetHeadingData(timestampSeconds, heading);
    }
  }

  /**
   * Sends all unread robot-pose estimations from all cameras to the provided consumer.
   *
   * <p>This method is supposed to be called from a routine updating drive-train pose with pose
   * estimates from the photon vision cameras.
   *
   * @param poseEstimateConsumer Functional interface for consuming computed pose estimates.
   */
  public void processAllUnreadResults(PoseEstimateConsumer<C> poseEstimateConsumer) {
    for (PhotonCameraWrapper<C> cameraWrapper : cameraWrappers) {
      List<EstimatedRobotPose> poses =
          cameraWrapper.photonCamera.getAllUnreadResults().stream()
              .map(cameraWrapper.estimator::update) // PhotonPipelineResult -> EstimatedRobotPose
              .flatMap(Optional::stream) // Convert Stream<Optional<P>> -> Stream<P>
              .toList();

      poses.forEach(pose -> poseEstimateConsumer.addEstimatedRobotPose(pose, cameraWrapper.camera));
      cameraWrapper.robotPosePublisher.publish(poses);
    }
  }

  @Override
  public void close() {
    cameraWrappers.forEach(PhotonCameraWrapper::close);
    cameraWrappers.clear();
  }
}
