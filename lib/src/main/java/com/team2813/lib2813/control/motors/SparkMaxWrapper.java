/*
Copyright 2024-2025 Prospect Robotics SWENext Club

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

import com.revrobotics.RelativeEncoder;
import com.revrobotics.spark.ClosedLoopSlot;
import com.revrobotics.spark.SparkBase;
import com.revrobotics.spark.SparkLowLevel;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.AlternateEncoderConfig;
import com.revrobotics.spark.config.ClosedLoopConfig;
import com.revrobotics.spark.config.SparkBaseConfig;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.team2813.lib2813.control.ControlMode;
import com.team2813.lib2813.control.InvertType;
import com.team2813.lib2813.control.PIDMotor;
import com.team2813.lib2813.util.ConfigUtils;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import java.util.ArrayList;
import java.util.List;

public class SparkMaxWrapper implements PIDMotor {
  private final List<SparkMax> followers = new ArrayList<>();
  private final SparkBase motor;
  private final RelativeEncoder encoder;
  private final boolean inverted;
  private final SparkBaseConfig config;
  private final SparkBase.ResetMode resetMode;
  private final SparkBase.PersistMode persistMode;

  /**
   * Create a new object to control a SPARK MAX motor Controller
   *
   * @param deviceId The device ID.
   * @param type The motor type connected to the controller. Brushless motor wires must be connected
   *     to their matching colors and the hall sensor must be plugged in. Brushed motors must be
   *     connected to the Red and Black terminals only.
   * @param inverted Whether the motor is inverted
   */
  public SparkMaxWrapper(int deviceId, SparkLowLevel.MotorType type, InvertType inverted) {
    motor = new SparkMax(deviceId, type);
    this.inverted = inverted.sparkMaxInvert().orElseThrow();
    config = new SparkMaxConfig().apply(new AlternateEncoderConfig().inverted(this.inverted));
    persistMode = SparkBase.PersistMode.kNoPersistParameters;
    resetMode = SparkBase.ResetMode.kResetSafeParameters;
    ConfigUtils.revConfig(() -> motor.configure(config, resetMode, persistMode));
    encoder = motor.getEncoder();
  }

  @Override
  public void set(ControlMode controlMode, double demand) {
    set(controlMode, demand, 0);
  }

  @Override
  public void set(ControlMode controlMode, double demand, double feedForward) {
    switch (controlMode) {
      case VOLTAGE:
        motor.setVoltage(demand);
        break;
      case DUTY_CYCLE:
      default:
        motor.set(demand);
        break;
    }
  }

  @Override
  public Angle getPositionMeasure() {
    return Units.Rotations.of(encoder.getPosition());
  }

  @Override
  public Current getAppliedCurrent() {
    return Units.Amps.of(motor.getOutputCurrent());
  }

  @Override
  public void stopMotor() {
    motor.stopMotor();
  }

  @Override
  public void setPosition(Angle position) {
    encoder.setPosition(position.in(Units.Rotations));
  }

  @Override
  public AngularVelocity getVelocityMeasure() {
    return Units.Rotations.per(Units.Minute).of(encoder.getVelocity());
  }

  @Override
  public void configPIDF(int slot, double p, double i, double d, double f) {
    ClosedLoopSlot[] slots = ClosedLoopSlot.values();
    if (slot < 0 || slot > slots.length) {
      throw new RuntimeException("Invalid slot!");
    }
    ClosedLoopSlot cSlot = slots[slot];
    config.apply(new ClosedLoopConfig().pidf(p, i, d, f, cSlot));
    ConfigUtils.revConfig(() -> motor.configure(config, resetMode, persistMode));
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

  public void addFollower(int deviceId, SparkLowLevel.MotorType type, InvertType inverted) {
    SparkMax follower = new SparkMax(deviceId, type);
    boolean isInverted =
        switch (inverted) {
          case CLOCKWISE, COUNTER_CLOCKWISE -> inverted.sparkMaxInvert().orElseThrow();
          case FOLLOW_MASTER -> this.inverted;
          case OPPOSE_MASTER -> !this.inverted;
        };
    ConfigUtils.revConfig(
        () ->
            follower.configure(
                new SparkMaxConfig().follow(motor).inverted(isInverted), resetMode, persistMode));
    followers.add(follower); // add to follower list so CANSparkMax follower object is preserved
  }
}
