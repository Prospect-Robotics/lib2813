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

import edu.wpi.first.math.geometry.Transform3d;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import org.photonvision.simulation.SimCameraProperties;
import org.photonvision.simulation.VisionSystemSim;

/**
 * A camera on a robot.
 *
 * <p>This class can be extended to add additional metadata about the camera.
 *
 * @since 2.0.0
 */
public class Camera {
  private final String name;
  private final Transform3d robotToCamera;
  protected final Optional<Supplier<SimCameraProperties>> simPropertiesSupplier;

  /**
   * Adds a camera and associated simulator properties to the multi pose estimator.
   *
   * @param name Unique name of the camera. It is recommended for this to describe the camera's
   *     location (ex: "frontLeft").
   * @param robotToCamera 3D position of the camera relative to the robot frame.
   */
  public Camera(String name, Transform3d robotToCamera) {
    this(name, robotToCamera, Optional.empty());
  }

  /**
   * Adds a camera and associated simulator properties to the multi pose estimator.
   *
   * @param name Unique name of the camera. It is recommended for this to describe the camera's
   *     location (ex: "frontLeft").
   * @param robotToCamera 3D position of the camera relative to the robot frame.
   * @param simPropertiesSupplier Factory for providing simulation properties for the camera. This
   *     is only called when {@link MultiPhotonPoseEstimator#addCamerasToSimulator(VisionSystemSim)}
   *     is called.
   */
  public Camera(
      String name, Transform3d robotToCamera, Supplier<SimCameraProperties> simPropertiesSupplier) {
    this(
        name,
        robotToCamera,
        Optional.of(
            Objects.requireNonNull(simPropertiesSupplier, "simPropertiesSupplier cannot be null")));
  }

  private Camera(
      String name,
      Transform3d robotToCamera,
      Optional<Supplier<SimCameraProperties>> simPropertiesSupplier) {
    Objects.requireNonNull(name, "camera name cannot be null");
    Objects.requireNonNull(robotToCamera, "robotToCamera cannot be null");
    if (name.isEmpty()) {
      throw new IllegalArgumentException("camera name cannot be empty");
    }
    this.name = name;
    this.robotToCamera = robotToCamera;
    this.simPropertiesSupplier = simPropertiesSupplier;
  }

  /** Gets the name of the camera. */
  public final String name() {
    return name;
  }

  /** Gets the 3D position of the camera relative to the robot frame. */
  public final Transform3d robotToCamera() {
    return robotToCamera;
  }
}
