package com.team2813.lib2813.limelight;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.wpilibj.DriverStation;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.team2813.lib2813.limelight.JSONHelper.*;
import static com.team2813.lib2813.limelight.Optionals.unboxDouble;
import static com.team2813.lib2813.limelight.Optionals.unboxLong;

class RestLimelight implements Limelight {
	private static final Map<String, RestLimelight> limelights = new HashMap<>();
	private final AprilTagMapPoseHelper aprilTagMapPoseHelper;
	private final DataCollection collectionThread;
	private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

	static final String DEFAULT_ADDRESS = "limelight.local";

	private ScheduledFuture<?> thread;

	boolean started = false;

	// specific types of data;
	private final LocationalData data;

	RestLimelight(String address) {
		data = new RestLocationalData();
		collectionThread = new DataCollection(address);
		aprilTagMapPoseHelper = new AprilTagMapPoseHelper(address);
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

	@Override
	public Optional<JSONObject> getJsonDump() {
		return collectionThread.getMostRecent();
	}

	/**
	 * Gets the targeting latency from the limelight
	 * @return The targeting latency
	 */
	public OptionalDouble getTargetingLatency() {
		return unboxDouble(getJsonDump().flatMap(getRoot()).flatMap(getDouble("tl")));
	}

	public OptionalDouble getCaptureLatency() {
		return unboxDouble(getJsonDump().flatMap(getRoot()).flatMap(getDouble("cl")));
	}

	@Override
	public OptionalDouble getTimestamp() {
		return unboxDouble(getJsonDump().flatMap(getRoot()).flatMap(getDouble("ts")));
	}
	
	/**
	 * Sets the field map for the limelight. Additionally, this may also upload the field map to the Limelight if desired.
	 * This will likely be a slow operation, and should not be regularly called.
	 * @param stream The reader which
	 * @param updateLimelight If the limelight should be updated with this field map
	 */
	@Override
	public void setFieldMap(InputStream stream, boolean updateLimelight) {
		aprilTagMapPoseHelper.setFieldMap(stream, updateLimelight);
	}
	
	@Override
	public List<Pose3d> getLocatedApriltags() {
		return aprilTagMapPoseHelper.getVisibleTagPoses(getVisibleTags());
	}
	
	private static <T> Function<T, Boolean> not(Function<? super T, Boolean> fnc) {
		return (t) -> !fnc.apply(t);
	} 

	public boolean hasTarget() {
		return getJsonDump().flatMap(getRoot()).flatMap(getArr("Fiducial")).map(not(JSONArray::isEmpty)).orElse(false);
	}

	public LocationalData getLocationalData() {
		return data;
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
	 * @return the {@link Limelight} object for interfacing with the limelight
	 */
	public static Limelight getDefaultLimelight() {
		return getLimelight(DEFAULT_ADDRESS);
	}

	/**
	 * Gets the limelight with the specified name. Calling with a blank {@code limelightName}
	 * is equivalent to calling {@link #getDefaultLimelight()}
	 * @param limelightAddress The hostname or ip address of the limelight
	 * @return the {@link Limelight} object for interfacing with the limelight
	 * @throws NullPointerException if {@code limelightName} is null
	 */
	public static Limelight getLimelight(String limelightAddress) {
		String addr = Objects.requireNonNull(limelightAddress,"limelightAddress shouldn't be null");
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
	
	@Override
	public Set<Integer> getVisibleTags() {
		return getJsonDump().flatMap(getRoot()).flatMap(getArr("Fiducial")).map((arr) -> {
			Set<Integer> ints = new HashSet<>();
			for (int i = 0; i < arr.length(); i++) {
				JSONObject obj = arr.optJSONObject(i);
				if (obj != null && obj.has("fID")) {
					ints.add(obj.getInt("fID"));
				}
			}
			return ints;
		}).orElseGet(Set::of);
	}
	
	private class RestLocationalData implements LocationalData {

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
			Rotation3d rotation = new Rotation3d(
							Math.toRadians(arr.getDouble(3)),
							Math.toRadians(arr.getDouble(4)),
							Math.toRadians(arr.getDouble(5))
			);
			return Optional.of(new Pose3d(arr.getDouble(0), arr.getDouble(1), arr.getDouble(2), rotation));
		}

		@Override
		public OptionalDouble getCaptureLatency() {
			return RestLimelight.this.getCaptureLatency();
		}

		@Override
		public OptionalDouble getTargetingLatency() {
			return RestLimelight.this.getTargetingLatency();
		}

		/**
		 * Gets the position of the robot with the center of the field as the origin
		 * @return The position of the robot
		 */
		public Optional<Pose3d> getBotpose() {
			return getJsonDump().flatMap(getRoot()).flatMap(getArr("botpose")).flatMap(this::parseArr);
		}

		/**
		 * Gets the position of the robot with the blue driverstation as the origin
		 * @return The position of the robot
		 */
		@Override
		public Optional<Pose3d> getBotposeBlue() {
			return getJsonDump().flatMap(getRoot()).flatMap(getArr("botpose_wpiblue")).flatMap(this::parseArr);
		}

		/**
		 * Gets the position of the robot with the red driverstation as the origin
		 * @return The position of the robot
		 */
		@Override
		public Optional<Pose3d> getBotposeRed() {
			return getJsonDump().flatMap(getRoot()).flatMap(getArr("botpose_wpired")).flatMap(this::parseArr);
		}

		/**
		 * Gets the id of the targeted tag.
		 */
		public OptionalLong getTagID() {
			return unboxLong(getJsonDump().flatMap(getRoot()).flatMap(getLong("pID")));
		}
	}
}
