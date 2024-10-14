package com.team2813.lib2813.control;

import edu.wpi.first.units.Angle;
import edu.wpi.first.units.Measure;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.Velocity;

/**
 * Specifies a device that can perceive rotational positions.
 */
public interface Encoder {
	/**
	 * Gets the position of the encoder
	 * @deprecated This method does not specify position in a specific measurement,
	 * so it is not safe to use. Use {@link #getPositionMeasure()} instead
	 * @return the position of the encoder
	 */
	@Deprecated(forRemoval = true)
	double position();

	/**
	 * Gets the position of the encoder
	 * @return the position of the encoder as a measure
	 */
	Measure<Angle> getPositionMeasure();

	/**
	 * Sets the position of the encoder
	 * @deprecated This method does not specify a unit, so it is not safe to use. Use
	 * {@link #setPosition(Measure)} instead.
	 * @param position the position of the encoder
	 */
	@Deprecated(forRemoval = true)
	void setPosition(double position);

	default void setPosition(Measure<Angle> position) {
		setPosition(position.in(Units.Radians));
	}

	/**
	 * Gets the velocity of the encoder
	 * @deprecated This method does not specify velocity in a specific measurement,
	 * so it is not safe to use. Use {@link #getVelocityMeasure()} instead
	 * @return the velocity that the encoder percieves
	 */
	@Deprecated(forRemoval = true)
	double getVelocity();

	/**
	 * Gets the velocity of the encoder
	 * @return The velocity as a measure
	 */
	default Measure<Velocity<Angle>> getVelocityMeasure() {
		return Units.RadiansPerSecond.of(getVelocity());
	}
}
