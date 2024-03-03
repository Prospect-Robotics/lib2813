package com.team2813.lib2813.control.motors;

import static com.team2813.lib2813.util.InputValidation.checkCanId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.SlotConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.configs.TalonFXConfigurator;
import com.ctre.phoenix6.controls.DutyCycleOut;
import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.controls.MotionMagicDutyCycle;
import com.ctre.phoenix6.controls.StrictFollower;
import com.ctre.phoenix6.controls.VelocityDutyCycle;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.team2813.lib2813.control.ControlMode;
import com.team2813.lib2813.control.DeviceInformation;
import com.team2813.lib2813.control.InvertType;
import com.team2813.lib2813.control.PIDMotor;
import com.team2813.lib2813.util.InvalidCanIdException;

public class TalonFXWrapper implements PIDMotor {
	/**
	 * Create a TalonFXWrapper on the specified canbus
	 * 
	 * @param canID      [0, 62] the can ID of the motor
	 * @param canbus     the canbus that the motor is on
	 * @param invertType the invert type
	 * @throws NullPointerException     if either {@code invertType} or
	 *                                  {@code canbus}
	 *                                  are null
	 * @throws IllegalArgumentException if {@code invertType} is not in
	 *                                  {@link InvertType#rotationValues}.
	 *                                  In other words, this exception is thrown
	 *                                  when passed an {@link InvertType} that is
	 *                                  for following motors
	 * @throws InvalidCanIdException    if the CAN id is invalid
	 */
	public TalonFXWrapper(int canID, String canbus, InvertType invertType) {
		Objects.requireNonNull(invertType, "invertType should not be null");
		Objects.requireNonNull(canbus, "canbus should not be null");
		if (!InvertType.rotationValues.contains(invertType)) {
			throw new IllegalArgumentException("invertType invalid");
		}
		motor = new TalonFX(checkCanId(canID), canbus);

		TalonFXConfiguration config = new TalonFXConfiguration();
		// should never throw anything, as the tests guarantee that everything in
		// rotationValues
		// returns a non-empy value with phoenixInvert
		config.MotorOutput.Inverted = invertType.phoenixInvert().orElseThrow(AssertionError::new);
		config.CurrentLimits = new CurrentLimitsConfigs()
				.withStatorCurrentLimit(40)
				.withSupplyCurrentThreshold(40)
				.withSupplyTimeThreshold(0.25)
				.withSupplyCurrentLimitEnable(true);
		TalonFXConfigurator configurator = motor.getConfigurator();
		configurator.apply(config);

		information = new DeviceInformation(canID, canbus);
	}

	/**
	 * Create a TalonFXWrapper on the RoboRIO's canbus
	 * 
	 * @param canID      [0, 62] the can ID of the motor
	 * @param invertType the invert type
	 * @throws NullPointerException     if {@code invertType} is null
	 * @throws IllegalArgumentException if {@code invertType} is not in
	 *                                  {@link InvertType#rotationValues}.
	 *                                  In other words, this exception is thrown
	 *                                  when passed an {@link InvertType} that is
	 *                                  for following motors
	 * @throws InvalidCanIdException    if the CAN id is invalid
	 */
	public TalonFXWrapper(int canID, InvertType invertType) {
		Objects.requireNonNull(invertType, "invertType should not be null");
		motor = new TalonFX(checkCanId(canID));
		if (!InvertType.rotationValues.contains(invertType)) {
			throw new IllegalArgumentException("invertType invalid");
		}

		TalonFXConfiguration config = new TalonFXConfiguration();
		// should never throw anything, as the tests guarantee that everything in
		// rotationValues
		// returns a non-empy value with phoenixInvert
		config.MotorOutput.Inverted = invertType.phoenixInvert().orElseThrow(AssertionError::new);
		config.CurrentLimits = new CurrentLimitsConfigs()
				.withStatorCurrentLimit(40)
				.withSupplyCurrentThreshold(40)
				.withSupplyTimeThreshold(0.25)
				.withSupplyCurrentLimitEnable(true);
		TalonFXConfigurator configurator = motor.getConfigurator();
		configurator.apply(config);

		information = new DeviceInformation(canID);
	}

	public void set(ControlMode controlMode, double demand) {
		set(controlMode, demand, 0);
	}

	@Override
	public void set(ControlMode controlMode, double demand, double feedForward) {
		switch (controlMode) {
			case VELOCITY:
				VelocityDutyCycle v = new VelocityDutyCycle(demand);
				v.FeedForward = feedForward;
				motor.setControl(v);
				break;
			case MOTION_MAGIC:
				MotionMagicDutyCycle mm = new MotionMagicDutyCycle(demand);
				mm.FeedForward = feedForward;
				motor.setControl(mm);
				break;
			default:
				DutyCycleOut dc = new DutyCycleOut(demand);
				motor.setControl(dc);
				break;
		}
	}

	@Override
	public double position() {
		return motor.getPosition().getValue();
	}

	@Override
	public void setPosition(double position) {
		motor.setPosition(position);
	}

	@Override
	public double getVelocity() {
		return motor.getVelocity().getValue();
	}

	public TalonFX motor() {
		return motor;
	}

	/**
	 * A list of followers, so that they aren't garbage collected
	 */
	private final List<TalonFX> followers = new ArrayList<>();
	/**
	 * the internal motor
	 */
	private final TalonFX motor;
	private final DeviceInformation information;


	public void setNeutralMode(NeutralModeValue mode) {
		motor.setNeutralMode(mode);
	}

	@Override
	public void configPIDF(int slot, double p, double i, double d, double f) {
		SlotConfigs conf = new SlotConfigs();
		conf.SlotNumber = slot;
		motor.getConfigurator().apply(
				conf.withKP(p).withKI(i).withKD(d).withKV(f));
	}

	@Override
	public void configPIDF(double p, double i, double d, double f) {
		configPIDF(0, p, i, d, f);
	}

	@Override
	public void configPID(int slot, double p, double i, double d) {
		configPIDF(slot, p, i, d, 0);
	}

	@Override
	public void configPID(double p, double i, double d) {
		configPIDF(0, p, i, d, 0);
	}

	public void addFollower(int deviceNumber, String canbus, InvertType invertType) {
		TalonFX follower = new TalonFX(checkCanId(deviceNumber), canbus);
		if (InvertType.rotationValues.contains(invertType)) {
			TalonFXConfiguration conf = new TalonFXConfiguration();
			// guaranteed to succseed
			conf.MotorOutput.Inverted = invertType.phoenixInvert().orElseThrow(AssertionError::new);
			follower.setControl(new StrictFollower(information.id()));
		} else {
			follower.setControl(new Follower(information.id(), invertType.equals(InvertType.OPPOSE_MASTER)));
		}
		followers.add(follower); // add to follower list so TalonFX follower object is preserved
	}

	public void addFollower(int deviceNumber, InvertType invertType) {
		TalonFX follower = new TalonFX(checkCanId(deviceNumber));
		if (InvertType.rotationValues.contains(invertType)) {
			TalonFXConfiguration conf = new TalonFXConfiguration();
			// guaranteed to succseed
			conf.MotorOutput.Inverted = invertType.phoenixInvert().orElseThrow(AssertionError::new);
			follower.setControl(new StrictFollower(information.id()));
		} else {
			follower.setControl(new Follower(information.id(), invertType.equals(InvertType.OPPOSE_MASTER)));
		}
		followers.add(follower); // add to follower list so TalonFX follower object is preserved
	}
}
