/*
Copyright 2024-2026 Prospect Robotics SWENext Club

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
package com.team2813.lib2813.limelight;

import static com.team2813.lib2813.limelight.JSONHelper.*;
import static com.team2813.lib2813.limelight.Optionals.unboxDouble;
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

class RestLimelight implements Limelight {
  private static final Map<String, RestLimelight> limelights = new HashMap<>();
  private final AprilTagMapPoseHelper aprilTagMapPoseHelper;
  private final DataCollection collectionThread;
  private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

  static final String DEFAULT_ADDRESS = "limelight.local";

  private ScheduledFuture<?> thread;

  boolean started = false;

  RestLimelight(String address) {
    var limelightClient = new LimelightClient(address);
    collectionThread = new DataCollection(limelightClient);
    aprilTagMapPoseHelper = new AprilTagMapPoseHelper(limelightClient);
  }

  void start() {
    if (!started) {
      thread = executor.scheduleAtFixedRate(collectionThread, 20, 40, TimeUnit.MILLISECONDS);
      started = true;
    }
  }

  void runThread() {
    collectionThread.run();
  }

  /**
   * Sets the field map for the limelight. Additionally, this may also upload the field map to the
   * Limelight if desired. This will likely be a slow operation, and should not be regularly called.
   *
   * @param stream The reader which
   * @param updateLimelight If the limelight should be updated with this field map
   */
  @Override
  public void setFieldMap(InputStream stream, boolean updateLimelight) throws IOException {
    aprilTagMapPoseHelper.setFieldMap(stream, updateLimelight);
  }

  private static <T> Function<T, Boolean> not(Function<? super T, Boolean> fnc) {
    return (t) -> !fnc.apply(t);
  }

  public boolean hasTarget() {
    return getLocationalData().hasTarget();
  }

  public LocationalData getLocationalData() {
    Optional<LocationalData> locationalData =
        collectionThread.getMostRecent().map(RestLocationalData::new);
    return locationalData.orElse(StubLocationalData.INVALID);
  }

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
   * @param limelightAddress The hostname or ip address of the limelight
   * @return the {@link Limelight} object for interfacing with the limelight
   * @throws NullPointerException if {@code limelightName} is null
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

  static void eraseInstances() {
    for (RestLimelight limelight : limelights.values()) {
      limelight.clean();
    }
    limelights.clear();
  }

  private class RestLocationalData implements LocationalData {
    private final JSONObject root;
    private final double responseTimestamp;

    RestLocationalData(DataCollection.Result result) {
      this.root = getRoot(result.json());
      this.responseTimestamp = result.responseTimestamp();
    }

    @Override
    public boolean isValid() {
      return getBooleanFromInt(root, "v").orElse(false);
    }

    @Override
    public boolean hasTarget() {
      return getArr(root, "Fiducial").map(not(JSONArray::isEmpty)).orElse(false);
    }

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

    @Override
    public OptionalDouble getCaptureLatency() {
      return unboxDouble(getDouble(root, "cl"));
    }

    @Override
    public OptionalDouble getTargetingLatency() {
      return unboxDouble(getDouble(root, "tl"));
    }

    @Override
    public Optional<Pose3d> getBotpose() {
      return getArr(root, "botpose").flatMap(this::parseArr);
    }

    @Override
    public Optional<BotPoseEstimate> getBotPoseEstimate() {
      return getArr(root, "botpose").flatMap(this::parseArr).map(this::toBotPoseEstimate);
    }

    @Override
    public Optional<Pose3d> getBotposeBlue() {
      return getArr(root, "botpose_wpiblue").flatMap(this::parseArr);
    }

    @Override
    public Optional<BotPoseEstimate> getBotPoseEstimateBlue() {
      return getArr(root, "botpose_wpiblue").flatMap(this::parseArr).map(this::toBotPoseEstimate);
    }

    /**
     * Gets the position of the robot with the red driverstation as the origin
     *
     * @return The position of the robot
     */
    @Override
    public Optional<Pose3d> getBotposeRed() {
      return getArr(root, "botpose_wpired").flatMap(this::parseArr);
    }

    @Override
    public Optional<BotPoseEstimate> getBotPoseEstimateRed() {
      return getArr(root, "botpose_wpired").flatMap(this::parseArr).map(this::toBotPoseEstimate);
    }

    private BotPoseEstimate toBotPoseEstimate(Pose3d pose) {
      // See
      // https://www.chiefdelphi.com/t/timestamp-parameter-when-adding-limelight-vision-to-odometry
      double latencyMillis = getCaptureLatency().orElse(0.0) + getTargetingLatency().orElse(0.0);
      double timestampSeconds = responseTimestamp - (latencyMillis / 1000);
      return new BotPoseEstimate(
          pose.toPose2d(), timestampSeconds, getVisibleAprilTagPoses().keySet());
    }

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
