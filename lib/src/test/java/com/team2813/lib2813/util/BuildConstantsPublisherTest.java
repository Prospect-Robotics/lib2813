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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.junit.jupiter.api.Test;

public class BuildConstantsPublisherTest {
  // Fake constants copied and adapted from this article
  // https://docs.wpilib.org/en/stable/docs/software/advanced-gradlerio/deploy-git-data.html
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
    public static final int DIRTY = 0;

    private FakeBuildConstants() {}
  }

  /**
   * A Truth {@link Subject} for asserting properties of strings that should parse as {@link
   * LocalDateTime}.
   *
   * <p>Composed with the help of Gemini: https://g.co/gemini/share/d8db68a8fbaf
   */
  private class DateTimeStringSubject extends Subject {
    private final String actual;
    // The format must be consistent with the `createVersionFile` settings in the build.gradle.
    private final DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    private DateTimeStringSubject(FailureMetadata metadata, String actual) {
      super(metadata, actual);
      this.actual = actual;
    }

    public void parsesAsLocalDateTime() {
      if (actual == null) {
        failWithActual(simpleFact("expected to parse as LocalDateTime, but was null"));
        return;
      }

      try {
        LocalDateTime.parse(actual, formatter);
      } catch (DateTimeParseException e) {
        failWithActual(
            fact("expected to parse as LocalDateTime with format", formatter),
            fact("but parsing failed with", e.getMessage()));
      }
    }
  }

  /**
   * Returns the value of the given key in the given table, or an empty string if the key is not
   * present.
   */
  private String getStringEntryOrEmpty(NetworkTable table, String key) {
    return table.getStringTopic(key).getEntry("").get();
  }

  /**
   * Returns the value of the given key in the given table, or the given default value if the key is
   * not present.
   */
  private Long getIntegerEntryOrDefault(NetworkTable table, String key, long defaultValue) {
    return table.getIntegerTopic(key).getEntry(defaultValue).get();
  }

  @Test
  public void publishesBuildConstantsToNetworkTables() {
    // Arrange.
    NetworkTableInstance ntInstance = NetworkTableInstance.create();
    BuildConstantsPublisher publisher = new BuildConstantsPublisher(FakeBuildConstants.class);
    NetworkTable table = ntInstance.getTable(BuildConstantsPublisher.TABLE_NAME);

    // Act.
    publisher.publish(ntInstance);

    // Assert.
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
    assertThat(getIntegerEntryOrDefault(table, "Dirty", -1)).isAnyOf(0l, 1l);
  }

  @Test
  public void logsBuildConstantsToConsole() {
    // Arrange.

    BuildConstantsPublisher publisher = new BuildConstantsPublisher(FakeBuildConstants.class);

    // Keep the original System.out
    PrintStream originalOut = System.out;

    // Redirect System.out to a ByteArrayOutputStream
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    // Act.
    publisher.log();

    // Assert.
    try {
      assertThat(outputStream.toString())
          .containsMatch(
              // NOTE that \r?\n is used to match both Windows (\r\n) and Unix (\n) line endings.
              "MavenName:     2813Robot\r?\n"
                  // Matches a Git revision number, e.g., "121"
                  + "GitRevision:   [0-9]+\r?\n"
                  // Matches a Git revision hash, e.g., "08205a25fe10c6c6c1ea4db2deabb4aaf4617637"
                  // Accepts "NA" for users that have no git installed.
                  + "GitSha:        (NA|[0-9a-f]{40})\r?\n"
                  // Matches a Git date, e.g., "2023-10-01 12:34:56 PDT"
                  + "GitDate:       \\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.+\r?\n"
                  // Matches a Git branch name, e.g., "main"
                  + "GitBranch:     .+\r?\n"
                  // Matches a build date, e.g., "2023-10-01 12:34:56 PDT"
                  + "BuildDate:     \\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.+\r?\n"
                  // Matches a Unix timestamp, e.g., "1696175696"
                  + "BuildUnixTime: \\d+\r?\n"
                  // Matches a dirty flag, e.g., "0" or "1"
                  + "Dirty:         [01]\r?\n");
    } finally {
      // Restore System.out
      System.setOut(originalOut);
    }
  }
}
