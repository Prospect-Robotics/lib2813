/*
Copyright 2025 Prospect Robotics SWENext Club

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
package com.team2813.lib2813.limelight.apriltag_map;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.Nat;
import edu.wpi.first.math.geometry.Pose3d;

public class Fiducial {
  private int id;
  private double[] transform;

  public Pose3d getPosition() {
    return new Pose3d(new Matrix<>(Nat.N4(), Nat.N4(), transform));
  }

  public int getId() {
    return id;
  }
}
