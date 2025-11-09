package com.team2813.lib2813.control.motors;

import com.team2813.lib2813.control.ControlMode;
import com.team2813.lib2813.control.Motor;
import com.team2813.lib2813.control.PIDMotor;
import com.team2813.lib2813.robot.PeriodicRegistry;
import com.team2813.lib2813.robot.RobotState;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.simulation.LinearSystemSim;

/** Helper methods for working with simulation for motors. */
public class MotorSimulation {

  /**
   * Wraps a motor and a simulated motor. The simulated motor will be updated when the motor is
   * updated.
   *
   * @param motor Motor to wrap
   * @param periodicRegistry Registry that will be used to register periodic functions
   * @param sim Simulation to associate with the motor
   * @return Wrapped motor
   */
  public static Motor wrap(
      Motor motor, PeriodicRegistry periodicRegistry, LinearSystemSim<?, N1, ?> sim) {
    if (motor instanceof PIDMotor pidMotor) {
      return new PIDMotorWrapper(pidMotor, periodicRegistry, sim);
    }
    return new MotorWrapper<>(motor, periodicRegistry, sim);
  }

  /**
   * Wraps a pid motor and a simulated motor. The simulated motor will be updated when the motor is
   * updated.
   *
   * @param motor Motor to wrap
   * @param periodicRegistry Registry that will be used to register periodic functions
   * @param sim Simulation to associate with the motor
   * @return Wrapped motor
   */
  public static PIDMotor wrap(
      PIDMotor motor, PeriodicRegistry periodicRegistry, LinearSystemSim<?, N1, ?> sim) {
    return new PIDMotorWrapper(motor, periodicRegistry, sim);
  }

  private static class MotorWrapper<M extends Motor> implements Motor {
    protected final M motor;
    private final LinearSystemSim<?, N1, ?> sim;
    private long lastSimUpdateTimeMillis = 0;

    MotorWrapper(M motor, PeriodicRegistry periodicRegistry, LinearSystemSim<?, N1, ?> sim) {
      this.motor = motor;
      this.sim = sim;
      periodicRegistry.addSimulationPeriodic(this::simulationPeriodic);
    }

    private void simulationPeriodic(RobotState robotState) {
      long currentTimeMicros = RobotController.getFPGATime();
      sim.update((currentTimeMicros - lastSimUpdateTimeMillis) / 1000.0);
      lastSimUpdateTimeMillis = currentTimeMicros;
    }

    @Override
    public void set(ControlMode mode, double demand) {
      motor.set(mode, demand);
      sim.setInput(demand);
    }

    @Override
    public void set(ControlMode mode, double demand, double feedForward) {
      motor.set(mode, demand, feedForward);
      sim.setInput(demand);
    }

    @Override
    public Current getAppliedCurrent() {
      return motor.getAppliedCurrent();
    }

    @Override
    public void disable() {
      motor.disable();
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
    public double position() {
      return motor.position();
    }

    @Override
    public Angle getPositionMeasure() {
      return motor.getPositionMeasure();
    }

    @Override
    public void setPosition(double position) {
      motor.setPosition(position);
    }

    @Override
    public double getVelocity() {
      return motor.getVelocity();
    }
  }

  private MotorSimulation() {
    throw new AssertionError("Not instantiable");
  }
}
