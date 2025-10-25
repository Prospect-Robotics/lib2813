package com.team2813.lib2813.limelight.apriltag_map;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.Nat;
import edu.wpi.first.math.geometry.Pose3d;

/**
 * Represents a fiducial (AprilTag) in 3D space.
 *
 * <p>This class stores the fiducial's unique ID and its pose as a 4x4 transformation matrix. It can
 * convert this transform into a {@link Pose3d} object for use with WPILib's geometry and odometry
 * systems.
 */
public class Fiducial {

  /** The unique identifier of this fiducial. */
  private int id;

  /**
   * The 4x4 transformation matrix representing the fiducial's pose in 3D space.
   *
   * <p>The matrix is stored in row-major order as a 16-element array.
   */
  private double[] transform;

  /**
   * Returns the 3D pose of this fiducial as a {@link Pose3d}.
   *
   * @return the fiducial's pose in 3D space
   */
  public Pose3d getPosition() {
    return new Pose3d(new Matrix<>(Nat.N4(), Nat.N4(), transform));
  }

  /**
   * Returns the unique ID of this fiducial.
   *
   * @return the fiducial's ID
   */
  public int getId() {
    return id;
  }
}
