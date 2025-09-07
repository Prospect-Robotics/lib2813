package com.team2813.lib2813.util;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import java.util.Optional;
import java.util.function.Function;

/**
 * Publishes build constants to NetworkTables.
 *
 * <p>The "build constants" are metadata related to the state of the code at the time it was built,
 * e.g, git branch, git commit, build time, etc. This information can be very valuable when
 * troubleshooting issues with the live code of the robot.
 *
 * <p>BuildConstantsPublisher receives this information from a specially built (as explained below)
 * BuildConstants class at build time. It provides an interface to publish it to NetworkTables
 * ({@link #publish(NetworkTableInstance)}) or print it to the robot console ({@link #log()}) - at
 * runtime. Build constants need to be published only once, typically during robot initialization.
 *
 * <p>The constants are published under the {@code "/Metadata"} table in NetworkTables. This is a
 * special NetworkTables table. Some tools have special support for the "/Metadata" table. For
 * instance, Advantage Scope has a dedicated Metadata tab that loads information like Build
 * Constants in a well formated table view.
 *
 * <p>To instantiate a BuildConstantsPublisher, a build constants class, {@code BuildConstants}, is
 * needs to be generated for the robot library by enabling the `gversion` plugin in the gradle build
 * file.
 *
 * <pre>{@code
 * plugins {
 *   ...
 *   // Plugin needed for Git Build Info
 *   // (see https://docs.wpilib.org/en/stable/docs/software/advanced-gradlerio/deploy-git-data.html)
 *   id 'com.peterabeles.gversion' version '1.10'
 *   ...
 * }
 * ...
 * // Generates a BuildConstants file.
 * // https://docs.wpilib.org/en/stable/docs/software/advanced-gradlerio/deploy-git-data.html
 * project.compileJava.dependsOn(createVersionFile)
 * def BUILD_CONSTANTS_AUTOGEN_PATH = 'build/generated/sources/build_constants/'
 * gversion {
 *     // Build inside build/ (so that it will be ignored by git due to .gitignore)
 *     // and inside build/generated/ (so that it will be ignored by our Spotless
 *     // rules).
 *     srcDir       = BUILD_CONSTANTS_AUTOGEN_PATH
 *     classPackage = 'com.team2813'
 *     className    = 'BuildConstants'
 *     dateFormat   = 'yyyy-MM-dd HH:mm:ss z'
 *     timeZone     = 'America/Los_Angeles' // Use preferred time zone
 *     indent       = '  '
 * }
 * sourceSets.main.java.srcDirs += BUILD_CONSTANTS_AUTOGEN_PATH
 * ...
 * }</pre>
 *
 * <p>With the BuildConstants generation enabled, the publisher is initialized and used like this:
 *
 * <pre>{@code
 * BuildConstantsPublisher buildConstantsPublisher(com.team2813.BuildConstants.class);
 * // Publish the build constants to "/Metadata" on the NetworkTables
 * buildConstantsPublisher.publish(NetworkTableInstance.getDefault());
 * // Log the build constants in the robot console as well.
 * buildConstantsPublisher.log();
 * }</pre>
 */
public final class BuildConstantsPublisher {
  /** The name of the NetworkTable under which the build constants are published. */
  public static final String METADATA_TABLE_NAME = "Metadata";

  private final Optional<BuildConstantsRecord> constants;

  /**
   * Constructs a BuildConstantsPublisher.
   *
   * <p>This constructor creates publishers for each build constant and publishes them to the
   * provided network table instance.
   *
   * @param buildConstantsClass Specially built class that contains the robot code built-time
   *     constants. See class description for instructions on how to generate the class.
   */
  public BuildConstantsPublisher(Class<?> buildConstantsClass) {
    constants = BuildConstantsRecord.fromGeneratedClass(buildConstantsClass);
  }

  /** Gets the build constants extracted from the publisher. */
  public Optional<BuildConstants> buildConstants() {
    return constants.map(Function.identity());
  }

  /**
   * Publishes the build constants to NetworkTables.
   *
   * <p>This is typically called once during robot initialization.
   *
   * @param ntInstance The top-level NetworkTable instance under whose "/Metadata" table the build
   *     constants are published.
   */
  public void publish(NetworkTableInstance ntInstance) {
    constants.ifPresent(
        values -> {
          NetworkTable table = ntInstance.getTable(METADATA_TABLE_NAME);
          table.getStringTopic("MavenName").publish().set(values.mavenName());
          table.getIntegerTopic("GitRevision").publish().set(values.gitRevision());
          table.getStringTopic("GitSha").publish().set(values.gitSha());
          table.getStringTopic("GitDate").publish().set(values.gitSubmitTimeString());
          table.getStringTopic("GitBranch").publish().set(values.gitBranch());
          table.getStringTopic("BuildDate").publish().set(values.buildTimeString());
          table.getIntegerTopic("BuildUnixTime").publish().set(values.buildTimeMillis());
          table.getIntegerTopic("Dirty").publish().set(values.dirty());
        });
  }

  /** Logs the build constants to the console. */
  public void log() {
    constants.ifPresent(
        values -> {
          System.out.println("MavenName:     " + values.mavenName());
          System.out.println("GitRevision:   " + values.gitRevision());
          System.out.println("GitSha:        " + values.gitSha());
          System.out.println("GitDate:       " + values.gitSubmitTimeString());
          System.out.println("GitBranch:     " + values.gitBranch());
          System.out.println("BuildDate:     " + values.buildTimeString());
          System.out.println("BuildUnixTime: " + values.buildTimeMillis());
          System.out.println("Dirty:         " + values.dirty());
        });
  }
}
