package com.team2813.lib2813.limelight;

import edu.wpi.first.networktables.DoubleArrayEntry;
import edu.wpi.first.networktables.NetworkTableEntry;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;

import java.util.List;

public class NetworkTablesLimelightTest extends LimelightTestCase {
  private static final String TABLE_NAME = "limelight";
  private static final long ONE_HALF_MS_IN_MICROS = 500;
  private static final long ONE_SECOND_IN_MICROS = 1_000_000;
  private static long fakeTimestampMicros = 15 * ONE_SECOND_IN_MICROS;
  private static final List<String> BOT_POSE_ESTIMATE_ENTRIES = List.of(
          "botpose",
          "botpose_wpired", "botpose_orb_wpired",
          "botpose_wpiblue", "botpose_orb_wpiblue");

  @After
  public void resetNetworkTables() {
    getJsonNTEntry().setString("");

    fakeTimestampMicros += ONE_HALF_MS_IN_MICROS;
    for (String entryName : BOT_POSE_ESTIMATE_ENTRIES) {
      clearBotPoseEstimate(entryName, fakeTimestampMicros);
    }
    fakeTimestampMicros += ONE_HALF_MS_IN_MICROS;
  }

  @Override
  protected Limelight createLimelight() {
    return new NetworkTablesLimelight(TABLE_NAME);
  }

  @Override
  protected void setJson(JSONObject json) {
    JSONObject resultsJson = json.getJSONObject("Results");
    getJsonNTEntry().setString(resultsJson.toString());

    // Copy "botpose_orb_wpired" and "botpose_orb_wpiblue" data to Network tables.
    double latencyMillis = resultsJson.getDouble("cl") + resultsJson.getDouble("tl");
    for (String entryName : BOT_POSE_ESTIMATE_ENTRIES) {
      setBotPoseEstimate(resultsJson, entryName, latencyMillis);
    }
  }

  private static NetworkTableEntry getJsonNTEntry() {
    return LimelightHelpers.getLimelightNTTableEntry(TABLE_NAME, "json");
  }

  private static void setBotPoseEstimate(JSONObject resultsJson, String entryName, double latencyMillis) {
    double[] estimate_array = getBotPoseEstimateArray(resultsJson, entryName, latencyMillis);
    DoubleArrayEntry tableEntry =
        LimelightHelpers.getLimelightDoubleArrayEntry(TABLE_NAME, entryName);
    tableEntry.set(estimate_array, fakeTimestampMicros);
  }

  private static void clearBotPoseEstimate(String entryName, long timestampMicros) {
    DoubleArrayEntry tableEntry =
            LimelightHelpers.getLimelightDoubleArrayEntry(TABLE_NAME, entryName);
    tableEntry.set(new double[0], timestampMicros);
  }

  private static double[] getBotPoseEstimateArray(JSONObject resultsJson, String entryName, double latencyMillis) {
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
