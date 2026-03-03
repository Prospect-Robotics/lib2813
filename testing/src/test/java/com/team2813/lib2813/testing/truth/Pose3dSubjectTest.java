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

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** Tests for {@link Pose3dSubject}. */
class Pose3dSubjectTest {
  private static final Pose3d POSE =
      new Pose3d(7.353, 0.706, 42.00, new Rotation3d(6.81, -25.67, 3.16));

  @ParameterizedTest
  @EnumSource(Pose3dComponent.class)
  public void isWithin_valueWithinTolerance_doesNotThrow(Pose3dComponent component) {
    Pose3d closePose = component.add(POSE, 0.009);

    Pose3dSubject.assertThat(closePose).isWithin(0.01).of(POSE);
  }

  @ParameterizedTest
  @EnumSource(Pose3dComponent.class)
  public void isWithin_valueNotWithinTolerance_throws(Pose3dComponent component) {
    Pose3d closePose = component.add(POSE, 0.011);

    assertThrows(
        AssertionError.class, () -> Pose3dSubject.assertThat(closePose).isWithin(0.01).of(POSE));
  }
}
