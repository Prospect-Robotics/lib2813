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

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.math.geometry.Rotation3d;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** Tests for {@link Rotation3dSubject}. */
class Rotation3dSubjectTest {
  private static final Rotation3d ROTATION = new Rotation3d(6.81, -25.67, 3.16);

  @ParameterizedTest
  @EnumSource(
      value = Pose3dComponent.class,
      names = {"ROLL", "PITCH", "YAW"})
  public void isWithin_valueWithinTolerance_doesNotThrow(Pose3dComponent component) {
    Rotation3d closeRotation = component.add(ROTATION, 0.009);

    Rotation3dSubject.assertThat(closeRotation).isWithin(0.01).of(ROTATION);
  }

  @ParameterizedTest
  @EnumSource(
      value = Pose3dComponent.class,
      names = {"ROLL", "PITCH", "YAW"})
  public void isWithin_valueNotWithinTolerance_throws(Pose3dComponent component) {
    Rotation3d closeRotation = component.add(ROTATION, 0.011);

    assertThrows(
        AssertionError.class,
        () -> Rotation3dSubject.assertThat(closeRotation).isWithin(0.01).of(ROTATION));
  }
}
