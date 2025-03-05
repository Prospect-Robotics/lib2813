package com.team2813.lib2813.limelight.apriltag_map;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

public class FiducialRetriever {
  private static final Gson gson = new Gson();
  private final List<Fiducial> fiducials;

  public FiducialRetriever(InputStream stream) {
    FieldMap map = gson.fromJson(new InputStreamReader(stream, UTF_8), FieldMap.class);
    fiducials = unmodifiableList(asList(map.fiducials));
  }
  
  public List<Fiducial> getFidicuals() {
    return fiducials;
  }
}
