package com.team2813.lib2813.limelight;

import org.json.JSONObject;

import java.util.Optional;
import java.util.OptionalDouble;

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

	OptionalDouble getCaptureLatency();

	@Deprecated
	Optional<JSONObject> getJsonDump();
}
