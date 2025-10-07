package com.team2813.lib2813.vision;

import edu.wpi.first.networktables.StructSubscriber;
import edu.wpi.first.networktables.TimestampedObject;
import edu.wpi.first.units.TimeUnit;
import edu.wpi.first.units.Units;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Holder for a value to publish to network tables with a provided timestamp.
 *
 * @param <T> type of the value to publish.
 */
final class TimestampedValue<T> {
  private final long networkTablesTimestamp;
  private final T value;

  /**
   * Creates a value associated with a provided FPGA timestamp.
   *
   * @param fpgaTimestamp time from the FPGA hardware clock
   * @param timeUnit the time unit of the fpgaTimestamp parameter
   * @param value the value record or computed at the provided timestamp.
   * @return timestamped value with the provided parameters
   * @param <T> Type of the value
   * @see edu.wpi.first.wpilibj.Timer#getFPGATimestamp()
   */
  public static <T> TimestampedValue<T> withFpgaTimestamp(
      double fpgaTimestamp, TimeUnit timeUnit, T value) {
    return withFpgaTimestampMicros(
        (long) Units.Microseconds.convertFrom(fpgaTimestamp, timeUnit), value);
  }

  /**
   * Creates a value associated with a provided FPGA timestamp in microseconds.
   *
   * @param fpgaTimestamp time from the FPGA hardware clock, in microseconds
   * @param value the value record or computed at the provided timestamp.
   * @return timestamped value with the provided parameters
   * @param <T> type of the value
   * @see edu.wpi.first.wpilibj.Timer#getFPGATimestamp()
   */
  public static <T> TimestampedValue<T> withFpgaTimestampMicros(long fpgaTimestamp, T value) {
    return new TimestampedValue<>(fpgaTimestamp, value);
  }

  /**
   * Creates a value from a NetworkTables timestamped object.
   *
   * @param timestampedObject timestampted object to copy from
   * @return timestamped value
   * @param <T> type of the value
   */
  public static <T> TimestampedValue<T> fromTimestampedObject(
      TimestampedObject<T> timestampedObject) {
    return withFpgaTimestampMicros(timestampedObject.timestamp, timestampedObject.value);
  }

  private TimestampedValue(long fpgaTimestamp, T value) {
    this.networkTablesTimestamp = fpgaTimestamp;
    this.value = Objects.requireNonNull(value);
  }

  static <T> List<TimestampedValue<T>> fromSubscriberQueue(StructSubscriber<T> subscriber) {
    return Arrays.stream(subscriber.readQueue())
        .map(TimestampedValue::fromTimestampedObject)
        .toList();
  }

  /**
   * Gets the network tables timestamp value for this instance.
   *
   * @return the FPGA timestamp in microseconds
   */
  public long networkTablesTimestampMicros() {
    // Note: Per the WPILib documentation at
    // https://docs.wpilib.org/en/stable/docs/software/networktables/networktables-intro.html#timestamps
    // timestamps in NetworkTables are measured in integer microseconds. When the RoboRIO is the
    // NetworkTables server, the server timestamp is the same as the FPGA timestamp returned by
    // Timer.getFPGATimestamp() (except the units are different: NetworkTables uses microseconds,
    // while getFPGATimestamp() returns seconds).
    return networkTablesTimestamp;
  }

  /** Gets the underlying value. */
  public T value() {
    return value;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other instanceof TimestampedValue<?> that) {
      return networkTablesTimestamp == that.networkTablesTimestamp && value.equals(that.value);
    }

    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(networkTablesTimestamp, value);
  }
}
