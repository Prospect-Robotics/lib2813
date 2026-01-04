/*
Copyright 2023-2025 Prospect Robotics SWENext Club

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.team2813.lib2813.control.motors;

import static com.team2813.lib2813.util.InputValidation.checkCanId;

import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.SlotConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.configs.TalonFXConfigurator;
import com.ctre.phoenix6.controls.*;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.MotorAlignmentValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.team2813.lib2813.control.ControlMode;
import com.team2813.lib2813.control.DeviceInformation;
import com.team2813.lib2813.control.InvertType;
import com.team2813.lib2813.control.PIDMotor;
import com.team2813.lib2813.subsystems.MotorSubsystem;
import com.team2813.lib2813.util.InvalidCanIdException;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TalonFXWrapper implements PIDMotor {
  /** A list of followers, so that they aren't garbage collected */
  private final List<TalonFX> followers = new ArrayList<>();

  /** the internal motor */
  private final TalonFX motor;

  private final DeviceInformation information;

  /**
   * Create a TalonFXWrapper on the specified canbus.
   *
   * @param canID [0, 62] the can ID of the motor
   * @param canbus the canbus that the motor is on
   * @param invertType the invert type
   * @throws NullPointerException if either {@code invertType} or {@code canbus} are null
   * @throws IllegalArgumentException if {@code invertType} is not in {@link
   *     InvertType#rotationValues}. In other words, this exception is thrown when passed an {@link
   *     InvertType} that is for following motors
   * @throws InvalidCanIdException if the CAN id is invalid
   */
  public TalonFXWrapper(int canID, CANBus canbus, InvertType invertType) {
    Objects.requireNonNull(invertType, "invertType should not be null");
    Objects.requireNonNull(canbus, "canbus should not be null");
    if (!InvertType.rotationValues.contains(invertType)) {
      throw new IllegalArgumentException("invertType invalid");
    }
    motor = new TalonFX(checkCanId(canID), canbus);

    TalonFXConfiguration config = new TalonFXConfiguration();
    // should never throw anything, as the tests guarantee that everything in
    // rotationValues
    // returns a non-empty value with phoenixInvert
    config.MotorOutput.Inverted = invertType.phoenixInvert().orElseThrow(AssertionError::new);
    config.CurrentLimits =
        new CurrentLimitsConfigs().withStatorCurrentLimit(40).withSupplyCurrentLimitEnable(true);
    TalonFXConfigurator configurator = motor.getConfigurator();
    configurator.apply(config);

    information = new DeviceInformation(canID, canbus);
  }

  /**
   * Create a TalonFXWrapper on the specified canbus name.
   *
   * @param canID [0, 62] the can ID of the motor
   * @param canbusName the canbus that the motor is on
   * @param invertType the invert type
   * @throws NullPointerException if either {@code invertType} or {@code canbus} are null
   * @throws IllegalArgumentException if {@code invertType} is not in {@link
   *     InvertType#rotationValues}. In other words, this exception is thrown when passed an {@link
   *     InvertType} that is for following motors
   * @throws InvalidCanIdException if the CAN id is invalid
   * @deprecated Constructing {@code DeviceInformation} with a CAN bus string is deprecated for
   *     removal in the 2027 season. Construct instances using a {@link CANBus} instance instead.
   */
  @Deprecated(forRemoval = true)
  public TalonFXWrapper(int canID, String canbusName, InvertType invertType) {
    this(
        canID,
        new CANBus(Objects.requireNonNull(canbusName, "canbus should not be null")),
        invertType);
  }

  /**
   * Create a TalonFXWrapper on the RoboRIO's canbus
   *
   * @param canID [0, 62] the can ID of the motor
   * @param invertType the invert type
   * @throws NullPointerException if {@code invertType} is null
   * @throws IllegalArgumentException if {@code invertType} is not in {@link
   *     InvertType#rotationValues}. In other words, this exception is thrown when passed an {@link
   *     InvertType} that is for following motors
   * @throws InvalidCanIdException if the CAN id is invalid
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
    // returns a non-empty value with phoenixInvert
    config.MotorOutput.Inverted = invertType.phoenixInvert().orElseThrow(AssertionError::new);
    config.CurrentLimits =
        new CurrentLimitsConfigs().withStatorCurrentLimit(40).withSupplyCurrentLimitEnable(true);
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
      case VOLTAGE:
        VoltageOut vo = new VoltageOut(demand);
        motor.setControl(vo);
        break;
      default:
        DutyCycleOut dc = new DutyCycleOut(demand);
        motor.setControl(dc);
        break;
    }
  }

  @Override
  public Angle getPositionMeasure() {
    return Units.Rotations.of(motor.getPosition().getValueAsDouble());
  }

  @Override
  public Current getAppliedCurrent() {
    return motor.getStatorCurrent().getValue();
  }

  @Override
  public void setPosition(Angle position) {
    motor.setPosition(position.in(Units.Rotations));
  }

  @Override
  public AngularVelocity getVelocityMeasure() {
    return Units.RotationsPerSecond.of(motor.getVelocity().getValueAsDouble());
  }

  public TalonFX motor() {
    return motor;
  }

  /**
   * Sets the behavior the motor should exhibit upon receiving a request to stop: {@link
   * MotorSubsystem#stopMotor()}
   *
   * <ul>
   *   <li>Coast: The motor stops applying an input, but continues to move with its inertia.
   *   <li>Brake: The motor stops applying an input, and actively opposes its inertia.
   * </ul>
   *
   * @param mode
   */
  public void setNeutralMode(NeutralModeValue mode) {
    motor.setNeutralMode(mode);
  }

  /**
   * Sends a disable command to the motor, placing it in its neutral value.
   *
   * @see TalonFXWrapper#setNeutralMode(NeutralModeValue)
   */
  @Override
  public void stopMotor() {
    motor.stopMotor();
  }

  @Override
  public void configPIDF(int slot, double p, double i, double d, double f) {
    SlotConfigs conf = new SlotConfigs();
    conf.SlotNumber = slot;
    motor.getConfigurator().apply(conf.withKP(p).withKI(i).withKD(d).withKV(f));
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

  @Deprecated(forRemoval = true)
  public void addFollower(int deviceNumber, String canbus, InvertType invertType) {
    addFollower(deviceNumber, new CANBus(canbus), invertType);
  }

  public void addFollower(int deviceNumber, CANBus canbus, InvertType invertType) {
    TalonFX follower = new TalonFX(checkCanId(deviceNumber), canbus);
    Optional<MotorAlignmentValue> alignmentValue = toMotorAlignmentValue(invertType);
    if (alignmentValue.isEmpty()) {
      TalonFXConfiguration conf = new TalonFXConfiguration();
      // guaranteed to succeed
      conf.MotorOutput.Inverted = invertType.phoenixInvert().orElseThrow(AssertionError::new);
      follower.setControl(new StrictFollower(information.id()));
    } else {
      follower.setControl(new Follower(information.id(), alignmentValue.get()));
    }
    followers.add(follower); // add to follower list so TalonFX follower object is preserved
  }

  public void addFollower(int deviceNumber, InvertType invertType) {
    TalonFX follower = new TalonFX(checkCanId(deviceNumber));
    Optional<MotorAlignmentValue> alignmentValue = toMotorAlignmentValue(invertType);
    if (alignmentValue.isEmpty()) {
      TalonFXConfiguration conf = new TalonFXConfiguration();
      // guaranteed to succeed
      conf.MotorOutput.Inverted = invertType.phoenixInvert().orElseThrow(AssertionError::new);
      follower.setControl(new StrictFollower(information.id()));
    } else {
      follower.setControl(new Follower(information.id(), alignmentValue.get()));
    }
    followers.add(follower); // add to follower list so TalonFX follower object is preserved
  }

  private static Optional<MotorAlignmentValue> toMotorAlignmentValue(InvertType invertType) {
    if (InvertType.rotationValues.contains(invertType)) {
      return Optional.empty();
    }
    return invertType.equals(InvertType.FOLLOW_MASTER)
        ? Optional.of(MotorAlignmentValue.Aligned)
        : Optional.of(MotorAlignmentValue.Opposed);
  }
}
