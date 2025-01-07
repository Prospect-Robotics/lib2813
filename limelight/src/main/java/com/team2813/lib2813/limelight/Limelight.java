package com.team2813.lib2813.limelight;

import static com.team2813.lib2813.limelight.JSONHelper.getArr;
import static com.team2813.lib2813.limelight.JSONHelper.getLong;
import static com.team2813.lib2813.limelight.JSONHelper.getRoot;
import static com.team2813.lib2813.limelight.JSONHelper.unboxLong;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.wpi.first.wpilibj.DriverStation;

public class Limelight {
	private static Map<String, Limelight> limelights = new HashMap<>();
	private final String name;
	private final DataCollection collectionThread;
	private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

	static final String DEFAULT_ADDRESS = "limelight.local";

	private ScheduledFuture<?> thread;

	boolean started = false;

	// specific types of data;
	private final LocationalData data;

	Limelight(String address) {
		data = new LocationalData(this);
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

	public Optional<JSONObject> getJsonDump() {
		return collectionThread.getMostRecent();
	}

	/**
	 * Gets the targeting latency from the limelight
	 * @return The targeting latency
	 */
	public OptionalLong getTargetingLatency() {
		return unboxLong(getJsonDump().flatMap(getRoot()).flatMap(getLong("tl")));
	}

	public OptionalLong getCaptureLatency() {
		return unboxLong(getJsonDump().flatMap(getRoot()).flatMap(getLong("cl")));
	}

	public OptionalLong getTimestamp() {
		return unboxLong(getJsonDump().flatMap(getRoot()).flatMap(getLong("ts")));
	}

	private static <T> Function<T, Boolean> not(Function<? super T, Boolean> fnc) {
		return (t) -> !fnc.apply(t);
	} 

	public boolean hasTarget() {
		return getJsonDump().flatMap(getRoot()).flatMap(getArr("Fiducial")).map(not(JSONArray::isEmpty)).orElse(false);
	}
	/**
	 * Gets an object for getting locational data
	 * @return an object for getting locational data
	 */
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
		return getLimelight("");
	}

	/**
	 * Gets the limelight with the specified name. Calling with a blank {@code limelightName}
	 * is equivalent to calling {@link #getDefaultLimelight()}
	 * @param limelightName The hostname or ip address of the limelight
	 * @return the {@link Limelight} object for interfacing with the limelight
	 * @throws NullPointerException if {@code limelightName} is null
	 */
	public static Limelight getLimelight(String limelightName) {
		String table = Objects.requireNonNull(limelightName,"limelightName shouldn't be null");
		if (table.isBlank()) {
			table = DEFAULT_ADDRESS;
		}
		Limelight result = limelights.computeIfAbsent(table, Limelight::new);
		result.start();
		return result;
	}

	static void eraseInstances() {
		for (Limelight limelight : limelights.values()) {
			limelight.clean();
		}
		limelights.clear();
	}
}
