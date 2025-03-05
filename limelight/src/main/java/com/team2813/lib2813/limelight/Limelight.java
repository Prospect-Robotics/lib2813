package com.team2813.lib2813.limelight;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.wpilibj.Filesystem;
import org.json.JSONObject;

import java.io.*;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;

public interface Limelight {
	
	/**
	 * Gets the limelight with the default name.
	 * @return the {@link Limelight} object for interfacing with the limelight
	 */
	public static Limelight getDefaultLimelight() {
		return RestLimelight.getDefaultLimelight();
	}

	OptionalDouble getTimestamp();

	boolean hasTarget();

	/**
	 * Gets an object for getting locational data
	 * @return an object for getting locational data
	 */
	LocationalData getLocationalData();
	
	/**
	 * Gets the set of all visible tags
	 * @return The visible tags
	 */
	Set<Integer> getVisibleTags();
	
	void setFieldMap(InputStream stream, boolean updateLimelight) throws IOException;
	
	/**
	 * Sets the field map for the limelight with a file in the deploy directory.
	 * Additionally, this may also upload the field map to the Limelight if desired.
	 * This will likely be a slow operation, and should not be regularly called.
	 * @param filepath The path to the file from the deploy directory (using UNIX file seperators)
	 * @param updateLimelight If the limelight should be updated with this field map
	 * @throws IOException If the given filepath does not exist in the deploy directory or could not be read
	 */
	default void setFieldMap(String filepath, boolean updateLimelight) throws IOException {
		File file = new File(Filesystem.getDeployDirectory(), filepath);
		try (var stream = new FileInputStream(file)) {
			setFieldMap(stream, updateLimelight);
		}
	}
	
	/**
	 * Gets the locations of the viewed AprilTags.
	 * This will always return an empty set if the field map was not set with {@link #setFieldMap(InputStream, boolean)}
	 * @return The located AprilTags
	 */
	List<Pose3d> getLocatedAprilTags();

	OptionalDouble getCaptureLatency();

	@Deprecated
	Optional<JSONObject> getJsonDump();
}
