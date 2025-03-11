package com.team2813.lib2813.limelight.apriltag_map;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class FiducialRetriever {
  private static final Gson gson = new Gson();
  private final Map<Integer, Fiducial> fiducialMap;

  public FiducialRetriever(InputStream stream) {
    FieldMap map = gson.fromJson(new InputStreamReader(stream, UTF_8), FieldMap.class);
    fiducialMap = Arrays.stream(map.fiducials).collect(Collectors.toUnmodifiableMap(Fiducial::getId, f -> f));
  }
  
  public Map<Integer, Fiducial> getFiducialMap() {
    return fiducialMap;
  }
}
