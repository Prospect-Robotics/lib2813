package com.team2813.lib2813.limelight;

import java.util.Optional;
import java.util.OptionalDouble;

import edu.wpi.first.math.geometry.Pose3d;

/**
 * Get positional data from limelight
 * @see Limelight
 */
public interface LocationalData {

	Optional<Pose3d> getBotpose();
	
	Optional<Pose3d> getBotposeBlue();
	
	Optional<Pose3d> getBotposeRed();

	OptionalDouble getCaptureLatency();

	OptionalDouble getTargetingLatency();

	default OptionalDouble lastMSDelay(){
		OptionalDouble a = getCaptureLatency();
		OptionalDouble b = getTargetingLatency();
		if (a.isPresent() && b.isPresent()) {
			return OptionalDouble.of(a.getAsDouble() + b.getAsDouble());
		}
		return OptionalDouble.empty();
	}
}
