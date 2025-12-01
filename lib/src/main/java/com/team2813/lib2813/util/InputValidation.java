/*
Copyright 2023-2025 Prospect Robotics SWENext Club

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

import java.util.function.IntFunction;

public class InputValidation {
  private InputValidation() {
    throw new AssertionError("non instantiable");
  }

  /**
   * Check if the given value is in the bounds
   *
   * @param lower the lower bound
   * @param upper the upper bound
   * @param actual the actual value
   * @param throwable a function that takes the actual value and returns an unchecked exception
   * @throws RuntimeException when the actual is not in between the bounds. exception is provided by
   *     {@code throwable}
   */
  private static void checkBounds(
      int lower, int upper, int actual, IntFunction<? extends RuntimeException> throwable) {
    assert lower <= upper;
    if (actual < lower || upper < actual) {
      throw throwable.apply(actual);
    }
  }

  /**
   * Checks if the input is a valid {@index CAN} Id, and throws an exception if it isn't
   *
   * @param id the can id, between 0 and 62, inclusive
   * @return the {@code id}
   * @throws InvalidCanIdException if the id is invalid
   */
  public static int checkCanId(int id) {
    checkBounds(0, 62, id, InvalidCanIdException::new);
    return id;
  }
}
