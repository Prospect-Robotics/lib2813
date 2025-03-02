package com.team2813.lib2813.limelight;

import org.json.JSONObject;

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
	
	Set<Integer> getVisibleTags();

	OptionalDouble getCaptureLatency();

	@Deprecated
	Optional<JSONObject> getJsonDump();
}
