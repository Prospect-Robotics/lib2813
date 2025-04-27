package com.team2813.lib2813.util;

import static com.google.common.truth.Truth.assertThat;
import static com.team2813.lib2813.util.InvalidCanIdExceptionSubject.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class InputValidationTest {
  // Tests for the `InputValidation.checkCanId(...)` method.
  public static class checkCanIdTest {
    @Test
    public void invalidCanIdTest() {
      // Can IDs can only valid in the range [0, 62].
      int invalidCanIds[] = {-50, -1, 63, 100};
      for (int invalidCanId : invalidCanIds) {
        var exception =
            assertThrows(
                InvalidCanIdException.class, () -> InputValidation.checkCanId(invalidCanId));
        assertThat(exception)
            .hasCanId(invalidCanId)
            .hasMessageThat()
            .contains("is not a valid can id");
      }
    }

    @Test
    public void validCanID() {
      // Can IDs can only valid in the range [0, 62].
      int validCanIds[] = {0, 1, 10, 62};
      for (int validCanId : validCanIds) {
        assertThat(InputValidation.checkCanId(validCanId)).isEqualTo(validCanId);
      }
    }
  }
}
