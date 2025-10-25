package com.team2813.lib2813.limelight;

import static com.team2813.lib2813.limelight.JSONHelper.*;
import static com.team2813.lib2813.limelight.Optionals.unboxDouble;
import static com.team2813.lib2813.limelight.Optionals.unboxLong;
import static java.util.Collections.*;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.wpilibj.DriverStation;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * REST-based implementation of the Limelight interface for communicating with a Limelight vision
 * camera. This class manages a connection to a Limelight device over HTTP and provides methods for
 * retrieving vision targeting data, robot pose estimates, and AprilTag information.
 *
 * <p>Instances are managed through static factory methods and are cached based on their network
 * address. The class uses a scheduled executor service to periodically collect data from the
 * Limelight device.
 */
class RestLimelight implements Limelight {
  /** Map of cached Limelight instances keyed by their network address. */
  private static final Map<String, RestLimelight> limelights = new HashMap<>();

  /** Helper for managing AprilTag field map poses. */
  private final AprilTagMapPoseHelper aprilTagMapPoseHelper;

  /** Thread for collecting data from the Limelight. */
  private final DataCollection collectionThread;

  /** Shared executor service for all Limelight instances. */
  private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

  /** Default network address for the Limelight device. */
  static final String DEFAULT_ADDRESS = "limelight.local";

  /** Future representing the scheduled data collection task. */
  private ScheduledFuture<?> thread;

  /** Whether the data collection thread has been started. */
  boolean started = false;

  /**
   * Creates a new RestLimelight instance with the specified network address.
   *
   * @param address the hostname or IP address of the Limelight device
   */
  RestLimelight(String address) {
    var limelightClient = new LimelightClient(address);
    collectionThread = new DataCollection(limelightClient);
    aprilTagMapPoseHelper = new AprilTagMapPoseHelper(limelightClient);
  }

  /**
   * Starts the periodic data collection thread if it hasn't been started already. The thread runs
   * at a fixed rate of 40ms with an initial delay of 20ms.
   */
  void start() {
    if (!started) {
      thread = executor.scheduleAtFixedRate(collectionThread, 20, 40, TimeUnit.MILLISECONDS);
      started = true;
    }
  }

  /**
   * Manually runs one iteration of the data collection thread. This is primarily useful for testing
   * purposes.
   */
  void runThread() {
    collectionThread.run();
  }

  /** {@inheritDoc} */
  @Override
  public Optional<JSONObject> getJsonDump() {
    return collectionThread.getMostRecent().map(DataCollection.Result::json);
  }

  /**
   * Gets the capture latency of the most recent frame from the Limelight.
   *
   * @return an OptionalDouble containing the capture latency in milliseconds, or empty if
   *     unavailable
   */
  public OptionalDouble getCaptureLatency() {
    return getLocationalData().getCaptureLatency();
  }

  /** {@inheritDoc} */
  @Override
  public OptionalDouble getTimestamp() {
    return getLocationalData().getTimestamp();
  }

  /**
   * Sets the field map for the limelight. Additionally, this may also upload the field map to the
   * Limelight if desired. This will likely be a slow operation, and should not be regularly called.
   *
   * @param stream the input stream containing the field map data
   * @param updateLimelight if true, uploads the field map to the Limelight device
   * @throws IOException if an I/O error occurs while reading the stream or uploading to the device
   */
  @Override
  public void setFieldMap(InputStream stream, boolean updateLimelight) throws IOException {
    aprilTagMapPoseHelper.setFieldMap(stream, updateLimelight);
  }

  /** {@inheritDoc} */
  @Override
  public List<Pose3d> getLocatedAprilTags(Set<Integer> visibleTags) {
    return aprilTagMapPoseHelper.getVisibleTagPoses(visibleTags);
  }

  /**
   * Creates a negation function that inverts the boolean result of another function.
   *
   * @param <T> the type of the function input
   * @param fnc the function to negate
   * @return a function that returns the opposite boolean value
   */
  private static <T> Function<T, Boolean> not(Function<? super T, Boolean> fnc) {
    return (t) -> !fnc.apply(t);
  }

  /**
   * Checks if the Limelight currently has a valid target in view.
   *
   * @return true if a target is detected, false otherwise
   */
  public boolean hasTarget() {
    return getLocationalData().hasTarget();
  }

