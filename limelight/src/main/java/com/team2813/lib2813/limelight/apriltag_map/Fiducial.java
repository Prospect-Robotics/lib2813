package com.team2813.lib2813.limelight.apriltag_map;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.Nat;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.units.Units;

public class Fiducial {
  private int id;
  private double[] transform;
  public Pose3d getPosition() {
    return new Pose3d(new Matrix<>(Nat.N4(), Nat.N4(), transform)).relativeTo(
            new Pose3d(
                    Units.Meters.of(-8.7736),
                    Units.Meters.of(-4.0257),
                    Units.Meters.of(0),
                    new Rotation3d()));
  }
  public int getId() {
    return id;
  }
}
