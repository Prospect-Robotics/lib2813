package com.team2813.lib2813.subsystems;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assumptions.*;

import com.team2813.lib2813.control.ControlMode;
import com.team2813.lib2813.control.Motor;
import com.team2813.lib2813.testing.FakeMotor;
import com.team2813.lib2813.testing.junit.jupiter.CommandTester;
import com.team2813.lib2813.testing.junit.jupiter.WPILibExtension;
import edu.wpi.first.wpilibj2.command.Command;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.EnumSource;

@ParameterizedClass
@EnumSource(ControlMode.class)
@ExtendWith(WPILibExtension.class)
public final class ParameterizedIntakeSubsystemTest {

  private static class ConcreteParameterizedIntakeSubsystem extends ParameterizedIntakeSubsystem {
    protected ConcreteParameterizedIntakeSubsystem(Motor intakeMotor, Params params) {
      super(intakeMotor, params);
    }
  }

  private final FakeMotor fakeMotor = new FakeMotor();
  private final ParameterizedIntakeSubsystem.Params params;

  public ParameterizedIntakeSubsystemTest(ControlMode controlMode) {
    params =
        ParameterizedIntakeSubsystem.Params.builder()
            .setControlMode(controlMode)
            .setIntakeDemand(42)
            .setOuttakeDemand(-3.1415)
            .build();
  }

  @Test
  public void initialState() {
    try (var ignored = new ConcreteParameterizedIntakeSubsystem(fakeMotor, params)) {
      fakeMotor.assertIsStopped();
    }
  }

  @Test
  public void intakeItem(CommandTester commandTester) {
    try (var intake = new ConcreteParameterizedIntakeSubsystem(fakeMotor, params)) {
      Command command = intake.intakeItemCommand();
      fakeMotor.assertIsStopped();

      commandTester.runUntilComplete(command);

      assertThat(fakeMotor.demand).isWithin(0.01).of(params.intakeDemand());
    }
  }

  @Test
  public void stopAfterIntakingItem(CommandTester commandTester) {
    assumeTrue(!ControlMode.MOTION_MAGIC.equals(params.controlMode()));
    try (var intake = new ConcreteParameterizedIntakeSubsystem(fakeMotor, params)) {
      Command command = intake.intakeItemCommand();
      commandTester.runUntilComplete(command);
      command = intake.stopMotorCommand();
      assertMotorIsRunning();

      commandTester.runUntilComplete(command);

      fakeMotor.assertIsStopped();
    }
  }

  @Test
  public void outtakeItem(CommandTester commandTester) {
    try (var intake = new ConcreteParameterizedIntakeSubsystem(fakeMotor, params)) {
      intake.intakeGamePiece();
      Command command = intake.outtakeItemCommand();
      assertMotorIsRunning();

      commandTester.runUntilComplete(command);

      assertThat(fakeMotor.demand).isWithin(0.01).of(params.outtakeDemand());
    }
  }

  @Test
  public void stopAfterOuttakingItem(CommandTester commandTester) {
    assumeTrue(!ControlMode.MOTION_MAGIC.equals(params.controlMode()));
    try (var intake = new ConcreteParameterizedIntakeSubsystem(fakeMotor, params)) {
      intake.intakeGamePiece();
      Command command = intake.outtakeItemCommand();
      commandTester.runUntilComplete(command);
      command = intake.stopMotorCommand();
      assertMotorIsRunning();

      commandTester.runUntilComplete(command);

      fakeMotor.assertIsStopped();
    }
  }

  private void assertMotorIsRunning() {
    assertThat(fakeMotor.demand).isNotWithin(0.01).of(0.0);
  }
}
