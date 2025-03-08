package com.team2813.lib2813.limelight;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;

import edu.wpi.first.math.geometry.Pose3d;

/**
 * Get positional data from limelight
 * @see Limelight
 */
public interface LocationalData {

	/** Returns {@code true} if the limelight has identified a target. */
	boolean hasTarget();

	/**
	 * Gets the position of the robot with the center of the field as the origin.
	 * @return The position of the robot
	 */
	Optional<Pose3d> getBotpose();

	/**
	 * Gets the position of the robot with the blue driverstation as the origin
	 * @return The position of the robot
	 */
	Optional<Pose3d> getBotposeBlue();

	/**
	 * Gets the position of the robot with the red driverstation as the origin
	 * @return The position of the robot
	 */
	Optional<Pose3d> getBotposeRed();

	OptionalDouble getCaptureLatency();

	OptionalDouble getTargetingLatency();

	OptionalDouble getTimestamp();

	default OptionalDouble lastMSDelay(){
		OptionalDouble a = getCaptureLatency();
		OptionalDouble b = getTargetingLatency();
		if (a.isPresent() && b.isPresent()) {
			return OptionalDouble.of(a.getAsDouble() + b.getAsDouble());
		}
		return OptionalDouble.empty();
	}

	/**
	 * Gets the set of all visible tags
	 * @return The visible tags
	 */
	Set<Integer> getVisibleTags();
}
