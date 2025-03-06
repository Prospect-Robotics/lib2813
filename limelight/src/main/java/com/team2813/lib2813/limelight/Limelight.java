package com.team2813.lib2813.limelight;

import org.json.JSONObject;

import java.util.Optional;
import java.util.OptionalDouble;

public interface Limelight {
	
	/**
	 * Gets the limelight with the default name.
	 * @return the {@link Limelight} object for interfacing with the limelight
	 */
	static Limelight getDefaultLimelight() {
		return RestLimelight.getDefaultLimelight();
	}

	/**
	 * Returns {@code true} if the limelight has identified a target.
	 *
	 * @deprecated use {@link LocationalData#hasTarget()}
	 */
	@Deprecated
	boolean hasTarget();

	/** Gets an object for getting locational data. */
	LocationalData getLocationalData();

	/**
	 * @deprecated use {@link LocationalData#getCaptureLatency()}
	 */
	@Deprecated
	OptionalDouble getCaptureLatency();

	/**
	 * Gets the most recent JSON from the Limelight.
	 * Does not work for all implementations.
	 *
	 * @deprecated use {@link LocationalData#isValid()}.
	 */
	@Deprecated
	Optional<JSONObject> getJsonDump();
}
