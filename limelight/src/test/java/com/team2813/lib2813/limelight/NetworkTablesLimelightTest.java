package com.team2813.lib2813.limelight;

import edu.wpi.first.networktables.NetworkTableEntry;
import org.json.JSONObject;
import org.junit.After;

public class NetworkTablesLimelightTest extends LimelightTestCase {
  private static final String TABLE_NAME = "limelight";

  @After
  public void resetNetworkTables() {
    getJsonNTEntry().setString("");
  }

  @Override
  protected Limelight createLimelight() {
    return new NetworkTablesLimelight(TABLE_NAME);
  }

  @Override
  protected void setJson(JSONObject json) {
    getJsonNTEntry().setString(json.getJSONObject("Results").toString());
  }
  
  private static NetworkTableEntry getJsonNTEntry() {
    return LimelightHelpers.getLimelightNTTableEntry(TABLE_NAME, "json");
  }
}
