/*
Copyright 2025-2026 Prospect Robotics SWENext Club

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

import static com.google.common.truth.Truth.assertThat;
import static com.team2813.lib2813.vision.TimestampedStructPublisher.DEFAULT_PUBLISHED_VALUE_VALID_MICROS;
import static com.team2813.lib2813.vision.TimestampedStructPublisher.EXPECTED_UPDATE_FREQUENCY_MICROS;

import com.team2813.lib2813.testing.junit.jupiter.ProvideUniqueNetworkTableInstance;
import edu.wpi.first.math.geometry.*;
import edu.wpi.first.networktables.*;
import edu.wpi.first.units.Units;
import java.util.*;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

/** Tests for {@link TimestampedStructPublisher}. */
@ProvideUniqueNetworkTableInstance(waitForListenerQueueSeconds = 0.6)
public class TimestampedStructPublisherTest {
  private static final long MICROSECONDS_PER_SECOND = 1_000_000;
  private static final Translation2d DEFAULT_VALUE = new Translation2d(28, 13);
  private static final String TABLE_NAME = "gearHeads";
  private static final String TOPIC_NAME = "championships";

  private final FakeClock fakeClock = new FakeClock();

  private TimestampedStructPublisher<Translation2d> createPublisher(
      NetworkTableInstance ntInstance) {
    NetworkTable table = ntInstance.getTable(TABLE_NAME);
    return new TimestampedStructPublisher<>(
        table.getStructTopic(TOPIC_NAME, Translation2d.struct), Translation2d.kZero, fakeClock);
  }

  @Test
  public void constructorPublishesZeroValue(NetworkTableInstance ntInstance) {
    // Arrange
    var topic = getTopic(ntInstance);

    try (StructSubscriber<Translation2d> subscriber = topic.subscribe(DEFAULT_VALUE)) {
      // Act
      createPublisher(ntInstance);

      // Assert
      List<TimestampedValue<Translation2d>> publishedValues =
          TimestampedValue.readQueue(subscriber);
      TimestampedValue<Translation2d> expectedValue =
          TimestampedValue.withFpgaTimestampMicros(1, Translation2d.kZero);
      assertThat(publishedValues).containsExactly(expectedValue);
    }
  }

  @Test
  public void publish_withOneValue(NetworkTableInstance ntInstance) {
    // Arrange
    var topic = getTopic(ntInstance);

    try (StructSubscriber<Translation2d> subscriber = topic.subscribe(DEFAULT_VALUE)) {
      TimestampedStructPublisher<Translation2d> publisher = createPublisher(ntInstance);
      long firstFpgaTimestampMillis = 25;
      Translation2d value = new Translation2d(7.35, 0.708);
      TimestampedValue<Translation2d> valueToPublish =
          TimestampedValue.withFpgaTimestamp(firstFpgaTimestampMillis, Units.Milliseconds, value);

      // Act
      publisher.publish(List.of(valueToPublish));

      // Assert
      List<TimestampedValue<Translation2d>> publishedValues =
          TimestampedValue.readQueue(subscriber);
      var expectedValue =
          TimestampedValue.withFpgaTimestampMicros(firstFpgaTimestampMillis * 1_000, value);
      assertThat(publishedValues).containsExactly(expectedValue);
    }
  }

  @Test
  public void publish_withManyValues(NetworkTableInstance ntInstance) {
    // Arrange
    var topic = getTopic(ntInstance);

    try (StructSubscriber<Translation2d> subscriber =
        topic.subscribe(DEFAULT_VALUE, PubSubOption.pollStorage(5))) {
      TimestampedStructPublisher<Translation2d> publisher = createPublisher(ntInstance);
      long firstFpgaTimestampMicros = 25;

      List<TimestampedValue<Translation2d>> valuesToPublish = new ArrayList<>(3);
      for (int i = 0; i < 3; i++) {
        Translation2d value = new Translation2d(7.35 + i, 0.708);
        TimestampedValue<Translation2d> valueToPublish =
            TimestampedValue.withFpgaTimestampMicros(firstFpgaTimestampMicros + i * 10, value);
        valuesToPublish.add(valueToPublish);
      }
      assertThat(subscriber.readQueue()).hasLength(1);

      // Act
      publisher.publish(valuesToPublish);

      // Assert
      List<TimestampedValue<Translation2d>> publishedValues =
          TimestampedValue.readQueue(subscriber);

      assertThat(publishedValues).containsExactlyElementsIn(valuesToPublish);
    }
  }

