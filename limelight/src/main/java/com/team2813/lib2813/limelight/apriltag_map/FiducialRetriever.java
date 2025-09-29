package com.team2813.lib2813.limelight.apriltag_map;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Retrieves and stores fiducial (AprilTag) information from a JSON input stream.
 *
 * <p>The JSON is expected to represent a {@link FieldMap}, which contains an array of
 * {@link Fiducial} objects. Each fiducial is mapped by its unique ID for easy lookup.
 */
public class FiducialRetriever {

  /** Gson instance used for deserializing JSON. */
  private static final Gson gson = new Gson();

  /** Map of fiducial ID to {@link Fiducial} object. */
  private final Map<Integer, Fiducial> fiducialMap;

  /**
   * Constructs a new {@link FiducialRetriever} by reading fiducials from the given input stream.
   *
   * @param stream an {@link InputStream} containing a JSON representation of a {@link FieldMap}
   * @throws com.google.gson.JsonSyntaxException if the JSON is invalid
   * @throws java.io.UncheckedIOException if there is an error reading from the stream
   */
  public FiducialRetriever(InputStream stream) {
    FieldMap map = gson.fromJson(new InputStreamReader(stream, UTF_8), FieldMap.class);
    // Convert array of fiducials to an unmodifiable map keyed by fiducial ID
    fiducialMap =
        Arrays.stream(map.fiducials)
            .collect(Collectors.toUnmodifiableMap(Fiducial::getId, f -> f));
  }

  /**
   * Returns an unmodifiable map of fiducials keyed by their ID.
   *
   * @return a {@link Map} of fiducial ID to {@link Fiducial}
   */
  public Map<Integer, Fiducial> getFiducialMap() {
    return fiducialMap;
  }
}
