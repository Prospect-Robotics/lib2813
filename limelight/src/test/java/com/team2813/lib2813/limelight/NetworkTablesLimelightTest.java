package com.team2813.lib2813.limelight;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class NetworkTablesLimelightTest {
  private static final String TABLE_NAME = "limelight";

  @After
  public void resetNetworkTables() {
    getJsonNTEntry().setString("");
  }
  
  @Test
  public void emptyValues() {
    NetworkTablesLimelight limelight = new NetworkTablesLimelight(TABLE_NAME);
    
    OptionalDouble latency = limelight.getCaptureLatency();
    
    assertFalse("NetworkTables should be empty", latency.isPresent());
  }

  @Test
  public void absentTest1() throws Exception {
    getJsonNTEntry().setString(readJSON("AbsentTest1.json"));
    NetworkTablesLimelight limelight = new NetworkTablesLimelight(TABLE_NAME);
    
    LocationalData locationalData = limelight.getLocationalData();
    assertFalse(locationalData.getBotpose().isPresent());
    OptionalDouble actualCaptureLatency = locationalData.getCaptureLatency();
    assertAlmostEqual(37.40, actualCaptureLatency, 0.005);
    OptionalDouble actualTargetingLatency = locationalData.getTargetingLatency();
    assertAlmostEqual(38.95, actualTargetingLatency, 0.005);
  }
  
  @Test
  public void presentTest() throws Exception {
    getJsonNTEntry().setString(readJSON("PresentTest1.json"));
    NetworkTablesLimelight limelight = new NetworkTablesLimelight(TABLE_NAME);
    
    LocationalData locationalData = limelight.getLocationalData();
    Optional<Pose3d> actualPose = locationalData.getBotpose();
    assertTrue(actualPose.isPresent());
    OptionalDouble actualCaptureLatency = locationalData.getCaptureLatency();
    assertAlmostEqual(37.40, actualCaptureLatency, 0.005);
    OptionalDouble actualTargetingLatency = locationalData.getTargetingLatency();
    assertAlmostEqual(66.61, actualTargetingLatency, 0.005);
    Rotation3d rotation = new Rotation3d(Math.toRadians(6.817779398227925), Math.toRadians(-25.663211825857257), Math.toRadians(-173.13543891950323));
    Pose3d expectedPose = new Pose3d(7.3505718968031255, 0.7083545864687876, 0.9059300968047116, rotation);
    assertEquals(expectedPose, actualPose.orElse(null));
    assertAlmostEqual(3664865.25, limelight.getTimestamp(), 0.005);
  }

  private static void assertAlmostEqual(double expected, OptionalDouble actual, double delta) {
    actual.ifPresentOrElse(
            d -> assertEquals(expected, d, delta),
            () -> assertEquals(OptionalDouble.of(expected), actual));
  }
  
  private static NetworkTableEntry getJsonNTEntry() {
    return LimelightHelpers.getLimelightNTTableEntry(TABLE_NAME, "json");
  }

  String readJSON(String fileName) throws IOException {
    try (InputStream is = getClass().getResourceAsStream(fileName)) {
      if (is == null) {
        throw new FileNotFoundException(fileName);
      }
      try (InputStreamReader isr = new InputStreamReader(is);
           BufferedReader reader = new BufferedReader(isr)) {
        JSONObject json = new JSONObject(reader.lines().collect(Collectors.joining(System.lineSeparator())));
        return json.getJSONObject("Results").toString();
      }
    }
  }
}
