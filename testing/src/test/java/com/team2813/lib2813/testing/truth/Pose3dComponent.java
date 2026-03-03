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

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation3d;
import java.util.stream.Stream;

/** Represents component in a three-dimensional coordinate system. */
enum Pose3dComponent implements Component {
  X(Type.TRANSLATION) {
    @Override
    Pose3d add(Pose3d pose, double value) {
      return new Pose3d(pose.getX() + value, pose.getY(), pose.getZ(), pose.getRotation());
    }
  },
  Y(Type.TRANSLATION) {
    @Override
    Pose3d add(Pose3d pose, double value) {
      return new Pose3d(pose.getX(), pose.getY() + value, pose.getZ(), pose.getRotation());
    }
  },
  Z(Type.TRANSLATION) {
    @Override
    Pose3d add(Pose3d pose, double value) {
      return new Pose3d(pose.getX(), pose.getY(), pose.getZ() + value, pose.getRotation());
    }
  },
  ROLL(Type.ROTATION) {
    @Override
    Pose3d add(Pose3d pose, double value) {
      Rotation3d rotation = pose.getRotation();
      return new Pose3d(
          pose.getTranslation(),
          new Rotation3d(rotation.getX() + value, rotation.getY(), rotation.getZ()));
    }
  },
  PITCH(Type.ROTATION) {
    @Override
    Pose3d add(Pose3d pose, double value) {
      Rotation3d rotation = pose.getRotation();
      return new Pose3d(
          pose.getTranslation(),
          new Rotation3d(rotation.getX(), rotation.getY() + value, rotation.getZ()));
    }
  },
  YAW(Type.ROTATION) {
    @Override
    Pose3d add(Pose3d pose, double value) {
      Rotation3d rotation = pose.getRotation();
      return new Pose3d(
          pose.getTranslation(),
          new Rotation3d(rotation.getX(), rotation.getY(), rotation.getZ() + value));
    }
  };

  /** An arguments provider for Pose3dComponent values that represent translations. */
  static class TranslationsArgumentsProvider extends ComponentArgumentsProvider<Pose3dComponent> {
    TranslationsArgumentsProvider() {
      super(Type.TRANSLATION, Stream.of(Pose3dComponent.values()));
    }
  }

  /** An arguments provider for Pose3dComponent values that represent rotations. */
  static class RotationsArgumentsProvider extends ComponentArgumentsProvider<Pose3dComponent> {
    RotationsArgumentsProvider() {
      super(Type.ROTATION, Stream.of(Pose3dComponent.values()));
    }
  }

  private final Type componentType;

  Pose3dComponent(Type componentType) {
    this.componentType = componentType;
  }

  @Override
  public final Type getType() {
    return componentType;
  }

  abstract Pose3d add(Pose3d pose, double value);

  final Translation3d add(Translation3d translation, double value) {
    Pose3d pose = new Pose3d(translation, Rotation3d.kZero);
    pose = add(pose, value);
    return pose.getTranslation();
  }

  final Rotation3d add(Rotation3d rotation, double value) {
    Pose3d pose = new Pose3d(Translation3d.kZero, rotation);
    pose = add(pose, value);
    return pose.getRotation();
  }
}
