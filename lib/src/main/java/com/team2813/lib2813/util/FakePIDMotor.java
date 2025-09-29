package com.team2813.lib2813.util;

import com.google.common.truth.Truth;
import com.team2813.lib2813.control.ControlMode;
import com.team2813.lib2813.control.PIDMotor;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.AngularVelocity;

/**
 * A fake implementation of {@link PIDMotor} used for testing.
 *
 * <p>This class simulates motor behavior by storing the most recent control mode and demand value.
 * Assertions are used (via Truth) to enforce correct expectations in tests.
 *
 * <p>Not intended for production use.
 *
 * @author Team 2813
 */
public abstract class FakePIDMotor implements PIDMotor {
  public double demand = 0.0f;
  private ControlMode controlMode;

  /**
   * Gets the current demand if the control mode is {@link ControlMode#VOLTAGE}.
   *
   * @return The current voltage demand.
   */
  public double getVoltage() {
    Truth.assertThat(controlMode).isEqualTo(ControlMode.VOLTAGE);
    return demand;
  }

  @Override
  public void set(ControlMode mode, double demand) {
    Truth.assertThat(mode).isNotNull();
    controlMode = mode;
    this.demand = demand;
  }

  @Override
  public void set(ControlMode mode, double demand, double feedForward) {
    set(mode, demand);
  }

  @Override
  public AngularVelocity getVelocityMeasure() {
    return Units.RadiansPerSecond.of(demand * 20);
  }

  @Override
  public double getVelocity() {
    throw new AssertionError("Called deprecated method");
  }
}