  /**
   * Gets the most recent locational data from the Limelight.
   *
   * @return LocationalData containing pose estimates, tag information, and latency data, or a stub
   *     with valid defaults if no data is available
   */
  public LocationalData getLocationalData() {
    Optional<LocationalData> locationalData =
        collectionThread.getMostRecent().map(RestLocationalData::new);
    return locationalData.orElse(StubLocationalData.VALID);
  }

  /**
   * Cleans up resources by canceling the data collection thread and waiting for termination. Logs
   * any interruption errors to the DriverStation.
   */
  private void clean() {
    try {
      thread.cancel(true);
      executor.awaitTermination(2, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      DriverStation.reportError(e.getMessage(), false);
    }
  }

  /**
   * Gets the limelight with the default name.
   *
   * @return the {@link Limelight} object for interfacing with the limelight
   */
  public static Limelight getDefaultLimelight() {
    return getLimelight(DEFAULT_ADDRESS);
  }

  /**
   * Gets the limelight with the specified name. Calling with a blank {@code limelightName} is
   * equivalent to calling {@link #getDefaultLimelight()}
   *
   * @param limelightAddress the hostname or IP address of the limelight
   * @return the {@link Limelight} object for interfacing with the limelight
   * @throws NullPointerException if {@code limelightAddress} is null
   * @throws IllegalArgumentException if {@code limelightAddress} is empty
   */
  public static Limelight getLimelight(String limelightAddress) {
    String addr = Objects.requireNonNull(limelightAddress, "limelightAddress shouldn't be null");
    if (addr.isEmpty()) {
      throw new IllegalArgumentException("limelightAddress shouldn't be empty");
    }
    RestLimelight result = limelights.computeIfAbsent(addr, RestLimelight::new);
    result.start();
    return result;
  }

  /**
   * Clears all cached Limelight instances and cleans up their resources. This method should be
   * called when shutting down or resetting the system.
   */
  static void eraseInstances() {
    for (RestLimelight limelight : limelights.values()) {
      limelight.clean();
    }
    limelights.clear();
  }

  /**
   * Implementation of LocationalData that parses data from JSON responses received from the
   * Limelight. This class provides access to robot pose estimates, AprilTag information, and timing
   * data.
   */
  private class RestLocationalData implements LocationalData {
    /** The root JSON object containing all Limelight data. */
    private final JSONObject root;

    /** The timestamp when the response was received, in seconds. */
    private final double responseTimestamp;

    /**
     * Creates a new RestLocationalData instance from a data collection result.
     *
     * @param result the result containing JSON data and timestamp
     */
    RestLocationalData(DataCollection.Result result) {
      this.root = getRoot(result.json());
      this.responseTimestamp = result.responseTimestamp();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isValid() {
      return getBooleanFromInt(root, "v").orElse(false);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasTarget() {
      return getArr(root, "Fiducial").map(not(JSONArray::isEmpty)).orElse(false);
    }

    /**
     * Checks if a pose array is invalid or contains only zero values.
     *
     * @param arr the JSON array to validate
     * @return true if the array is invalid (wrong length, no target, or all zeros), false otherwise
     */
    private boolean invalidArray(JSONArray arr) {
      boolean simple = arr.length() != 6 || !hasTarget();
      if (simple) {
        return true;
      }
      Integer intZero = 0;
      Double doubleZero = 0.0;
      for (Object o : arr) {
        if (!intZero.equals(o) && !doubleZero.equals(o)) {
          return false;
        }
      }
      return true;
    }

    /**
     * Parses a JSON array into a Pose3d object. The array is expected to contain 6 elements: [x, y,
     * z, roll, pitch, yaw] where rotation values are in degrees.
     *
     * @param arr the JSON array containing pose data
     * @return an Optional containing the parsed Pose3d, or empty if the array is invalid
     */
    private Optional<Pose3d> parseArr(JSONArray arr) {
      if (invalidArray(arr)) {
        return Optional.empty();
      }
      Rotation3d rotation =
          new Rotation3d(
              Math.toRadians(arr.getDouble(3)),
              Math.toRadians(arr.getDouble(4)),
              Math.toRadians(arr.getDouble(5)));
      return Optional.of(
          new Pose3d(arr.getDouble(0), arr.getDouble(1), arr.getDouble(2), rotation));
    }

    /** {@inheritDoc} */
    @Override
    public OptionalDouble getTimestamp() {
      return unboxDouble(getDouble(root, "ts"));
    }

    /** {@inheritDoc} */
    @Override
    public OptionalDouble getCaptureLatency() {
      return unboxDouble(getDouble(root, "cl"));
    }

    /** {@inheritDoc} */
    @Override
    public OptionalDouble getTargetingLatency() {
      return unboxDouble(getDouble(root, "tl"));
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Pose3d> getBotpose() {
      return getArr(root, "botpose").flatMap(this::parseArr);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<BotPoseEstimate> getBotPoseEstimate() {
      return getArr(root, "botpose").flatMap(this::parseArr).map(this::toBotPoseEstimate);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Pose3d> getBotposeBlue() {
      return getArr(root, "botpose_wpiblue").flatMap(this::parseArr);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<BotPoseEstimate> getBotPoseEstimateBlue() {
      return getArr(root, "botpose_wpiblue").flatMap(this::parseArr).map(this::toBotPoseEstimate);
    }

    /**
     * Gets the position of the robot with the red driverstation as the origin.
     *
     * @return an Optional containing the robot's Pose3d in red alliance coordinates, or empty if
     *     unavailable
     */
    @Override
    public Optional<Pose3d> getBotposeRed() {
      return getArr(root, "botpose_wpired").flatMap(this::parseArr);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<BotPoseEstimate> getBotPoseEstimateRed() {
      return getArr(root, "botpose_wpired").flatMap(this::parseArr).map(this::toBotPoseEstimate);
    }

    /**
     * Converts a Pose3d into a BotPoseEstimate with timestamp and visible tag information. The
     * timestamp is calculated by subtracting the total latency from the response timestamp.
     *
     * @param pose the 3D pose to convert
     * @return a BotPoseEstimate with the 2D pose projection, adjusted timestamp, and visible tags
     * @see <a
     *     href="https://www.chiefdelphi.com/t/timestamp-parameter-when-adding-limelight-vision-to-odometry">Chief
     *     Delphi Discussion</a>
     */
    private BotPoseEstimate toBotPoseEstimate(Pose3d pose) {
      // See
      // https://www.chiefdelphi.com/t/timestamp-parameter-when-adding-limelight-vision-to-odometry
      double latencyMillis = getCaptureLatency().orElse(0.0) + getTargetingLatency().orElse(0.0);
      double timestampSeconds = responseTimestamp - (latencyMillis / 1000);
      return new BotPoseEstimate(
          pose.toPose2d(), timestampSeconds, getVisibleAprilTagPoses().keySet());
    }

    /**
     * Gets the ID of the currently targeted AprilTag.
     *
     * @return an OptionalLong containing the tag ID, or empty if no tag is targeted
     */
    OptionalLong getTagID() {
      return unboxLong(getLong(root, "pID"));
    }

    /** {@inheritDoc} */
    @Override
    public Set<Integer> getVisibleTags() {
      return getArr(root, "Fiducial")
          .map(
              arr -> {
                Set<Integer> ints = new HashSet<>();
                for (int i = 0; i < arr.length(); i++) {
                  JSONObject obj = arr.optJSONObject(i);
                  if (obj != null && obj.has("fID")) {
                    ints.add(obj.getInt("fID"));
                  }
                }
                return unmodifiableSet(ints);
              })
          .orElseGet(Set::of);
    }

    /** {@inheritDoc} */
    @Override
    public Map<Integer, Pose3d> getVisibleAprilTagPoses() {
      return getArr(root, "Fiducial")
          .map(
              arr -> {
                Map<Integer, Pose3d> map = new HashMap<>();
                for (int i = 0; i < arr.length(); i++) {
                  JSONObject obj = arr.optJSONObject(i);
                  if (obj != null && obj.has("fID")) {
                    int id = obj.getInt("fID");
                    aprilTagMapPoseHelper.getTagPose(id).ifPresent(pose -> map.put(id, pose));
                  }
                }
                return unmodifiableMap(map);
              })
          .orElse(emptyMap());
    }
  }
}
