/*
Copyright 2025 Prospect Robotics SWENext Club

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.team2813.lib2813.limelight.apriltag_map;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class FiducialRetriever {
  private static final Gson gson = new Gson();
  private final Map<Integer, Fiducial> fiducialMap;

  public FiducialRetriever(InputStream stream) {
    FieldMap map = gson.fromJson(new InputStreamReader(stream, UTF_8), FieldMap.class);
    fiducialMap =
        Arrays.stream(map.fiducials).collect(Collectors.toUnmodifiableMap(Fiducial::getId, f -> f));
  }

  public Map<Integer, Fiducial> getFiducialMap() {
    return fiducialMap;
  }
}
