package com.team2813.lib2813.limelight.apriltag_map;

import com.google.gson.Gson;
import edu.wpi.first.wpilibj.Filesystem;

import java.awt.*;
import java.io.*;

public class FiducialRetriever {
  private static final Gson gson = new Gson();
  private final Fiducial[] fiducials;
  public FiducialRetriever(InputStream stream) {
    FieldMap map = gson.fromJson(new InputStreamReader(stream), FieldMap.class);
    fiducials = map.fiducials;
  }
  
  public Fiducial[] getFidicuals() {
    return fiducials;
  }
}
