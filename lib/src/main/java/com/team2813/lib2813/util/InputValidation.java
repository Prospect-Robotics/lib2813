package com.team2813.lib2813.util;

import java.util.function.IntFunction;

/**
 * Utility class for input validation.
 *
 * <p>Provides methods for validating IDs and other numeric inputs. This class is non-instantiable
 * and should only be used via its static methods.
 *
 * @author Team 2813
 */
public class InputValidation {
  private InputValidation() {
    throw new AssertionError("non instantiable");
  }

  /**
   * Checks if the given value is within the provided bounds.
   *
   * @param lower the lower bound
   * @param upper the upper bound
   * @param actual the actual value
   * @param throwable a function that takes the actual value and returns an unchecked exception
   * @throws RuntimeException when {@code actual} is not in the range [{@code lower}, {@code upper}]
   */
  private static void checkBounds(
      int lower, int upper, int actual, IntFunction<? extends RuntimeException> throwable) {
    assert lower <= upper;
    if (actual < lower || upper < actual) {
      throw throwable.apply(actual);
    }
  }

  /**
   * Checks if the input is a valid {@index CAN} ID, and throws an exception if it is not.
   *
   * @param id the CAN ID, between 0 and 62 inclusive
   * @return the {@code id} if valid
   * @throws InvalidCanIdException if the CAN ID is invalid
   */
  public static int checkCanId(int id) {
    checkBounds(0, 62, id, InvalidCanIdException::new);
    return id;
  }
}
