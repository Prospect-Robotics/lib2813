package com.team2813.lib2813.util;

import static com.google.common.truth.Fact.fact;
import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assertThat;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link BuildConstantsPublisher}.
 *
 * <p>This test validates that build constants can be correctly extracted, published to
 * NetworkTables, and logged to the console. It also includes a custom Truth {@link Subject} for
 * verifying that string representations of dates parse as {@link LocalDateTime}.
 */
public class BuildConstantsPublisherTest {

  /** Date format used to parse and verify build and git dates. */
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

  /**
   * Fake build constants to simulate Gradle-generated build information.
   *
   * <p>Values are adapted from WPILib documentation examples.
   */
  public final class FakeBuildConstants {
    public static final String MAVEN_GROUP = "";
    public static final String MAVEN_NAME = "2813Robot";
    public static final String VERSION = "unspecified";
    public static final int GIT_REVISION = 1;
    public static final String GIT_SHA = "fad108a4b1c1dcdfc8859c6295ea64e06d43f557";
    public static final String GIT_DATE = "2023-10-26 17:38:59 EDT";
    public static final String GIT_BRANCH = "main";
    public static final String BUILD_DATE = "2023-10-27 12:29:57 EDT";
    public static final long BUILD_UNIX_TIME = 1698424197122L;
    public static final int DIRTY = 1;

    private FakeBuildConstants() {}
  }

  /**
   * Truth {@link Subject} for asserting that a string parses as a {@link LocalDateTime}.
   *
   * <p>Useful for validating build and git date formats in tests.
   */
  private class DateTimeStringSubject extends Subject {
    private final String actual;

    private DateTimeStringSubject(FailureMetadata metadata, String actual) {
      super(metadata, actual);
      this.actual = actual;
    }

    /**
     * Asserts that the string parses as {@link LocalDateTime} using {@link #DATE_TIME_FORMATTER}.
     */
    public void parsesAsLocalDateTime() {
      if (actual == null) {
        failWithActual(simpleFact("expected to parse as LocalDateTime, but was null"));
        return;
      }

      try {
        LocalDateTime.parse(actual, DATE_TIME_FORMATTER);
      } catch (DateTimeParseException e) {
        failWithActual(
            fact("expected to parse as LocalDateTime with format", DATE_TIME_FORMATTER),
            fact("but parsing failed with", e.getMessage()));
      }
    }
  }

  /** Returns the string value of a key in a NetworkTable, or empty if the key is not present. */
  private String getStringEntryOrEmpty(NetworkTable table, String key) {
    return table.getStringTopic(key).getEntry("").get();
  }

  /**
   * Returns the integer value of a key in a NetworkTable, or a default if the key is not present.
   */
  private Long getIntegerEntryOrDefault(NetworkTable table, String key, long defaultValue) {
    return table.getIntegerTopic(key).getEntry(defaultValue).get();
  }

  /**
   * Tests that {@link BuildConstantsPublisher#buildConstants()} correctly extracts build constants
   * from the fake class.
   */
  @Test
  public void extractsBuildConstants() {
    BuildConstantsPublisher publisher = new BuildConstantsPublisher(FakeBuildConstants.class);

    var constants = publisher.buildConstants();

    ZonedDateTime expectedBuildTime =
        ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(FakeBuildConstants.BUILD_UNIX_TIME),
                ZoneId.of("America/New_York"))
            .withNano(0);

    ZonedDateTime expectedGitCommitTime =
        ZonedDateTime.parse(FakeBuildConstants.GIT_DATE, DATE_TIME_FORMATTER);

    var expectedRecord =
        new BuildConstantsRecord(
            FakeBuildConstants.MAVEN_NAME,
            FakeBuildConstants.GIT_REVISION,
            FakeBuildConstants.GIT_SHA,
            FakeBuildConstants.GIT_BRANCH,
            expectedGitCommitTime,
            expectedBuildTime,
            FakeBuildConstants.BUILD_UNIX_TIME,
            FakeBuildConstants.DIRTY);

    assertThat(constants).hasValue(expectedRecord);
  }

  /**
   * Tests that {@link BuildConstantsPublisher#publish(NetworkTableInstance)} correctly publishes
   * build constants to NetworkTables.
   */
  @Test
  public void publishesBuildConstantsToNetworkTables() {
    NetworkTableInstance ntInstance = NetworkTableInstance.create();
    BuildConstantsPublisher publisher = new BuildConstantsPublisher(FakeBuildConstants.class);
    NetworkTable table = ntInstance.getTable(BuildConstantsPublisher.METADATA_TABLE_NAME);

    publisher.publish(ntInstance);

    assertThat(table).isNotNull();
    assertThat(table.getKeys())
        .containsExactly(
            "MavenName",
            "GitRevision",
            "GitSha",
            "GitDate",
            "GitBranch",
            "BuildUnixTime",
            "BuildDate",
            "Dirty");

    assertThat(getStringEntryOrEmpty(table, "MavenName")).isEqualTo("2813Robot");
    assertThat(getIntegerEntryOrDefault(table, "GitRevision", 0)).isGreaterThan(0);
    assertThat(getStringEntryOrEmpty(table, "GitSha")).isNotEmpty();
    assertThat(getStringEntryOrEmpty(table, "GitDate")).isNotEmpty();
    assertThat(getStringEntryOrEmpty(table, "GitBranch")).isNotEmpty();
    assertThat(getIntegerEntryOrDefault(table, "BuildUnixTime", 0)).isNotEqualTo(0);
    assertThat(getStringEntryOrEmpty(table, "BuildDate")).isNotEmpty();

    assertAbout(DateTimeStringSubject::new)
        .that(getStringEntryOrEmpty(table, "BuildDate"))
        .parsesAsLocalDateTime();

    assertThat(getIntegerEntryOrDefault(table, "Dirty", -1)).isAnyOf(0L, 1L);

    ntInstance.close();
  }

  /**
   * Tests that {@link BuildConstantsPublisher#log()} correctly prints build constants to the
   * console in the expected format.
   */
  @Test
  public void logsBuildConstantsToConsole() {
    BuildConstantsPublisher publisher = new BuildConstantsPublisher(FakeBuildConstants.class);

    PrintStream originalOut = System.out;
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    try {
      publisher.log();

      assertThat(outputStream.toString())
          .containsMatch(
              "MavenName:     2813Robot\r?\n"
                  + "GitRevision:   [0-9]+\r?\n"
                  + "GitSha:        (NA|[0-9a-f]{40})\r?\n"
                  + "GitDate:       \\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.+\r?\n"
                  + "GitBranch:     .+\r?\n"
                  + "BuildDate:     \\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.+\r?\n"
                  + "BuildUnixTime: \\d+\r?\n"
                  + "Dirty:         [01]\r?\n");
    } finally {
      System.setOut(originalOut);
    }
  }
}
