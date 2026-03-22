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

import edu.wpi.first.math.geometry.Translation2d;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

/** Tests for {@link Translation2dSubject}. */
class Translation2dSubjectTest {
  private static final Translation2d TRANSLATION = new Translation2d(7.353, 0.706);

  @ParameterizedTest
  @ArgumentsSource(Pose2dComponent.TranslationsArgumentsProvider.class)
  public void isWithin_valueWithinTolerance_doesNotThrow(Pose2dComponent component) {
    Translation2d closeTranslation = component.add(TRANSLATION, 0.009);

    Translation2dSubject.assertThat(closeTranslation).isWithin(0.01).of(TRANSLATION);
  }

  @ParameterizedTest
  @ArgumentsSource(Pose2dComponent.TranslationsArgumentsProvider.class)
  public void isWithin_valueNotWithinTolerance_throws(Pose2dComponent component) {
    Translation2d closeTranslation = component.add(TRANSLATION, 0.011);

    assertThrows(
        AssertionError.class,
        () -> Translation2dSubject.assertThat(closeTranslation).isWithin(0.01).of(TRANSLATION));
  }
}
