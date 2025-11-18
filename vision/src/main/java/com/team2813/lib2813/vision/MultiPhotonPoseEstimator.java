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
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StructPublisher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
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
 */
public class MultiPhotonPoseEstimator implements AutoCloseable {
  private final List<PhotonCameraWrapper> cameraWrappers;
  private PhotonPoseEstimator.PoseStrategy poseEstimatorStrategy;

  /** A builder for {@code MultiPhotonPoseEstimator}. */
  public static class Builder {
    private final Map<String, CameraConfig> cameraConfigs = new HashMap<>();
    private final AprilTagFieldLayout aprilTagFieldLayout;
    private final NetworkTableInstance ntInstance;
    private final PhotonPoseEstimator.PoseStrategy poseEstimatorStrategy;

    /**
     * {@code MultiPhotonPoseEstimator} builder constructor.
     *
     * @param ntInstance Network table instance used to log the pose of AprilTag detections as well
     *     as pose estimates.
     * @param aprilTagFieldLayout WPILib field description (dimensions) including AprilTag 3D
     *     locations.
     * @param poseEstimatorStrategy Posing strategy (for instance, multi tag PnP, closest to camera
     *     tag, etc.)
     */
    public Builder(
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
     * @param name Unique name of the camera. It is recommended for this to describe the camera's
     *     location (ex: "frontLeft").
     * @param transform 3D position of the camera relative to the robot frame.
     * @return Builder instance.
     */
    public Builder addCamera(String name, Transform3d transform) {
      return addCamera(name, transform, Optional.empty());
    }

    /**
     * Adds a camera and associated simulator properties to the multi pose estimator.
     *
     * @param name Unique name of the camera. It is recommended for this to describe the camera's
     *     location (ex: "frontLeft").
     * @param transform 3D position of the camera relative to the robot frame.
     * @param simulationPropertiesSupplier Factory for providing simulation properties for the
     *     camera. This is only called when {@link #addCamerasToSimulator(VisionSystemSim)} is
     *     called.
     * @return Builder instance.
     */
    public Builder addCamera(
        String name,
        Transform3d transform,
        Supplier<SimCameraProperties> simulationPropertiesSupplier) {
      return addCamera(name, transform, Optional.of(simulationPropertiesSupplier));
    }

    private Builder addCamera(
        String name,
        Transform3d transform,
        Optional<Supplier<SimCameraProperties>> simPropertiesSupplier) {
      Objects.requireNonNull(name, "camera name cannot be null");
      Objects.requireNonNull(transform, "transform cannot be null");
      if (cameraConfigs.put(name, new CameraConfig(transform, simPropertiesSupplier)) != null) {
        throw new IllegalArgumentException(String.format("Already a camera with name '%s'", name));
      }
      return this;
    }

    /** Builds a configured MultiPhotonPoseEstimator. */
    public MultiPhotonPoseEstimator build() {
      return new MultiPhotonPoseEstimator(this);
    }
  }

  /**
   * Adds all cameras to a simulated vision system.
   *
   * @param simVisionSystem The simulated visual system.
   */
  public void addCamerasToSimulator(VisionSystemSim simVisionSystem) {
    // Validate all inputs and create SimCameraProperties for each camera.
    Map<String, SimCameraProperties> cameraNameToSimProperties =
        cameraWrappers.stream()
            .collect(
                toMap(
                    wrapper -> wrapper.camera.getName(), PhotonCameraWrapper::createSimProperties));

    // Add cameras to the simulated vision system
    cameraWrappers.forEach(
        wrapper -> {
          SimCameraProperties cameraProps = cameraNameToSimProperties.get(wrapper.camera.getName());
          PhotonCameraSim simCamera = new PhotonCameraSim(wrapper.camera(), cameraProps);
          simVisionSystem.addCamera(simCamera, wrapper.estimator.getRobotToCameraTransform());
        });
  }

  /**
   * Configuration for a camera that is connected to PhotonVision.
   *
   * @param robotToCamera The 3D fixed pose of the camera relative to the robot. Intuitively, this *
   *     field describes where on the robot the camera is mounted.
   * @param simulationPropertiesSupplier Factory for providing simulation properties for the camera.
   */
  private record CameraConfig(
      Transform3d robotToCamera,
      Optional<Supplier<SimCameraProperties>> simulationPropertiesSupplier) {}

