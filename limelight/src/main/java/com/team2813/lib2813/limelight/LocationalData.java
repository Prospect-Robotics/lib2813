package com.team2813.lib2813.limelight;

import java.util.Optional;
import java.util.OptionalLong;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.networktables.NetworkTable;

/**
 * Get positional data from limelight
 * @see Limelight
 */
public class LocationalData {
	private final NetworkTable table;
	private final Limelight limelight;
	private static final double[] defaultArr = new double[0];
	
	LocationalData(Limelight limelight) {
		this.limelight = limelight;
		this.table = limelight.networkTable();
	}

	private Optional<Pose3d> parseArr(double[] arr) {
		if (arr == defaultArr || arr.length < 6) {
			return Optional.empty();
		}
		Rotation3d rotation = new Rotation3d(arr[3], arr[4], arr[5]);
		return Optional.of(new Pose3d(arr[0], arr[1], arr[2], rotation));
	}

	/**
	 * Gets the position of the robot with the center of the field as the origin
	 * @return The position of the robot
	 */
	public Optional<Pose3d> getBotpose() {
		return parseArr(table.getEntry("botpose").getDoubleArray(defaultArr));
	}

	/**
	 * Gets the position of the robot with the blue driverstation as the origin
	 * @return The position of the robot
	 */
	public Optional<Pose3d> getBotposeBlue() {
		return parseArr(table.getEntry("botpose_wpiblue").getDoubleArray(defaultArr));
	}

	/**
	 * Gets the position of the robot with the red driverstation as the origin
	 * @return The position of the robot
	 */
	public Optional<Pose3d> getBotposeRed() {
		return parseArr(table.getEntry("botpose_wpired").getDoubleArray(defaultArr));
	}

	/**
	 * Gets the id of the targeted tag.
	 */
	public OptionalLong getTagID() {
		long tid = table.getEntry("tid").getInteger(0);
		return limelight.hasTarget() ?  OptionalLong.of(tid) : OptionalLong.empty();
	}
}
