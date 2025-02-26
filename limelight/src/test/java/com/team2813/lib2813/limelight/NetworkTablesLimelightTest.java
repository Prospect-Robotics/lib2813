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
import static org.junit.Assume.*;

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
