package com.team2813.lib2813.limelight;

import static com.team2813.lib2813.limelight.JSONHelper.getLong;
import static com.team2813.lib2813.limelight.JSONHelper.unboxLong;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Function;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;

/**
 * Get positional data from limelight
 * @see Limelight
 */
public class LocationalData {
	private final Limelight limelight;
	private OptionalLong msDelay = OptionalLong.empty();
	
	LocationalData(Limelight limelight) {
		this.limelight = limelight;
	}

	private Optional<Pose3d> parseArr(JSONArray arr) {
		if (arr.length() < 6 || !limelight.hasTarget()) {
			msDelay = OptionalLong.empty();
			return Optional.empty();
		}
		msDelay = OptionalLong.of(arr.getLong(6));
		Rotation3d rotation = new Rotation3d(
			Math.toRadians(arr.getDouble(3)),
			Math.toRadians(arr.getDouble(4)),
			Math.toRadians(arr.getDouble(5))
		);
		return Optional.of(new Pose3d(arr.getDouble(0), arr.getDouble(1), arr.getDouble(2), rotation));
	}

	private static Function<JSONObject, Optional<JSONArray>> getArr(String key) {
		return (obj) -> {
			if (!obj.has(key)) {
				return Optional.empty();
			}
			try {
				return Optional.of(obj.getJSONArray(key));
			} catch (JSONException e) {
				return Optional.empty();
			}
		};
	}

	public OptionalLong lastMSDelay() {
		return msDelay;
	} 

	/**
	 * Gets the position of the robot with the center of the field as the origin
	 * @return The position of the robot
	 */
	public Optional<Pose3d> getBotpose() {
		return limelight.getJsonDump().flatMap(getArr("botpose")).flatMap(this::parseArr);
	}

	/**
	 * Gets the position of the robot with the blue driverstation as the origin
	 * @return The position of the robot
	 */
	public Optional<Pose3d> getBotposeBlue() {
		return limelight.getJsonDump().flatMap(getArr("botpose_wpiblue")).flatMap(this::parseArr);
	}

	/**
	 * Gets the position of the robot with the red driverstation as the origin
	 * @return The position of the robot
	 */
	public Optional<Pose3d> getBotposeRed() {
		return limelight.getJsonDump().flatMap(getArr("botpose_wpired")).flatMap(this::parseArr);
	}

	/**
	 * Gets the id of the targeted tag.
	 */
	public OptionalLong getTagID() {
		return unboxLong(limelight.getJsonDump().flatMap(getLong("")));
	}
}
