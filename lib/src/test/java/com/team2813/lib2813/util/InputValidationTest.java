package com.team2813.lib2813.util;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.assertThrows;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

/**
 * Unit tests for the {@link InputValidation} utility class.
 *
 * <p>This test class specifically verifies that CAN ID validation behaves correctly, throwing an
 * {@link InvalidCanIdException} for invalid IDs and returning the ID unchanged for valid IDs.
 *
 * <p>Uses the {@link Enclosed} runner to group tests logically in nested static classes.
 */
@RunWith(Enclosed.class)
public class InputValidationTest {

  /**
   * Tests for the {@link InputValidation#checkCanId(int)} method.
   *
   * <p>CAN IDs are valid in the range [0, 62]. This class ensures that invalid IDs throw {@link
   * InvalidCanIdException} and that valid IDs return unchanged.
   */
  public static class CheckCanIdTest {

    /**
     * Verifies that {@link InputValidation#checkCanId(int)} throws {@link InvalidCanIdException}
     * for IDs outside the valid range [0, 62].
     */
    @Test
    public void invalidCanId() {
      int[] invalidCanIds = {-50, -1, 63, 100};
      for (int invalidCanId : invalidCanIds) {
        InvalidCanIdException exception =
            assertThrows(
                InvalidCanIdException.class, () -> InputValidation.checkCanId(invalidCanId));
        // Verify that the exception contains the invalid ID
        assertThat(exception.getCanId()).isEqualTo(invalidCanId);
        // Verify that the exception message mentions invalid CAN ID
        assertThat(exception).hasMessageThat().contains("is not a valid CAN ID");
      }
    }

    /**
     * Verifies that {@link InputValidation#checkCanId(int)} returns the original ID for valid CAN
     * IDs in the range [0, 62].
     */
    @Test
    public void validCanID() {
      int[] validCanIds = {0, 1, 10, 62};
      for (int validCanId : validCanIds) {
        int returnValue = InputValidation.checkCanId(validCanId);
        assertWithMessage("Expected a valid CAN ID").that(returnValue).isEqualTo(validCanId);
      }
    }
  }
}
