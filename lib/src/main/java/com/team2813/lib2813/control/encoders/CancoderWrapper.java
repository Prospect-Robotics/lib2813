package com.team2813.lib2813.control.encoders;
import com.team2813.lib2813.control.DeviceInformation;
import com.team2813.lib2813.control.Encoder;
import com.team2813.lib2813.util.ConfigUtils;

import edu.wpi.first.units.Angle;
import edu.wpi.first.units.Measure;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.Velocity;

import com.ctre.phoenix6.hardware.CANcoder;

public class CancoderWrapper implements Encoder {
	private CANcoder cancoder;
	private DeviceInformation info;
	public CancoderWrapper(int id, String canbus) {
		cancoder = new CANcoder(id, canbus);
		info = new DeviceInformation(id, canbus);
	}
	public CancoderWrapper(int id) {
		cancoder = new CANcoder(id);
		info = new DeviceInformation(id);
	}
	@Override
	public double position() {
		return cancoder.getPosition().getValueAsDouble();
	}

	@Override
	public Measure<Angle> getPositionMeasure() {
		return Units.Rotations.of(cancoder.getPosition().getValueAsDouble());
	}

	@Override
	public void setPosition(double position) {
		ConfigUtils.phoenix6Config(() -> cancoder.setPosition(position));
	}
	@Override
	public void setPosition(Measure<Angle> position) {
		ConfigUtils.phoenix6Config(() -> cancoder.setPosition(position.in(Units.Rotations)));
	}
	public CANcoder encoder() {
		return cancoder;
	}

	@Override
	public double getVelocity() {
		return cancoder.getVelocity().getValueAsDouble();
	}
	@Override
	public Measure<Velocity<Angle>> getVelocityMeasure() {
		return Units.RotationsPerSecond.of(cancoder.getVelocity().getValue());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CancoderWrapper))
			return false;
		CancoderWrapper other = (CancoderWrapper) obj;
		return info.equals(other.info);
	}
}
