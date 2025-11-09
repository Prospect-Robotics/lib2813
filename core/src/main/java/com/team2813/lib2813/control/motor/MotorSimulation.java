/*
Copyright 2026 Prospect Robotics SWENext Club

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
package com.team2813.lib2813.control.motor;

import com.team2813.lib2813.control.ControlMode;
import com.team2813.lib2813.control.Motor;
import com.team2813.lib2813.control.PIDMotor;
import com.team2813.lib2813.robot.PeriodicRegistry;
import com.team2813.lib2813.robot.RobotState;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.simulation.LinearSystemSim;

/** Helper methods for working with simulation for motors. */
public class MotorSimulation {

  /**
   * Wraps a motor and a simulated motor to simulate a flywheel mechanism.
   *
   * <p>The simulated motor will be updated when the motor is updated.
   *
   * @param motor Motor to wrap
   * @param periodicRegistry Registry that will be used to register periodic functions
   * @param sim Simulation to associate with the motor
   * @return Wrapped motor
   */
  public static Motor flywheel(
      Motor motor, PeriodicRegistry periodicRegistry, LinearSystemSim<?, N1, ?> sim) {
    if (motor instanceof PIDMotor pidMotor) {
      return new PIDMotorWrapper(pidMotor, periodicRegistry, sim);
    }
    return new MotorWrapper<>(motor, periodicRegistry, sim);
  }

  /**
   * Wraps a pid motor and a simulated motor to simulate a flywheel mechanism.
   *
   * <p>The simulated motor will be updated when the motor is updated.
   *
   * @param motor Motor to wrap
   * @param periodicRegistry Registry that will be used to register periodic functions
   * @param sim Simulation to associate with the motor
   * @return Wrapped motor
   */
  public static PIDMotor flywheel(
      PIDMotor motor, PeriodicRegistry periodicRegistry, LinearSystemSim<?, N1, ?> sim) {
    return new PIDMotorWrapper(motor, periodicRegistry, sim);
  }

  private static class MotorWrapper<M extends Motor> extends MotorDecorator<M> {
    private final LinearSystemSim<?, N1, ?> sim;
    private long lastSimUpdateTimeMillis = 0;

    MotorWrapper(M motor, PeriodicRegistry periodicRegistry, LinearSystemSim<?, N1, ?> sim) {
      super(motor);
      this.sim = sim;
      periodicRegistry.addSimulationPeriodic(this::simulationPeriodic);
    }

    private void simulationPeriodic(RobotState robotState) {
      long currentTimeMicros = RobotController.getFPGATime();
      sim.update((currentTimeMicros - lastSimUpdateTimeMillis) / 1000.0);
      lastSimUpdateTimeMillis = currentTimeMicros;
    }

    private double toVelocity(ControlMode mode, double demand) {
      return switch (mode) {
        case VELOCITY, DUTY_CYCLE -> demand;
        case VOLTAGE -> demand / RobotController.getBatteryVoltage();
        default -> {
          throw new IllegalArgumentException("Mode not supported: " + mode);
        }
      };
    }

    @Override
    public void set(ControlMode mode, double demand) {
      double velocity = toVelocity(mode, demand);
      super.set(mode, demand);
      sim.setInput(velocity);
    }

    @Override
    public void set(ControlMode mode, double demand, double feedForward) {
      double velocity = toVelocity(mode, demand);
      motor.set(mode, demand, feedForward);
      sim.setInput(velocity);
    }
  }

  private static class PIDMotorWrapper extends MotorWrapper<PIDMotor> implements PIDMotor {

    PIDMotorWrapper(
        PIDMotor motor, PeriodicRegistry periodicRegistry, LinearSystemSim<?, N1, ?> sim) {
      super(motor, periodicRegistry, sim);
    }

    @Override
    public void configPIDF(int slot, double p, double i, double d, double f) {
      motor.configPIDF(slot, p, i, d, f);
    }

    @Override
    public void configPIDF(double p, double i, double d, double f) {
      motor.configPIDF(p, i, d, f);
    }

    @Override
    public void configPID(int slot, double p, double i, double d) {
      motor.configPID(slot, p, i, d);
    }

    @Override
    public void configPID(double p, double i, double d) {
      motor.configPID(p, i, d);
    }

    @Override
    public Angle getPositionMeasure() {
      return motor.getPositionMeasure();
    }

    @Override
    public void setPosition(Angle position) {
      motor.setPosition(position);
    }

    @Override
    public AngularVelocity getVelocityMeasure() {
      return motor.getVelocityMeasure();
    }
  }

  private MotorSimulation() {
    throw new AssertionError("Not instantiable");
  }
}
