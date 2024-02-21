package com.team2813.lib2813.limelight;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

import org.json.JSONObject;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

public class Limelight {
	private static Map<String, Limelight> limelights = new HashMap<>();
	private static NetworkTableInstance tableInstance = NetworkTableInstance.getDefault();
	private final NetworkTable limelightTable;
	private final LimelightConfig limelightConfig;
	private final String name;

	static final String DEFAULT_TABLE = "limelight";

	// specific types of data;
	private final LocationalData data;

	private Limelight(String name) {
		limelightTable = tableInstance.getTable(name);
		data = new LocationalData(this);
		limelightConfig = new LimelightConfig(this);
		this.name = name;
	}

	String getName() {
		return name;
	}

	NetworkTable networkTable() {
		return limelightTable;
	}

	public Optional<JSONObject> getJsonDump() {
		String json = limelightTable.getEntry("json").getString(null);
		return json == null ? Optional.empty() : Optional.of(
			new JSONObject(json)
		);
	}

	public OptionalLong getPipeline() {
		long pipeline = limelightTable.getEntry("pipeline").getInteger(-1);
		return pipeline == -1 ? OptionalLong.empty() : OptionalLong.of(pipeline);
	}

	/**
	 * Sets the pipeline
	 * @param val the pipeline number [0, 9]
	 * @throws IllegalArgumentException if the pipeline is invalid
	 */
	public void setPipeline(int val) {
		if (val > 9 || val < 0) {
			throw new IllegalArgumentException("Invalid pipeline number");
		}
		limelightTable.getEntry("pipeline").setInteger(val);
	}

	/**
	 * Gets an object for getting locational data
	 * @return an object for getting locational data
	 */
	public LocationalData getLocationalData() {
		return data;
	}

	/**
	 * Gets an object for configuring the limelight
	 * @return an object for configuring the limelight
	 */
	public LimelightConfig getConfig() {
		return limelightConfig;
	}

	/**
	 * Checks if the limelight has a target
	 * @return {@code true} if there is a target seen by the limelight
	 */
	public boolean hasTarget() {
		return 1 == limelightTable.getEntry("tv").getInteger(0);
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
	 * is equivilent to calling {@link #getDefaultLimelight()}
	 * @param limelightName The name of the limelight to interface with
	 * @return the {@link Limelight} object for interfacing with the limelight
	 * @throws NullPointerException if {@code limelightName} is null
	 */
	public static Limelight getLimelight(String limelightName) {
		String table = Objects.requireNonNull(limelightName,"limelightName shouldn't be null");
		if (table.isBlank()) {
			table = DEFAULT_TABLE;
		}
		return limelights.computeIfAbsent(table, Limelight::new);
	}

	static void eraseInstances() {
		limelights.clear();
	}

	/**
	 * Sets the {@link NetworkTableInstance} to use for creating new {@link Limelight} objects.
	 * For testing purposes; call <strong>before</strong> getting a limelight instance
	 * @param tableInstance
	 */
	static void setTableInstance(NetworkTableInstance tableInstance) {
		Limelight.tableInstance = tableInstance;
	}
}
