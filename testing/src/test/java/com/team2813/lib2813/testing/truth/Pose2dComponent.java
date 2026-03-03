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
package com.team2813.lib2813.testing.truth;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

enum Pose2dComponent {
  X {
    @Override
    Pose2d add(Pose2d pose, double value) {
      return new Pose2d(pose.getX() + value, pose.getY(), pose.getRotation());
    }
  },
  Y {
    @Override
    Pose2d add(Pose2d pose, double value) {
      return new Pose2d(pose.getX(), pose.getY() + value, pose.getRotation());
    }
  },
  R {
    @Override
    Pose2d add(Pose2d pose, double value) {
      return new Pose2d(
          pose.getTranslation(), new Rotation2d(pose.getRotation().getRadians() + value));
    }
  };

  abstract Pose2d add(Pose2d pose, double value);

  final Translation2d add(Translation2d translation, double value) {
    Pose2d pose = new Pose2d(translation, Rotation2d.kZero);
    pose = add(pose, value);
    return pose.getTranslation();
  }

  final Rotation2d add(Rotation2d rotation, double value) {
    Pose2d pose = new Pose2d(Translation2d.kZero, rotation);
    pose = add(pose, value);
    return pose.getRotation();
  }
}
