/*
Copyright 2025 Prospect Robotics SWENext Club

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

  /**
   * Reads all valid value changes since the last call to {@code readQueue()}.
   *
   * <p>This is a convenience method for use in tests. It calls {@link StructSubscriber#readQueue()}
   * and converts the values to {@code TimestampedValue} values.
   *
   * @param subscriber NetworkTables struct-encoded value subscriber to read from.
   * @return Timestamped values; empty if no valid new changes have been published since the
   *     previous call.
   */
  public static <T> List<TimestampedValue<T>> readQueue(StructSubscriber<T> subscriber) {
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