  /**
   * Wrapper containing a PhotonVision camera, pose estimator and publishers.
   *
   * @param camera A camera connected to PhotonVision.
   * @param estimator A pose estimator configured for this camera.
   * @param robotToCamera The 3D fixed pose of the camera relative to the robot. Intuitively, this
   *     field describes where on the robot the camera is mounted.
   * @param simPropertiesSupplier Factory for providing simulation properties for the camera.
   * @param robotPosePublisher A publisher reporting PhotonVision pose detections to NetworkTables
   *     during the robot runtime.
   * @param cameraPosePublisher A publisher reporting the position of the camera in field-centric
   *     coordinates. In other words, this is the pose most recently set by {@link @setDrivePose}
   *     with the camera's own robotToCamera pose appended to it.
   */
  private record PhotonCameraWrapper(
      PhotonCamera camera,
      PhotonPoseEstimator estimator,
      Transform3d robotToCamera,
      Optional<Supplier<SimCameraProperties>> simPropertiesSupplier,
      PhotonVisionPosePublisher robotPosePublisher,
      StructPublisher<Pose3d> cameraPosePublisher)
      implements AutoCloseable {

    /**
     * Publishes the position of this camera.
     *
     * @param robotPose 3D field-centric (relative to blue origin) pose of the drive train.
     */
    void publishCameraPose(Pose3d robotPose) {
      cameraPosePublisher.set(robotPose.plus(robotToCamera));
    }

    /**
     * Create calibration and performance values for this camera using the caller-provided supplier.
     *
     * @throws IllegalStateException if the caller did not provide a supplier.
     * @throws NullPointerException if the caller-provided supplier returns {@code null}.
     */
    private SimCameraProperties createSimProperties() {
      SimCameraProperties simProperties =
          simPropertiesSupplier
              .orElseThrow(
                  () ->
                      new IllegalStateException(
                          String.format(
                              "Must pass Supplier<SimCameraProperties> to addCamera() to use camera"
                                  + " %s in simulation",
                              camera().getName())))
              .get();
      if (simProperties == null) {
        throw new NullPointerException(
            String.format(
                "Supplier<SimCameraProperties> passed to addCamera(\"%s\", ...) cannot provide null"
                    + " values",
                camera().getName()));
      }
      return simProperties;
    }

    @Override
    public void close() {
      camera.close();
      cameraPosePublisher.close();
      // TODO: Update PhotonVisionPosePublisher to support close() and call it here
    }
  }

  /** Creates an instance using values from a {@code Builder}. */
  private MultiPhotonPoseEstimator(Builder builder) {
    poseEstimatorStrategy = builder.poseEstimatorStrategy;
    cameraWrappers =
        builder.cameraConfigs.entrySet().stream()
            .map(entry -> createCameraWrapperFromConfig(builder, entry.getKey(), entry.getValue()))
            .collect(toCollection(ArrayList::new));
  }

  /**
   * Creates a {@link PhotonCameraWrapper} instance for a camera with the given name and camera
   * configuration.
   *
   * <p>The returned value is used to get pose estimates from the camera.
   */
  private static PhotonCameraWrapper createCameraWrapperFromConfig(
      Builder builder, String cameraName, CameraConfig cameraConfig) {
    PhotonCamera camera = new PhotonCamera(builder.ntInstance, cameraName);
    PhotonPoseEstimator estimator =
        new PhotonPoseEstimator(
            builder.aprilTagFieldLayout, builder.poseEstimatorStrategy, cameraConfig.robotToCamera);

    // Create NetworkTables publishers for 1) the position of the camera relative to the robot and
    // 2) the estimated position provided by the camera.
    NetworkTable parentTable = getTableForCamera(camera);
    StructPublisher<Pose3d> cameraPosePublisher =
        parentTable.getStructTopic(CAMERA_POSE_TOPIC, Pose3d.struct).publish();
    var estimatedPosePublisher =
        new PhotonVisionPosePublisher(parentTable, builder.aprilTagFieldLayout);

    return new PhotonCameraWrapper(
        camera,
        estimator,
        cameraConfig.robotToCamera,
        cameraConfig.simulationPropertiesSupplier,
        estimatedPosePublisher,
        cameraPosePublisher);
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
    for (PhotonCameraWrapper cameraWrapper : cameraWrappers) {
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
    for (PhotonCameraWrapper cameraWrapper : cameraWrappers) {
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
    for (PhotonCameraWrapper cameraWrapper : cameraWrappers) {
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
    for (PhotonCameraWrapper cameraWrapper : cameraWrappers) {
      cameraWrapper.estimator.resetHeadingData(timestampSeconds, heading);
    }
  }

  public void resetHeadingData(double timestampSeconds, Rotation3d heading) {
    // TODO: Use PhotonPoseEstimator.resetHeadingData(double, Rotation2d) once we use a version of
    // PhotonVision that includes it (see  https://github.com/PhotonVision/photonvision/pull/2013).
    for (PhotonCameraWrapper cameraWrapper : cameraWrappers) {
      cameraWrapper.estimator.resetHeadingData(timestampSeconds, heading.toRotation2d());
      cameraWrapper.estimator.addHeadingData(timestampSeconds, heading);
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
  public void processAllUnreadResults(PoseEstimateConsumer poseEstimateConsumer) {
    for (PhotonCameraWrapper cameraWrapper : cameraWrappers) {
      List<EstimatedRobotPose> poses =
          cameraWrapper.camera.getAllUnreadResults().stream()
              .map(cameraWrapper.estimator::update) // PhotonPipelineResult -> EstimatedRobotPose
              .flatMap(Optional::stream) // Convert Stream<Optional<P>> -> Stream<P>
              .toList();

      poses.forEach(poseEstimateConsumer::addEstimatedRobotPose);
      cameraWrapper.robotPosePublisher.publish(poses);
    }
  }

  @Override
  public void close() {
    cameraWrappers.forEach(PhotonCameraWrapper::close);
    cameraWrappers.clear();
  }
}
