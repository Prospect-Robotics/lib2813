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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class InputValidationTest {

  // Tests for the `InputValidation.checkCanId(...)` method.
  @Nested
  public class CheckCanIdTest {
    @Test
    public void invalidCanId() {
      // Can IDs can only valid in the range [0, 62].
      int[] invalidCanIds = {-50, -1, 63, 100};
      for (int invalidCanId : invalidCanIds) {
        InvalidCanIdException exception =
            assertThrows(
                InvalidCanIdException.class, () -> InputValidation.checkCanId(invalidCanId));
        assertThat(exception.getCanId()).isEqualTo(invalidCanId);
        assertThat(exception).hasMessageThat().contains("is not a valid can id");
      }
    }

    @Test
    public void validCanID() {
      // Can IDs can only valid in the range [0, 62].
      int[] validCanIds = {0, 1, 10, 62};
      for (int validCanId : validCanIds) {
        int returnValue = InputValidation.checkCanId(validCanId);
        assertWithMessage("Expected a valid CAN ID").that(returnValue).isEqualTo(validCanId);
      }
    }
  }
}
