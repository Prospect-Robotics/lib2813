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

import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** Tests for {@link Pose2dSubject}. */
class Pose2dSubjectTest {
  private static final Pose2d POSE = new Pose2d(7.353, 0.706, new Rotation2d(Math.PI / 6));

  @ParameterizedTest
  @EnumSource(Pose2dComponent.class)
  public void isWithin_valueWithinTolerance_doesNotThrow(Pose2dComponent component) {
    Pose2d closePose = component.add(POSE, 0.009);

    Pose2dSubject.assertThat(closePose).isWithin(0.01).of(POSE);
  }

  @ParameterizedTest
  @EnumSource(Pose2dComponent.class)
  public void isNotWithin_valueWithinTolerance_throws(Pose2dComponent component) {
    Pose2d closePose = component.add(POSE, 0.009);

    assertThrows(
        AssertionError.class, () -> Pose2dSubject.assertThat(closePose).isNotWithin(0.01).of(POSE));
  }

  @ParameterizedTest
  @EnumSource(Pose2dComponent.class)
  public void isWithin_valueNotWithinTolerance_throws(Pose2dComponent component) {
    Pose2d closePose = component.add(POSE, 0.011);

    assertThrows(
        AssertionError.class, () -> Pose2dSubject.assertThat(closePose).isWithin(0.01).of(POSE));
  }

  @ParameterizedTest
  @EnumSource(Pose2dComponent.class)
  public void isNotWithin_valueNotWithinTolerance_doesNotThrow(Pose2dComponent component) {
    Pose2d closePose = component.add(POSE, 0.011);

    Pose2dSubject.assertThat(closePose).isNotWithin(0.01).of(POSE);
  }
}
