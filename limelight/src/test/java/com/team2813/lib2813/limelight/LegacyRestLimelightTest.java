package com.team2813.lib2813.limelight;

import org.json.JSONObject;

/**
 * Test suite for verifying backward compatibility with legacy Limelight JSON format.
 * 
 * <p>This class extends {@link RestLimelightTest} to ensure that the RestLimelight
 * implementation continues to work with older JSON responses that include a "Results"
 * root object. This legacy format was used in earlier versions of the Limelight API,
 * and maintaining compatibility ensures that existing systems continue to function
 * correctly.
 * 
 * <p>The key difference between this test and the parent class is that all JSON
 * test data files in the resources directory contain the legacy "Results" root
 * object structure, which this test specifically validates.
 * 
 * @see RestLimelightTest
 */
public class LegacyRestLimelightTest extends RestLimelightTest {
  
  /**
   * Sets the JSON response for the fake Limelight using the legacy format.
   * 
   * <p>This override ensures that JSON objects with the legacy "Results" root
   * structure are properly handled. All JSON files in the test resources directory
   * use this legacy format, making this test class essential for verifying
   * backward compatibility.
   * 
   * @param json the JSONObject containing legacy-formatted Limelight data with
   *             a "Results" root object
   */
  @Override
  protected void setJson(JSONObject json) {
    // All json files in resources has the "Results" root json object, which is now legacy.
    // Therefore, this json object is legacy and this test will make sure legacy still works
    fakeLimelight.setResultsResponse(json);
  }
}