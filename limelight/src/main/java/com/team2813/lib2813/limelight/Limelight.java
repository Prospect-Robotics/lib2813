package com.team2813.lib2813.limelight;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.wpilibj.Filesystem;
import java.io.*;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import org.json.JSONObject;

/**
 * Interface for interacting with a Limelight vision camera.
 *
 * <p>Provides methods for retrieving locational data, working with field maps, and accessing raw
 * JSON output. Some legacy methods are marked {@link Deprecated} in favor of {@link
 * LocationalData}-based APIs.
 */
public interface Limelight {

  /**
   * Gets the Limelight with the default name.
   *
   * @return the {@link Limelight} object for interfacing with the default camera
   */
  static Limelight getDefaultLimelight() {
    return RestLimelight.getDefaultLimelight();
  }

  /**
   * Returns the timestamp of the most recent capture, in seconds.
   *
   * @deprecated Use methods in {@link LocationalData} that return a {@link BotPoseEstimate}.
   * @return an {@link OptionalDouble} containing the timestamp if available
   */
  @Deprecated
  OptionalDouble getTimestamp();

  /**
   * Returns true if the Limelight currently has a valid target.
   *
   * @deprecated Use {@link LocationalData#hasTarget()} instead.
   * @return true if a target is detected
   */
  @Deprecated
  boolean hasTarget();

  /** Gets an object for retrieving locational data from the Limelight. */
  LocationalData getLocationalData();

  /**
   * Sets the field map for the Limelight from an input stream. Optionally, this can upload the map
   * to the Limelight.
   *
   * @param stream the input stream containing the field map
   * @param updateLimelight whether to update the Limelight with this map
   * @throws IOException if reading from the stream fails
   */
  void setFieldMap(InputStream stream, boolean updateLimelight) throws IOException;

  /**
   * Sets the field map for the Limelight using a file in the deploy directory.
   *
   * <p>This method opens the file and delegates to {@link #setFieldMap(InputStream, boolean)}.
   * Uploading the map to the Limelight can be slow and should not be called frequently.
   *
   * @param filepath path to the file relative to the deploy directory (use UNIX file separators)
   * @param updateLimelight whether to update the Limelight with this map
   * @throws IOException if the file does not exist or cannot be read
   */
  default void setFieldMap(String filepath, boolean updateLimelight) throws IOException {
    File file = new File(Filesystem.getDeployDirectory(), filepath);
    try (var stream = new FileInputStream(file)) {
      setFieldMap(stream, updateLimelight);
    }
  }

  /**
   * Returns the 3D poses of the specified visible AprilTags.
   *
   * @deprecated Use {@link LocationalData#getVisibleAprilTagPoses()} instead.
   * @param visibleTags a set of AprilTag IDs to locate
   * @return a list of {@link Pose3d} objects representing each tag's position
   */
  @Deprecated
  List<Pose3d> getLocatedAprilTags(Set<Integer> visibleTags);

  /**
   * Returns the capture latency for the most recent Limelight frame, in seconds.
   *
   * @deprecated Use {@link LocationalData#getCaptureLatency()} instead.
   * @return an {@link OptionalDouble} containing the latency if available
   */
  @Deprecated
  OptionalDouble getCaptureLatency();

  /**
   * Returns the most recent raw JSON dump from the Limelight.
   *
   * @deprecated Use {@link LocationalData#isValid()} instead. Not all Limelight implementations
   *     support this method.
   * @return an {@link Optional} containing the JSON object if available
   */
  @Deprecated
  Optional<JSONObject> getJsonDump();
}
