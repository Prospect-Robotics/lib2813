package com.team2813.lib2813.limelight;

import org.json.JSONObject;

public class LegacyRestLimelightTest extends RestLimelightTest {
  @Override
  protected void setJson(JSONObject json) {
    // All json files in resources has the "Results" root json object, which is now legacy.
    // Therefore, this json object is legacy and this test will make sure legacy still works
    fakeLimelight.setResultsResponse(json);
  }
}
