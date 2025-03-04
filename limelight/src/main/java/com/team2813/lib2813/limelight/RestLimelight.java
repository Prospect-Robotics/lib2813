package com.team2813.lib2813.limelight;

import static com.team2813.lib2813.limelight.JSONHelper.getArr;
import static com.team2813.lib2813.limelight.JSONHelper.getDouble;
import static com.team2813.lib2813.limelight.JSONHelper.getLong;
import static com.team2813.lib2813.limelight.JSONHelper.getRoot;
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

	String getName() {
		return name;
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
		return getLocationalData().getTargetingLatency();
	}

	public OptionalDouble getCaptureLatency() {
		return getLocationalData().getCaptureLatency();
	}

	@Override
	public OptionalDouble getTimestamp() {
		return getLocationalData().getTimestamp();
	}

	private static <T> Function<T, Boolean> not(Function<? super T, Boolean> fnc) {
		return (t) -> !fnc.apply(t);
	}

	public boolean hasTarget() {
		return getLocationalData().hasTarget();
	}

	public LocationalData getLocationalData() {
		return getJsonDump().flatMap(getRoot()).map(RestLocationalData::fromJsonDump).orElse(StubLocationalData.INSTANCE);
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

		static LocationalData fromJsonDump(JSONObject root) {
			return new RestLocationalData(root);
		}

		RestLocationalData(JSONObject root) {
			this.root = root;
		}

		@Override
		public boolean hasTarget() {
			return getArr("Fiducial").apply(root).map(not(JSONArray::isEmpty)).orElse(false);
		}

		private boolean invalidArray(JSONArray arr) {
			boolean simple = arr.length() != 6 || !hasTarget();
			if (simple) {
				return true;
			}
			Integer intZero = Integer.valueOf(0);
			Double doubleZero = Double.valueOf(0);
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
		public OptionalDouble getTimestamp() {
			return unboxDouble(getDouble("ts").apply(root));
		}

		@Override
		public OptionalDouble getCaptureLatency() {
			return unboxDouble(getDouble("cl").apply(root));
		}

		@Override
		public OptionalDouble getTargetingLatency() {
			return unboxDouble(getDouble("tl").apply(root));
		}

		@Override
		public Optional<Pose3d> getBotpose() {
			return getArr("botpose").apply(root).flatMap(this::parseArr);
		}

		/**
		 * Gets the position of the robot with the blue driverstation as the origin
		 * @return The position of the robot
		 */
		@Override
		public Optional<Pose3d> getBotposeBlue() {
			return getArr("botpose_wpiblue").apply(root).flatMap(this::parseArr);
		}

		/**
		 * Gets the position of the robot with the red driverstation as the origin
		 * @return The position of the robot
		 */
		@Override
		public Optional<Pose3d> getBotposeRed() {
			return getArr("botpose_wpired").apply(root).flatMap(this::parseArr);
		}

		/**
		 * Gets the id of the targeted tag.
		 */
		OptionalLong getTagID() {
			return unboxLong(getLong("pID").apply(root));
		}
	}
}
