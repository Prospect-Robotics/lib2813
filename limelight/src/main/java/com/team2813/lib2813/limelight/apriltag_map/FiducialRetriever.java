package com.team2813.lib2813.limelight.apriltag_map;

import com.google.gson.Gson;
import edu.wpi.first.wpilibj.Filesystem;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;

public class FiducialRetriever {
  private static final Gson gson = new Gson();
  private final Fiducial[] fiducials;
  public FiducialRetriever(InputStream stream) {
    FieldMap map = gson.fromJson(new InputStreamReader(stream, UTF_8), FieldMap.class);
    fiducials = map.fiducials;
  }
  
  public Fiducial[] getFidicuals() {
    return fiducials;
  }
}
