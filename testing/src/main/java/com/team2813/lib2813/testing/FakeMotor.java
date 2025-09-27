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
  private double feedForward = 0.0f;
  public double demand = 0.0f;

  public Resistance resistance = Resistance.ofBaseUnits(0.025f, Units.Ohms);
  private ControlMode controlMode = ControlMode.VOLTAGE;

  public Voltage getMotorVoltage() {
    if (!ControlMode.VOLTAGE.equals(controlMode)) {
      throw new IllegalStateException("Cannot get voltage when controlMode is " + controlMode);
    }
    return Voltage.ofBaseUnits(demand, Units.Volts);
  }

  /**
   * Gets the feedforward that was applied.
   *
   * @return feedforward, in fractional units between -1 and +1.
   */
  public double getFeedForward() {
    return switch (controlMode) {
      case VELOCITY, MOTION_MAGIC -> feedForward;
      default ->
          throw new IllegalStateException(
              "Cannot get feedforward when controlMode is " + controlMode);
    };
  }

  public final void assertIsStopped() {
    assertThat(isStopped).isTrue();
  }

  @Override
  public final void set(ControlMode mode, double demand) {
    set(mode, demand, 0);
  }

  @Override
  public void set(ControlMode mode, double demand, double feedForward) {
    Objects.requireNonNull(mode, "mode should not be null");
    if (feedForward >= 0) {
      assertThat(feedForward).isAtMost(1.01);
    } else {
      assertThat(feedForward).isAtLeast(-1.01);
    }

    if (controlMode != ControlMode.MOTION_MAGIC) {
      isStopped = (Math.abs(demand) <= 0.0001 && Math.abs(feedForward) <= 0.0001);
    }
    this.controlMode = mode;
    this.demand = demand;
    this.feedForward = feedForward;
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
