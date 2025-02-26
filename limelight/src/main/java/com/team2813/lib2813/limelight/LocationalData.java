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

	/**
	 * Capture latency in milliseconds.
	 *
	 * <p>Per the Limelight docs, this is the time between the end of the exposure of the middle row
	 * to the beginning of the tracking loop.
	 *
	 * @deprecated Use {@link #getFpgaTimestamp()}
	 */
	@Deprecated
	OptionalDouble getCaptureLatency();

	/**
	 * Targeting latency in milliseconds.
	 *
	 * <p>Per the Limelight docs, this is the time consumed by the tracking loop this frame.
	 *
	 * @deprecated Use {@link #getFpgaTimestamp()}
	 */
	@Deprecated
	OptionalDouble getTargetingLatency();

	@Deprecated
	default OptionalDouble lastMSDelay(){
		OptionalDouble a = getCaptureLatency();
		OptionalDouble b = getTargetingLatency();
		if (a.isPresent() && b.isPresent()) {
			return OptionalDouble.of(a.getAsDouble() + b.getAsDouble());
		}
		return OptionalDouble.empty();
	}

	/**
	 * Gets the estimated time of the vision measurement.
	 *
	 * <p>This value needs to be passed to {@code com.ctre.phoenix6.Utils.fpgaToCurrentTime()},
	 * and then the resulting value can then be passed to
	 * {@code com.ctre.phoenix6.swerve.SwerveDrivetrain.addVisionMeasurement()}.
	 *
	 * @return Number of seconds, since the FPGA started, for the measurement.
	 */
	double getFpgaTimestamp();

	/**
	 * Gets the set of all visible tags
	 * @return The visible tags
	 */
	Set<Integer> getVisibleTags();
}
