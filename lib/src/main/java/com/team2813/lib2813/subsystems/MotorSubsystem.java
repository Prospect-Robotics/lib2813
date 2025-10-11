package com.team2813.lib2813.subsystems;

import com.team2813.lib2813.control.Encoder;
import com.team2813.lib2813.control.Motor;
import com.team2813.lib2813.control.PIDMotor;
import edu.wpi.first.units.measure.Angle;
import java.util.function.Supplier;

/**
 * This class is a mask of PositionalMotorSubsystem, kept for backwards compatibility.
 *
 * @see com.team2813.lib2813.subsystems.PositionalMotorSubsystem
 */
public abstract class MotorSubsystem<T extends Supplier<Angle>>
    extends PositionalMotorSubsystem<T> {

  MotorSubsystem(MotorSubsystemConfiguration motorSubsystemConfiguration) {
    super(motorSubsystemConfiguration);
  }

  public static class MotorSubsystemConfiguration extends PositionalMotorSubsystemConfiguration {
    public MotorSubsystemConfiguration(Motor motor, Encoder encoder) {
      super(motor, encoder);
    }

    public MotorSubsystemConfiguration(PIDMotor pidMotor) {
      super(pidMotor);
    }
  }
}
