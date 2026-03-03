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

import edu.wpi.first.math.geometry.Rotation2d;
import org.junit.jupiter.api.Test;

/** Tests for {@link Rotation2dSubject}. */
class Rotation2dSubjectTest {
  private static final Rotation2d ROTATION = new Rotation2d(Math.PI / 6);

  @Test
  public void isWithin_valueWithinTolerance_doesNotThrow() {
    Rotation2d closeRotation = Pose2dComponent.R.add(ROTATION, 0.009);

    Rotation2dSubject.assertThat(closeRotation).isWithin(0.01).of(ROTATION);
  }

  @Test
  public void isWithin_valueNotWithinTolerance_throws() {
    Rotation2d closeRotation = Pose2dComponent.R.add(ROTATION, 0.011);

    assertThrows(
        AssertionError.class,
        () -> Rotation2dSubject.assertThat(closeRotation).isWithin(0.01).of(ROTATION));
  }
}
