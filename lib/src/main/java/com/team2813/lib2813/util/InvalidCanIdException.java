package com.team2813.lib2813.util;

/**
 * Signifies that a {@index CAN} ID provided to a function is invalid.
 *
 * <p>A valid CAN ID is in the range [0, 62]. If an ID outside this range is passed, this exception
 * is thrown. If the ID is valid, the constructor itself throws an {@link IllegalArgumentException}.
 *
 * <p>This exception stores the invalid CAN ID and provides standard {@code equals} and {@code
 * hashCode} implementations based on the message.
 *
 * @author Team 2813
 */
public class InvalidCanIdException extends RuntimeException {
  /**
   * The CAN ID that caused this exception.
   *
   * @serial an integer that is not between 0 and 62
   */
  private final int canId;

  /**
   * Constructs an InvalidCanIdException for a given CAN ID.
   *
   * @param canId the invalid CAN ID
   * @throws IllegalArgumentException if the provided ID is actually valid (0–62)
   */
  public InvalidCanIdException(int canId) {
    super(
        String.format(
            "%d is not a valid CAN ID (a valid CAN ID is between 0 and 62, inclusive)", canId));
    this.canId = canId;
    if (0 <= canId && canId <= 62) {
      throw new IllegalArgumentException(
          String.format(
              "%d is a valid CAN ID (it is between 0 and 62, inclusive)", canId));
    }
  }

  /** Returns the invalid CAN ID that caused this exception. */
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
