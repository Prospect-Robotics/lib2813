package com.team2813.lib2813.util;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.assertThrows;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class InputValidationTest {
  // Tests for the `InputValidation.checkCanId(...)` method.
  public static class CheckCanIdTest {
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