  @Test
  public void publish_withEmptyList_withStalePreviousValue(NetworkTableInstance ntInstance) {
    // Arrange
    var topic = getTopic(ntInstance);

    try (StructSubscriber<Translation2d> subscriber =
        topic.subscribe(DEFAULT_VALUE, PubSubOption.pollStorage(5))) {
      TimestampedStructPublisher<Translation2d> publisher = createPublisher(ntInstance);
      long firstFpgaTimestampMicros = 25;
      Translation2d value = new Translation2d(7.35, 0.708);
      TimestampedValue<Translation2d> valueToPublish =
          TimestampedValue.withFpgaTimestampMicros(firstFpgaTimestampMicros, value);
      assertThat(subscriber.readQueue()).hasLength(1); // queued by constructor
      publisher.publish(List.of(valueToPublish));
      assertThat(subscriber.readQueue()).hasLength(1);
      // Advance the clock so that the previously-published data will be considered stale.
      fakeClock.setFpgaTimestampMicros(firstFpgaTimestampMicros);
      fakeClock.incrementFpgaTimestampMicros(DEFAULT_PUBLISHED_VALUE_VALID_MICROS + 1);

      // Act
      publisher.publish(List.of());

      // Assert
      List<TimestampedValue<Translation2d>> publishedValues =
          TimestampedValue.readQueue(subscriber);
      TimestampedValue<Translation2d> expectedValue =
          TimestampedValue.withFpgaTimestampMicros(
              firstFpgaTimestampMicros + EXPECTED_UPDATE_FREQUENCY_MICROS, Translation2d.kZero);
      assertThat(publishedValues).containsExactly(expectedValue);
    }
  }

  @Test
  public void publish_withEmptyList_withNonStalePreviousValue(NetworkTableInstance ntInstance) {
    // Arrange
    var topic = getTopic(ntInstance);

    try (StructSubscriber<Translation2d> subscriber =
        topic.subscribe(DEFAULT_VALUE, PubSubOption.pollStorage(5))) {
      TimestampedStructPublisher<Translation2d> publisher = createPublisher(ntInstance);
      long firstFpgaTimestampMicros = 25;

      Translation2d value = new Translation2d(7.35, 0.708);
      TimestampedValue<Translation2d> valueToPublish =
          TimestampedValue.withFpgaTimestampMicros(firstFpgaTimestampMicros, value);
      assertThat(subscriber.readQueue()).hasLength(1); // queued by constructor
      publisher.publish(List.of(valueToPublish));
      assertThat(subscriber.readQueue()).hasLength(1);
      // Advance the clock, but not as far so that the previously-published data would be considered
      // stale.
      fakeClock.setFpgaTimestampMicros(firstFpgaTimestampMicros);
      fakeClock.incrementFpgaTimestampMicros(DEFAULT_PUBLISHED_VALUE_VALID_MICROS - 1);

      // Act
      publisher.publish(List.of());

      // Assert
      List<TimestampedValue<Translation2d>> publishedValues =
          TimestampedValue.readQueue(subscriber);
      assertThat(publishedValues).isEmpty();
    }
  }

  private StructTopic<Translation2d> getTopic(NetworkTableInstance ntInstance) {
    NetworkTable table = ntInstance.getTable(TABLE_NAME);
    return table.getStructTopic(TOPIC_NAME, Translation2d.struct);
  }

  private static class FakeClock implements Supplier<Double> {
    private double fpgaTimestampSeconds = 2.0;

    @Override
    public Double get() {
      return fpgaTimestampSeconds;
    }

    void setFpgaTimestampMicros(double micros) {
      fpgaTimestampSeconds = micros / MICROSECONDS_PER_SECOND;
    }

    void incrementFpgaTimestampMicros(double micros) {
      fpgaTimestampSeconds += micros / MICROSECONDS_PER_SECOND;
    }
  }
}
