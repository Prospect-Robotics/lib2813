package com.team2813.lib2813.limelight;

import static com.team2813.lib2813.limelight.JSONHelper.*;
import static com.team2813.lib2813.limelight.Optionals.unboxDouble;
import static com.team2813.lib2813.limelight.Optionals.unboxLong;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Time;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.wpi.first.wpilibj.DriverStation;

class RestLimelight implements Limelight {
	private static final Map<String, RestLimelight> limelights = new HashMap<>();
	private final String name;
	private final DataCollection collectionThread;
	private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

	static final String DEFAULT_ADDRESS = "limelight.local";

	private ScheduledFuture<?> thread;

	boolean started = false;

	RestLimelight(String address) {
		this.name = address;
		collectionThread = new DataCollection(address);
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

	public String getName() {
		return name;
	}

	@Override
	public Optional<JSONObject> getJsonDump() {
		return collectionThread.getMostRecent().map(DataCollection.Result::json);
	}

	/**
	 * Gets the targeting latency from the limelight
	 * @return The targeting latency
	 */
	public OptionalDouble getTargetingLatency() {
		return getLocationalData().getTargetingLatency();
	}

	public OptionalDouble getCaptureLatency() {
		return getLocationalData().getCaptureLatency();
	}

	private static <T> Function<T, Boolean> not(Function<? super T, Boolean> fnc) {
		return (t) -> !fnc.apply(t);
	}

	public boolean hasTarget() {
		return getLocationalData().hasTarget();
	}
   
	public LocationalData getLocationalData() {
		return collectionThread.getMostRecent().map(RestLocationalData::fromResult).orElse(StubLocationalData.VALID);
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
	
	private static class RestLocationalData implements LocationalData {
		private final JSONObject root;
		private final double jsonParseTimeMillis;

		static LocationalData fromResult(DataCollection.Result result) {
			JSONObject root = getRoot(result.json());
			return new RestLocationalData(root, result.jsonParseTimeMillis());
		}

		RestLocationalData(JSONObject root, double jsonParseTimeMillis) {
			this.root = root;
			this.jsonParseTimeMillis = jsonParseTimeMillis;
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
			Double doubleZero = 0d;
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
			return unboxDouble(getDouble(root, "cl"));
		}

		@Override
		public OptionalDouble getTargetingLatency() {
			return unboxDouble(getDouble(root, "tl"));
		}

		@Override
		public Optional<Time> getTotalLatency() {
			// See https://www.chiefdelphi.com/t/timestamp-parameter-when-adding-limelight-vision-to-odometry
			double millis = jsonParseTimeMillis + getCaptureLatency().orElse(0.0) + getTargetingLatency().orElse(0.0);
			return Optional.of(Units.Milliseconds.of(millis));
		}

		/**
		 * Gets the position of the robot with the center of the field as the origin
		 * @return The position of the robot
		 */
		public Optional<Pose3d> getBotpose() {
			return getArr(root, "botpose").flatMap(this::parseArr);
		}

		/**
		 * Gets the position of the robot with the blue driverstation as the origin
		 * @return The position of the robot
		 */
		@Override
		public Optional<Pose3d> getBotposeBlue() {
			return getArr(root, "botpose_wpiblue").flatMap(this::parseArr);
		}

		/**
		 * Gets the position of the robot with the red driverstation as the origin
		 * @return The position of the robot
		 */
		@Override
		public Optional<Pose3d> getBotposeRed() {
			return getArr(root, "botpose_wpired").flatMap(this::parseArr);
		}

		/**
		 * Gets the id of the targeted tag.
		 */
		OptionalLong getTagID() {
			return unboxLong(getLong(root, "pID"));
		}
	}
}
