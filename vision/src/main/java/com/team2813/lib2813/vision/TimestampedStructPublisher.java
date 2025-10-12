package com.team2813.lib2813.vision;

import edu.wpi.first.networktables.StructPublisher;
import edu.wpi.first.networktables.StructTopic;
import edu.wpi.first.units.TimeUnit;
import edu.wpi.first.units.Units;
import edu.wpi.first.wpilibj.TimedRobot;
import java.util.List;
import java.util.function.Supplier;

/**
 * Publishes timestamped data to a network tables topic.
 *
 * <p>If an empty list is passed to the {@link #publish(List)} method, then a zero value is
 * published only if the most recent published data is too far in the past. This can reduce
 * flickering when the data is displayed in tools like AdvantageScope. It is particularly useful for
 * data that takes longer to produce than the frequency of the robot's event loop (for example,
 * estimated robot pose data produced by a camera).
 */
final class TimestampedStructPublisher<S> {
  private static final long MICROS_PER_SECOND = 1_000_000;
  static final long EXPECTED_UPDATE_FREQUENCY_MICROS =
      (long) (TimedRobot.kDefaultPeriod * MICROS_PER_SECOND);
  static final long DEFAULT_PUBLISHED_VALUE_VALID_MICROS = 2 * EXPECTED_UPDATE_FREQUENCY_MICROS;
  private long publishedValueValidMicros = DEFAULT_PUBLISHED_VALUE_VALID_MICROS;

  private final StructPublisher<S> publisher;
  private final Supplier<Double> fpgaTimestampSupplier;
  private final S zeroValue;
  private long lastUpdateTimeMicros;
  private boolean publishedZeroValue;

  /**
   * Creates a publisher.
   *
   * @param topic Topic to publish to.
   * @param zeroValue Value to publish when data is determined to be stale.
   * @param fpgaTimestampSupplier Supplies FPGA timestamps in seconds.
   */
  TimestampedStructPublisher(
      StructTopic<S> topic, S zeroValue, Supplier<Double> fpgaTimestampSupplier) {
    this.fpgaTimestampSupplier = fpgaTimestampSupplier;
    this.zeroValue = zeroValue;
    this.publisher = topic.publish();
    this.publisher.set(zeroValue, 1);
    publishedZeroValue = true;
  }

  /**
   * Sets the maximum amount of time that can pass before a published value can be considered stale.
   *
   * @param time Amount of time.
   * @param timeUnit Units for the time parameter.
   */
  public void setTimeUntilStale(long time, TimeUnit timeUnit) {
    publishedValueValidMicros = (long) Math.floor(Units.Microseconds.convertFrom(time, timeUnit));
  }

  /**
   * Publishes the values to network tables.
   *
   * <p>This should be called in a <a
   * href="https://docs.wpilib.org/en/stable/docs/software/convenience-features/scheduling-functions.html">periodic
   * method</a> once per loop, even if no data is currently available.
   */
  public void publish(List<TimestampedValue<S>> timestampedValues) {
    if (timestampedValues.isEmpty()) {
      if (!publishedZeroValue) {
        long currentTimeMicros = currentTimeMicros();
        long microsSinceLastUpdate = currentTimeMicros - lastUpdateTimeMicros;
        if (microsSinceLastUpdate > publishedValueValidMicros) {
          long timestamp = lastUpdateTimeMicros + EXPECTED_UPDATE_FREQUENCY_MICROS;
          publisher.set(zeroValue, timestamp);
          publishedZeroValue = true;
        }
      }
    } else {
      for (var timestampedValue : timestampedValues) {
        long timestampMicros = timestampedValue.networkTablesTimestampMicros();
        lastUpdateTimeMicros = Math.max(lastUpdateTimeMicros, timestampMicros);
        publisher.set(timestampedValue.value(), timestampMicros);
      }
      publishedZeroValue = false;
    }
  }

  private long currentTimeMicros() {
    return (long) (fpgaTimestampSupplier.get() * MICROS_PER_SECOND);
  }
}
