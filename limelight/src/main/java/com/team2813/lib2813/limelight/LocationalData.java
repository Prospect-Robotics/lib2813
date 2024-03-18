package com.team2813.lib2813.limelight;

import static com.team2813.lib2813.limelight.JSONHelper.getLong;
import static com.team2813.lib2813.limelight.JSONHelper.getRoot;
import static com.team2813.lib2813.limelight.JSONHelper.unboxLong;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Function;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Get positional data from limelight
 * @see Limelight
 */
public class LocationalData {
	private final Limelight limelight;
	
	LocationalData(Limelight limelight) {
		this.limelight = limelight;
	}

	private boolean invalidArray(JSONArray arr) {
		boolean simple = arr.length() != 6;
		if (simple) {
			return simple;
		}
		Integer zero = Integer.valueOf(0);
		for (Object o : arr) {
			if (!zero.equals(o)) {
				return false;
			}
		}
		return true;
	}

	private Optional<Pose3d> parseArr(JSONArray arr) {
		if (invalidArray(arr)) {
			SmartDashboard.putBoolean("invalid array", true);
			return Optional.empty();
		}
		SmartDashboard.putBoolean("invalid array", false);
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
		OptionalLong a = limelight.getCaptureLatency();
		OptionalLong b = limelight.getTargetingLatency();
		if (a.isPresent() && b.isPresent()) {
			return OptionalLong.of(a.getAsLong() + b.getAsLong());
		}
		return OptionalLong.empty();
	} 

	/**
	 * Gets the position of the robot with the center of the field as the origin
	 * @return The position of the robot
	 */
	public Optional<Pose3d> getBotpose() {
		return limelight.getJsonDump().flatMap(getRoot()).flatMap(getArr("botpose")).flatMap(this::parseArr);
	}

	/**
	 * Gets the position of the robot with the blue driverstation as the origin
	 * @return The position of the robot
	 */
	public Optional<Pose3d> getBotposeBlue() {
		return limelight.getJsonDump().flatMap(getRoot()).flatMap(getArr("botpose_wpiblue")).flatMap(this::parseArr);
	}

	/**
	 * Gets the position of the robot with the red driverstation as the origin
	 * @return The position of the robot
	 */
	public Optional<Pose3d> getBotposeRed() {
		return limelight.getJsonDump().flatMap(getRoot()).flatMap(getArr("botpose_wpired")).flatMap(this::parseArr);
	}

	/**
	 * Gets the id of the targeted tag.
	 */
	public OptionalLong getTagID() {
		return unboxLong(limelight.getJsonDump().flatMap(getRoot()).flatMap(getLong("")));
	}
}
