package com.team2813.lib2813.util;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

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
public class BuildConstantsPublisher {
  /** The name of the NetworkTable under which the build constants are published. */
  public static final String METADATA_TABLE_NAME = "Metadata";

  private String m_mavenName;
  // Don't resolve BuildConstants.MAVEN_GROUP because it is always empty
  private int m_gitRevision;
  // Don't resolve BuildConstants.VERSION because it is always "unspecified".
  private String m_gitSha;
  private String m_gitDate;
  private String m_gitBranch;
  private String m_buildDate;
  private long m_buildUnixTime;
  private int m_dirty;

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
    try {
      m_mavenName = (String) buildConstantsClass.getDeclaredField("MAVEN_NAME").get(null);
      m_gitRevision = (int) buildConstantsClass.getDeclaredField("GIT_REVISION").get(null);
      m_gitSha = (String) buildConstantsClass.getDeclaredField("GIT_SHA").get(null);
      m_gitDate = (String) buildConstantsClass.getDeclaredField("GIT_DATE").get(null);
      m_gitBranch = (String) buildConstantsClass.getDeclaredField("GIT_BRANCH").get(null);
      m_buildDate = (String) buildConstantsClass.getDeclaredField("BUILD_DATE").get(null);
      m_buildUnixTime = (long) buildConstantsClass.getDeclaredField("BUILD_UNIX_TIME").get(null);
      m_dirty = (int) buildConstantsClass.getDeclaredField("DIRTY").get(null);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      // TODO(vdikov): Add a proper error logging here so that developers can catch it when it
      // happens
      e.printStackTrace();
    }
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
    NetworkTable table = ntInstance.getTable(METADATA_TABLE_NAME);
    table.getStringTopic("MavenName").publish().set(m_mavenName);
    table.getIntegerTopic("GitRevision").publish().set(m_gitRevision);
    table.getStringTopic("GitSha").publish().set(m_gitSha);
    table.getStringTopic("GitDate").publish().set(m_gitDate);
    table.getStringTopic("GitBranch").publish().set(m_gitBranch);
    table.getStringTopic("BuildDate").publish().set(m_buildDate);
    table.getIntegerTopic("BuildUnixTime").publish().set(m_buildUnixTime);
    table.getIntegerTopic("Dirty").publish().set(m_dirty);
  }

  /** Logs the build constants to the console. */
  public void log() {
    System.out.println("MavenName:     " + m_mavenName);
    System.out.println("GitRevision:   " + m_gitRevision);
    System.out.println("GitSha:        " + m_gitSha);
    System.out.println("GitDate:       " + m_gitDate);
    System.out.println("GitBranch:     " + m_gitBranch);
    System.out.println("BuildDate:     " + m_buildDate);
    System.out.println("BuildUnixTime: " + m_buildUnixTime);
    System.out.println("Dirty:         " + m_dirty);
  }
}
