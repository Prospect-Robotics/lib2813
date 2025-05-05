/*
Copyright 2023-2026 Prospect Robotics SWENext Club

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
package com.team2813.lib2813.util;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.assertThrows;

import java.util.stream.IntStream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests for {@link InputValidation}. */
public class InputValidationTest {

  /** Tests for the `InputValidation.checkCanId(...)` method. */
  @Nested
  public class CheckCanIdTest {
    @ParameterizedTest
    @ValueSource(ints = {-50, -1, 63, 100}) // Can IDs can only valid in the range [0, 62]
    public void invalidCanId(int invalidCanId) {
      InvalidCanIdException exception =
          assertThrows(InvalidCanIdException.class, () -> InputValidation.checkCanId(invalidCanId));

      assertThat(exception.getCanId()).isEqualTo(invalidCanId);
      assertThat(exception).hasMessageThat().contains("is not a valid can id");
    }

    @ParameterizedTest
    @MethodSource("validCanIds")
    public void checkCanId_returnsInputForValidCanId(int validCanId) {
      int returnValue = InputValidation.checkCanId(validCanId);

      assertWithMessage("Expected to return the passed-in value")
          .that(returnValue)
          .isEqualTo(validCanId);
    }

    private static IntStream validCanIds() {
      return IntStream.range(0, 63);
    }
  }
}
