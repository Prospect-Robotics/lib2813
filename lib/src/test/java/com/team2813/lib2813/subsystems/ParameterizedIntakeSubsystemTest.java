package com.team2813.lib2813.subsystems;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.team2813.lib2813.control.ControlMode;
import com.team2813.lib2813.control.PIDMotor;
import com.team2813.lib2813.testing.junit.jupiter.CommandTester;
import com.team2813.lib2813.testing.junit.jupiter.WPILibExtension;
import com.team2813.lib2813.util.FakePIDMotor;
import edu.wpi.first.wpilibj2.command.Command;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Answers;

/**
 * Parameterized unit tests for {@link ParameterizedIntakeSubsystem}.
 *
 * <p>This test class runs each test for every {@link ControlMode} enum value, ensuring the intake
 * subsystem behaves correctly regardless of control mode configuration.
 *
 * <p>The tests verify:
 *
 * <ul>
 *   <li>Initial motor state is stopped
 *   <li>Motor responds correctly to intake commands
 *   <li>Motor stops correctly after intake/outtake commands
 *   <li>Motor responds correctly to outtake commands
 * </ul>
 */
@ParameterizedClass
@EnumSource(ControlMode.class) // Runs the test class once for each ControlMode
@ExtendWith(WPILibExtension.class) // Ensures WPILib-related setup/teardown happens
public final class ParameterizedIntakeSubsystemTest {

  /**
   * Concrete implementation of {@link ParameterizedIntakeSubsystem} for testing.
   *
   * <p>Allows constructing a testable instance with a fake motor and parameters.
   */
  private static class ConcreteParameterizedIntakeSubsystem extends ParameterizedIntakeSubsystem {
    protected ConcreteParameterizedIntakeSubsystem(PIDMotor intakeMotor, Params params) {
      super(intakeMotor, params);
    }
  }

  /** Fake motor instance used to verify motor demands without real hardware. */
  final FakePIDMotor fakeMotor = mock(FakePIDMotor.class, Answers.CALLS_REAL_METHODS);

  /** Parameter object controlling control mode, intake, and outtake demands. */
  private final ParameterizedIntakeSubsystem.Params params;

  /**
   * Constructor called for each {@link ControlMode} when running parameterized tests.
   *
   * @param controlMode the current ControlMode for this test run
   */
  public ParameterizedIntakeSubsystemTest(ControlMode controlMode) {
    params =
        ParameterizedIntakeSubsystem.Params.builder()
            .setControlMode(controlMode) // set control mode for PIDMotor
            .setIntakeDemand(42) // demand used during intake
            .setOuttakeDemand(-3.1415) // demand used during outtake
            .build();
  }

  /**
   * Verifies that the motor is stopped upon initial subsystem creation.
   *
   * <p>Also ensures that the fake motor has not been interacted with.
   */
  @Test
  public void initialState() {
    try (var ignored = new ConcreteParameterizedIntakeSubsystem(fakeMotor, params)) {
      assertMotorIsStopped();
      verifyNoInteractions(fakeMotor); // ensures motor has not received any commands yet
    }
  }

  /**
   * Tests that running the intake item command correctly sets the motor demand.
   *
   * @param commandTester utility to run WPILib commands in a test environment
   */
  @Test
  public void intakeItem(CommandTester commandTester) {
    try (var intake = new ConcreteParameterizedIntakeSubsystem(fakeMotor, params)) {
      Command command = intake.intakeItemCommand();
      assertMotorIsStopped();

      commandTester.runUntilComplete(command); // executes command fully

      // Verify that the motor received the expected intake demand
      assertThat(fakeMotor.demand).isWithin(0.01).of(params.intakeDemand());
    }
  }

  /**
   * Verifies that the motor stops correctly after completing an intake command.
   *
   * @param commandTester utility to run WPILib commands in a test environment
   */
  @Test
  public void stopAfterIntakingItem(CommandTester commandTester) {
    try (var intake = new ConcreteParameterizedIntakeSubsystem(fakeMotor, params)) {
      Command command = intake.intakeItemCommand();
      commandTester.runUntilComplete(command); // run intake

      command = intake.stopMotorCommand();
      assertMotorIsRunning(); // motor should still be running before stop command

      commandTester.runUntilComplete(command);

      assertMotorIsStopped(); // motor should now be stopped
    }
  }

  /**
   * Tests that the outtake item command correctly sets the motor demand.
   *
   * @param commandTester utility to run WPILib commands in a test environment
   */
  @Test
  public void outtakeItem(CommandTester commandTester) {
    try (var intake = new ConcreteParameterizedIntakeSubsystem(fakeMotor, params)) {
      intake.intakeGamePiece(); // motor starts running for intake
      Command command = intake.outtakeItemCommand();
      assertMotorIsRunning(); // motor should still be running before outtake

      commandTester.runUntilComplete(command);

      assertThat(fakeMotor.demand).isWithin(0.01).of(params.outtakeDemand());
    }
  }

  /**
   * Verifies that the motor stops correctly after completing an outtake command.
   *
   * @param commandTester utility to run WPILib commands in a test environment
   */
  @Test
  public void stopAfterOuttakingItem(CommandTester commandTester) {
    try (var intake = new ConcreteParameterizedIntakeSubsystem(fakeMotor, params)) {
      intake.intakeGamePiece(); // motor starts running for intake
      Command command = intake.outtakeItemCommand();
      commandTester.runUntilComplete(command); // run outtake

      command = intake.stopMotorCommand();
      assertMotorIsRunning(); // motor should still be running before stop command

      commandTester.runUntilComplete(command);

      assertMotorIsStopped(); // verify motor stops after stop command
    }
  }

  /** Asserts that the fake motor is currently stopped (demand ~ 0). */
  private void assertMotorIsStopped() {
    assertThat(fakeMotor.demand).isWithin(0.01).of(0.0);
  }

  /** Asserts that the fake motor is currently running (demand != 0). */
  private void assertMotorIsRunning() {
    assertThat(fakeMotor.demand).isNotWithin(0.01).of(0.0);
  }
}
