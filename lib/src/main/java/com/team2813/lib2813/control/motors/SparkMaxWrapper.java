package com.team2813.lib2813.control.motors;

import com.revrobotics.*;
import com.team2813.lib2813.control.ControlMode;
import com.team2813.lib2813.control.PIDMotor;
import com.team2813.lib2813.control.InvertType;
import com.team2813.lib2813.util.ConfigUtils;

import edu.wpi.first.units.Angle;
import edu.wpi.first.units.Measure;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.Velocity;

import java.util.ArrayList;
import java.util.List;

public class SparkMaxWrapper extends CANSparkBase implements PIDMotor {
    private final List<CANSparkMax> followers = new ArrayList<>();
    private final RelativeEncoder encoder;
    private final SparkPIDController pidController;

    /**
     * Create a new object to control a SPARK MAX motor Controller
     *
     * @param deviceId The device ID.
     * @param type     The motor type connected to the controller. Brushless motor wires must be connected
     *                 to their matching colors and the hall sensor must be plugged in. Brushed motors must be
     *                 connected to the Red and Black terminals only.
     * @param inverted Whether the motor is inverted
     */
    public SparkMaxWrapper(int deviceId, CANSparkLowLevel.MotorType type, InvertType inverted) {
        super(deviceId, type);
        encoder = getEncoder();
        pidController = getPIDController();

        ConfigUtils.revConfig(this::restoreFactoryDefaults);

        ConfigUtils.revConfig(() -> enableVoltageCompensation(12));
        ConfigUtils.revConfig(() -> setSmartCurrentLimit(40));
        setInverted(inverted.sparkMaxInvert().get());
    }

    @Override
    public void set(ControlMode controlMode, double demand) {
        pidController.setReference(demand, controlMode.getSparkMode());
    }

    @Override
    public void set(ControlMode controlMode, double demand, double feedForward) {
        pidController.setReference(demand, controlMode.getSparkMode(), 0, feedForward);
    }

    @Override
    public double position() {
        return encoder.getPosition();
    }

	@Override
	public Measure<Angle> getPositionMeasure() {
		return Units.Rotations.of(encoder.getPosition());
	}

    @Override
    public void setPosition(double position) {
        encoder.setPosition(position);
    }

	@Override
	public void setPosition(Measure<Angle> position) {
		encoder.setPosition(position.in(Units.Rotations));
	}

    @Override
    public double getVelocity() {
        return encoder.getVelocity();
    }

	@Override
	public Measure<Velocity<Angle>> getVelocityMeasure() {
		return Units.Rotations.per(Units.Minute).of(encoder.getVelocity());
	}

    @Override
    public void configPIDF(int slot, double p, double i, double d, double f) {
        ConfigUtils.revConfig(() -> pidController.setP(p, slot));
        ConfigUtils.revConfig(() -> pidController.setI(i, slot));
        ConfigUtils.revConfig(() -> pidController.setD(d, slot));
        ConfigUtils.revConfig(() -> pidController.setFF(f, slot));
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

    public void configMotionMagic(int slot, double minVelocity, double maxVelocity, double maxAcceleration) {
        ConfigUtils.revConfig(() -> pidController.setSmartMotionMinOutputVelocity(minVelocity, slot));
        ConfigUtils.revConfig(() -> pidController.setSmartMotionMaxVelocity(maxVelocity, slot));
        ConfigUtils.revConfig(() -> pidController.setSmartMotionMaxAccel(maxAcceleration, slot));
    }

    public void configMotionMagic(double minVelocity, double maxVelocity, double maxAcceleration) {
        configMotionMagic(0, minVelocity, maxVelocity, maxAcceleration);
    }

    public void configMotionMagic(int slot, double maxVelocity, double maxAcceleration) {
        configMotionMagic(slot, 0, maxVelocity, maxAcceleration);
    }


    public void configMotionMagic(double maxVelocity, double maxAcceleration) {
        configMotionMagic(0, 0, maxVelocity, maxAcceleration);
    }

    public void configPeriodicFramePeriod(CANSparkLowLevel.PeriodicFrame frame, int periodMs) {
        ConfigUtils.revConfig(() -> setPeriodicFramePeriod(frame, periodMs));
    }

    public void addFollower(int deviceId, CANSparkLowLevel.MotorType type, boolean inverted) {
        CANSparkMax follower = new CANSparkMax(deviceId, type);
        follower.follow(this, inverted);
        followers.add(follower); // add to follower list so CANSparkMax follower object is preserved
    }
}

