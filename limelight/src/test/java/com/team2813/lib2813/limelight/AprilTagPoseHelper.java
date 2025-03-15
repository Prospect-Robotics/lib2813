package com.team2813.lib2813.limelight;

import edu.wpi.first.math.geometry.*;
import edu.wpi.first.units.Units;


public class AprilTagPoseHelper {
  private AprilTagPoseHelper() {
    throw new AssertionError("Not instantiable!");
  }
  /**
   * 2025 <a href="https://firstfrc.blob.core.windows.net/frc2025/FieldAssets/2025FieldDrawings-FieldLayoutAndMarking.pdf">AprilTag locations</a>.
   * The format for each entry is {X, Y, Z, Z-rot, Y-rot}. (inches, degrees)
   */
  private static final double[][] aprilTagLocations2025 = new double[][] {
          { 657.37, 25.80, 58.50, 126, 0 }, // ID 1
          { 657.37, 291.20, 58.50, 234, 0 }, // ID 2
          { 455.15, 317.15, 51.25, 270, 0 }, // ID 3
          { 365.20, 241.64, 73.54, 0, 30 }, // ID 4
          { 365.20, 75.39, 73.54, 0, 30 }, // ID 5
          { 530.49, 130.17, 12.13, 300, 0 }, // ID 6
          { 546.87, 158.50, 12.13, 0, 0 }, // ID 7
          { 530.49, 186.83, 12.13, 60, 0 }, // ID 8
          { 497.77, 186.83, 12.13, 120, 0}, // ID 9
          { 481.39, 158.50, 12.13, 180, 0 }, // ID 10
          { 497.77, 130.17, 12.13, 240, 0 }, // ID 11
          { 33.51, 25.80, 58.50, 54, 0 }, // ID 12
          { 33.51, 291.20, 58.50, 306, 0 }, // ID 13
          { 325.68, 241.64, 73.54, 180, 30 }, // ID 14
          { 325.68, 75.39, 73.54, 180, 30 }, // ID 15
          { 235.73, -0.15, 51.25, 90, 0}, // ID 16
          { 160.39, 130.17, 12.13, 240, 0 }, // ID 17
          { 144.00, 158.50, 12.13, 180, 0 }, // ID 18
          { 160.39, 186.83, 12.13, 120, 0 }, // ID 19
          { 193.10, 186.83, 12.13, 60, 0 }, // ID 20
          { 209.49, 158.50, 12.13, 0, 0 }, // ID 21
          { 193.10, 130.17, 12.13, 300, 0 }, // ID 22
  };
  
  /**
   * Gets the location of the tag in 3D space for the 2025 field.
   * This uses the Welded field layout.
   * @param id The tag ID [1,22]
   * @return The location of the tag
   */
  public static Pose3d get2025TagLocation(int id) {
    if (id < 1 || id > 22) {
      throw new IllegalArgumentException(String.format("id needs to be on the interval [1,22], but was %d!", id));
    }
    return getTagLocation(aprilTagLocations2025[id - 1]);
  }
  
  /**
   * Checks if the id is for a valid 2025 AprilTag
   * @param id The tag ID
   * @return {@code true} if id is on the interval [1, 22]
   */
  public static boolean isValid2025Tag(int id) {
    return id >= 1 && id <= 22;
  }
  
  private static Pose3d getTagLocation(double[] arr) {
    Translation3d translation3d = new Translation3d(Units.Inches.of(arr[0]), Units.Inches.of(arr[1]), Units.Inches.of(arr[2]));
    Rotation3d rotation3d = new Rotation3d(Units.Degrees.of(0), Units.Degrees.of(arr[4]), Units.Degrees.of(arr[3]));
    return new Pose3d(translation3d, rotation3d);
  }
}
