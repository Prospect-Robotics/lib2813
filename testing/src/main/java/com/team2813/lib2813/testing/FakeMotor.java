package com.team2813.lib2813.testing;

import static com.google.common.truth.Truth.assertThat;

import com.team2813.lib2813.control.ControlMode;
import com.team2813.lib2813.control.Motor;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Resistance;
import edu.wpi.first.units.measure.Voltage;
import java.util.Objects;

public class FakeMotor implements Motor {
  private boolean isStopped = true;
  public double demand = 0.0f;
  public Resistance resistance = Resistance.ofBaseUnits(0.025f, Units.Ohms);
  private ControlMode controlMode = ControlMode.VOLTAGE;

  public Voltage getMotorVoltage() {
    if (!ControlMode.VOLTAGE.equals(controlMode)) {
      throw new IllegalStateException("Cannot get voltage when controlMode is " + controlMode);
    }
    return Voltage.ofBaseUnits(demand, Units.Volts);
  }

  public final void assertIsStopped() {
    assertThat(isStopped).isTrue();
  }

  @Override
  public final void set(ControlMode mode, double demand) {
    controlMode = Objects.requireNonNull(mode, "mode should not be null");
    if (controlMode != ControlMode.MOTION_MAGIC) {
      isStopped = (Math.abs(demand) <= 0.0001);
    }
    this.demand = demand;
  }

  @Override
  public void set(ControlMode mode, double demand, double feedForward) {
    set(mode, demand);
  }

  @Override
  public final Current getAppliedCurrent() {
    try {
      return getMotorVoltage().div(resistance);
    } catch (IllegalStateException e) {
      throw new IllegalStateException("Cannot calculate voltage", e);
    } catch (RuntimeException e) {
      throw new RuntimeException("Cannot calculate voltage", e);
    }
  }
}
