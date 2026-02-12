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

/** Signifies that a {@index CAN} Id was given to a function, and it was invalid */
public class InvalidCanIdException extends RuntimeException {
  /**
   * The CAN id that is invalid
   *
   * @serial an integer that is not between 0 and 62
   */
  private final int canId;

  public InvalidCanIdException(int canId) {
    super(
        String.format(
            "%d is not a valid can id (a valid can id is between 0 and 62, inclusive)", canId));
    this.canId = canId;
    if (0 <= canId && canId <= 62) {
      throw new IllegalArgumentException(
          String.format("%s is a valid can id (it is between 0 and 62, inclusive)", canId));
    }
  }

  public int getCanId() {
    return canId;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof InvalidCanIdException)) return false;
    InvalidCanIdException other = (InvalidCanIdException) o;
    return getMessage().equals(other.getMessage());
  }

  @Override
  public int hashCode() {
    return getMessage().hashCode();
  }
}
