package com.team2813.lib2813.limelight;

import edu.wpi.first.networktables.DoubleArrayEntry;
import edu.wpi.first.networktables.NetworkTableEntry;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;

/**
 * Unit test class for {@link NetworkTablesLimelight}.
 *
 * <p>This test framework simulates Limelight JSON data being written into NetworkTables and
 * validates how the {@link Limelight} implementation handles pose estimation entries.
 */
public class NetworkTablesLimelightTest extends LimelightTestCase {

  /** Name of the Limelight NetworkTable. */
  private static final String TABLE_NAME = "limelight";

  /** Half a millisecond in microseconds. */
  private static final long ONE_HALF_MS_IN_MICROS = 500;

  /** One second in microseconds. */
  private static final long ONE_SECOND_IN_MICROS = 1_000_000;

  /** Simulated timestamp in microseconds, advanced between test steps. */
  private static long fakeTimestampMicros = 15 * ONE_SECOND_IN_MICROS;

  /**
   * List of NetworkTables entries used for storing bot pose estimates. These cover both red/blue
   * alliances and orb-based estimates.
   */
  private static final List<String> BOT_POSE_ESTIMATE_ENTRIES =
      List.of(
          "botpose",
          "botpose_wpired",
          "botpose_orb_wpired",
          "botpose_wpiblue",
          "botpose_orb_wpiblue");

  /**
   * Resets Limelight NetworkTable entries after each test.
   *
   * <p>Clears the JSON entry and all bot pose estimate entries, advancing the fake timestamp by two
   * half-millisecond steps.
   */
  @After
  public void resetNetworkTables() {
    getJsonNTEntry().setString("");

    fakeTimestampMicros += ONE_HALF_MS_IN_MICROS;
    for (String entryName : BOT_POSE_ESTIMATE_ENTRIES) {
      clearBotPoseEstimate(entryName, fakeTimestampMicros);
    }
    fakeTimestampMicros += ONE_HALF_MS_IN_MICROS;
  }

  /**
   * Creates a new {@link NetworkTablesLimelight} instance for testing.
   *
   * @return a Limelight instance connected to the {@value #TABLE_NAME} table
   */
  @Override
  protected Limelight createLimelight() {
    return new NetworkTablesLimelight(TABLE_NAME);
  }

  /**
   * Sets the Limelight JSON data in NetworkTables.
   *
   * <p>Copies JSON pose estimation arrays into their corresponding NetworkTable entries when
   * latency values are available.
   *
   * @param json the simulated Limelight JSON response
   */
  @Override
  protected void setJson(JSONObject json) {
    JSONObject resultsJson = json.getJSONObject("Results");
    getJsonNTEntry().setString(resultsJson.toString());

    // Copy "botpose_orb_wpired" and "botpose_orb_wpiblue" data to Network tables.
    if (resultsJson.has("cl") && resultsJson.has("tl")) {
      double latencyMillis = resultsJson.getDouble("cl") + resultsJson.getDouble("tl");
      for (String entryName : BOT_POSE_ESTIMATE_ENTRIES) {
        setBotPoseEstimate(resultsJson, entryName, latencyMillis);
      }
    }
  }

  /**
   * Returns the JSON {@link NetworkTableEntry} for Limelight.
   *
   * @return the NetworkTables entry holding Limelight JSON
   */
  private static NetworkTableEntry getJsonNTEntry() {
    return LimelightHelpers.getLimelightNTTableEntry(TABLE_NAME, "json");
  }

  /**
   * Writes a bot pose estimate array into the specified NetworkTables entry.
   *
   * @param resultsJson the Limelight "Results" JSON object
   * @param entryName the entry name to populate
   * @param latencyMillis the latency value in milliseconds
   */
  private static void setBotPoseEstimate(
      JSONObject resultsJson, String entryName, double latencyMillis) {
    double[] estimate_array = getBotPoseEstimateArray(resultsJson, entryName, latencyMillis);
    DoubleArrayEntry tableEntry =
        LimelightHelpers.getLimelightDoubleArrayEntry(TABLE_NAME, entryName);
    tableEntry.set(estimate_array, fakeTimestampMicros);
  }

  /**
   * Clears a bot pose estimate entry by setting it to an empty array.
   *
   * @param entryName the NetworkTables entry to clear
   * @param timestampMicros the timestamp in microseconds
   */
  private static void clearBotPoseEstimate(String entryName, long timestampMicros) {
    DoubleArrayEntry tableEntry =
        LimelightHelpers.getLimelightDoubleArrayEntry(TABLE_NAME, entryName);
    tableEntry.set(new double[0], timestampMicros);
  }

  /**
   * Builds a bot pose estimate array from Limelight JSON data.
   *
   * <p>The array is structured as:
   *
   * <ul>
   *   <li>Indices 0–5: pose data from Limelight
   *   <li>Index 6: latency in milliseconds
   *   <li>Index 7: tag count
   *   <li>Index 8: tag ID
   * </ul>
   *
   * Additional indices are reserved for multiple tags.
   *
   * @param resultsJson the Limelight "Results" JSON object
   * @param entryName the entry to extract
   * @param latencyMillis the latency in milliseconds
   * @return a double array representing the bot pose estimate, or an empty array if not present
   */
  private static double[] getBotPoseEstimateArray(
      JSONObject resultsJson, String entryName, double latencyMillis) {
    if (!resultsJson.has(entryName)) {
      return new double[0]; // getBotPoseEstimate_wpi{Blue,Red}() will return null
    }
    int tagCount = 1;
    double[] result = new double[11 + 7 * tagCount];
    JSONArray array = resultsJson.getJSONArray(entryName);
    for (int i = 0; i < 6; i++) {
      result[i] = array.getDouble(i);
    }
    result[6] = latencyMillis;
    result[7] = tagCount;
    result[8] = 1; // tagId
    return result;
  }
}
