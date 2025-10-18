package com.team2813.lib2813.subsystems;

import static edu.wpi.first.units.Units.Radians;

import com.ctre.phoenix6.signals.NeutralModeValue;
import com.team2813.lib2813.control.ControlMode;
import com.team2813.lib2813.control.InvertType;
import com.team2813.lib2813.control.motors.TalonFXWrapper;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.networktables.BooleanPublisher;
import edu.wpi.first.networktables.DoublePublisher;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.units.measure.Angle;
import java.util.function.Supplier;

/**
 * Generic configurable elevator base class. Extend this to create specific elevator implementations
 * (set motor IDs, PID values, enum positions, etc.).
 *
 * <p>Author: Team 2813
 */
public abstract class ElevatorBase extends MotorSubsystem<ElevatorBase.PositionBase> {

  /** Defines an elevator position with a unit-safe angle */
  public interface PositionBase extends Supplier<Angle> {
    Angle get();
  }

  protected final TalonFXWrapper motor;
  protected final PIDController pid;

  protected final BooleanPublisher atPos;
  protected final DoublePublisher pos;

  /**
   * @param motor the TalonFXWrapper to control this elevator
   * @param pid PID controller for closed-loop position control
   * @param gearRatio mechanism gear ratio (rotations -> mechanism movement)
   * @param ntInstance NetworkTables instance for telemetry
   */
  protected ElevatorBase(
      TalonFXWrapper motor,
      PIDController pid,
      NetworkTableInstance ntInstance,
      double acceptableError) {
    super(
        new MotorSubsystemConfiguration(motor)
            .controlMode(ControlMode.VOLTAGE)
            .acceptableError(acceptableError)
            .rotationUnit(Radians)
            .controller(pid));
    this.motor = motor;
    this.pid = pid;
    NetworkTable nt = ntInstance.getTable("Elevator");
    atPos = nt.getBooleanTopic("at position").publish();
    pos = nt.getDoubleTopic("position").publish();
  }

  /**
   * Clamp/control how output is sent to the motor
   *
   * @param output - the output sent to the motor
   * @param setpoint - the PID setpoint of the motor
   */
  @Override
  protected double clampOutput(double output) {
    // TODO Auto-generated method stub
    return super.clampOutput(output);
  }

  /** Telemetry updates */
  @Override
  public void periodic() {
    super.periodic();
    atPos.set(atPosition());
    pos.set(getMeasurement());
  }

  /**
   * Utility method for making a default-configured motor
   *
   * @param masterID - the CAN ID of the master motor
   * @param followerID - the CAN ID of the follower motor
   */
  protected static TalonFXWrapper makeMotor(int masterID, int followerID) {
    TalonFXWrapper wrapper = new TalonFXWrapper(masterID, InvertType.CLOCKWISE);
    wrapper.setNeutralMode(NeutralModeValue.Brake);
    wrapper.addFollower(followerID, InvertType.FOLLOW_MASTER);
    return wrapper;
  }
}
