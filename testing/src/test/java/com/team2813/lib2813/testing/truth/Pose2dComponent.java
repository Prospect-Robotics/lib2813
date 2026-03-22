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
import java.util.stream.Stream;

/** Represents component in a two-dimensional coordinate system. */
enum Pose2dComponent implements Component {
  X(Type.TRANSLATION) {
    @Override
    Pose2d add(Pose2d pose, double value) {
      return new Pose2d(pose.getX() + value, pose.getY(), pose.getRotation());
    }
  },
  Y(Type.TRANSLATION) {
    @Override
    Pose2d add(Pose2d pose, double value) {
      return new Pose2d(pose.getX(), pose.getY() + value, pose.getRotation());
    }
  },
  R(Type.ROTATION) {
    @Override
    Pose2d add(Pose2d pose, double value) {
      return new Pose2d(
          pose.getTranslation(), new Rotation2d(pose.getRotation().getRadians() + value));
    }
  };

  /** An arguments provider for Pose3dComponent values that represent translations. */
  static class TranslationsArgumentsProvider extends ComponentArgumentsProvider<Pose2dComponent> {
    TranslationsArgumentsProvider() {
      super(Type.TRANSLATION, Stream.of(Pose2dComponent.values()));
    }
  }

  private final Type componentType;

  Pose2dComponent(Type componentType) {
    this.componentType = componentType;
  }

  @Override
  public final Type getType() {
    return componentType;
  }

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
