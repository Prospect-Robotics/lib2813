package com.team2813.lib2813.testing.junit.jupiter;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotState;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Events;

/**
 * Unit tests for the {@link WPILibExtension} JUnit 5 extension.
 *
 * <p>This class ensures that WPILib-specific functionality (like {@link CommandScheduler} and
 * {@link DriverStation}) behaves correctly when used with the extension. It verifies that:
 *
 * <ul>
 *   <li>Commands are not scheduled unexpectedly before or after tests
 *   <li>{@link DriverStation} is properly enabled during tests
 *   <li>{@link CommandTester} can execute and verify commands
 * </ul>
 */
public class WPILibExtensionTest {

  /** A fake command used for scheduling tests. */
  static final Command FAKE_COMMAND = new Command() {};

  @BeforeAll
  static void initializeHal() {
    if (!HAL.initialize(500, 0)) {
      throw new IllegalStateException("Could not initialize Hardware Abstraction Layer");
    }
  }

  /**
   * Sample test class demonstrating usage of {@link WPILibExtension} with JUnit 5.
   *
   * <p>Uses {@link TestMethodOrder} to enforce ordering and {@link Tag} to mark it as special
   * testkit-only.
   */
  @ExtendWith(WPILibExtension.class)
  @Tag("ignore-outside-testkit")
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  public static class SampleTest {

    @BeforeAll
    public static void verifyFakeCommandNotScheduledBeforeAll() {
      CommandScheduler commandScheduler = CommandScheduler.getInstance();
      assertWithMessage("Expect all commands to have been cancelled")
          .that(commandScheduler.isScheduled(FAKE_COMMAND))
          .isFalse();
    }

    @BeforeAll
    public static void verifyDriverStationEnabled() {
      assertThat(DriverStation.isEnabled()).isTrue();
    }

    /** Verifies that FAKE_COMMAND is not scheduled before the first test, then schedules it. */
    @Test
    @Order(1)
    public void verifyFakeCommandNotScheduledBeforeTest() {
      CommandScheduler commandScheduler = CommandScheduler.getInstance();
      assertWithMessage("Expect all commands to have been cancelled")
          .that(commandScheduler.isScheduled(FAKE_COMMAND))
          .isFalse();

      commandScheduler.schedule(FAKE_COMMAND);
      assertThat(commandScheduler.isScheduled(FAKE_COMMAND));
    }

    /** Verifies that FAKE_COMMAND is not scheduled before the second test, then schedules it. */
    @Test
    @Order(2)
    public void verifyFakeCommandNotScheduledAfterTest(CommandTester commandTester) {
      CommandScheduler commandScheduler = CommandScheduler.getInstance();
      assertWithMessage("Expect all commands to have been cancelled")
          .that(commandScheduler.isScheduled(FAKE_COMMAND))
          .isFalse();

      commandScheduler.schedule(FAKE_COMMAND);
      assertThat(commandScheduler.isScheduled(FAKE_COMMAND));
    }

    /** Verifies that {@link CommandTester} runs and correctly verifies a command. */
    @Test
    @Order(3)
    public void verifyCommandTester(CommandTester commandTester) {
      VerifiableCommand command = new VerifiableCommand();
      commandTester.runUntilComplete(command);
      command.verify();
    }

    @AfterAll
    public static void verifyFakeCommandNotScheduledAfterAllTests() {
      CommandScheduler commandScheduler = CommandScheduler.getInstance();
      assertWithMessage("Expect all commands to have been cancelled")
          .that(commandScheduler.isScheduled(FAKE_COMMAND))
          .isFalse();
    }
  } // end SampleTest

  /** Verifies that {@link WPILibExtension} executes SampleTest correctly and without failures. */
  @Test
  void verifyExtension() {
    withDriverStationTemporarilyEnabled(
        () -> {
          CommandScheduler commandScheduler = CommandScheduler.getInstance();
          commandScheduler.enable();
          commandScheduler.schedule(FAKE_COMMAND);
          boolean isScheduled = commandScheduler.isScheduled(FAKE_COMMAND);
          commandScheduler.disable();
          assertThat(isScheduled).isTrue();
        });

    EngineExecutionResults results =
        EngineTestKit.engine("junit-jupiter").selectors(selectClass(SampleTest.class)).execute();

    assertHasNoFailures(results);
  }

  /**
   * Temporarily enables the driver station for the duration of {@code runnable}, restoring its
   * previous state afterwards.
   */
  private void withDriverStationTemporarilyEnabled(Runnable runnable) {
    assertThat(RobotState.isDisabled()).isTrue();
    DriverStationSim.setEnabled(true);
    DriverStationSim.notifyNewData();
    assertThat(RobotState.isDisabled()).isFalse();

    try {
      runnable.run();
    } finally {
      DriverStationSim.setEnabled(false);
      DriverStationSim.notifyNewData();
    }
  }

  private void assertHasNoFailures(EngineExecutionResults results) {
    assertHasNoFailures(results.containerEvents());
    assertHasNoFailures(results.testEvents());
  }

  private void assertHasNoFailures(Events events) {
    events.assertStatistics(
        stats -> {
          stats.skipped(0);
          stats.failed(0);
        });
  }

  /**
   * A test command used for verifying {@link CommandTester}.
   *
   * <p>Tracks initialize() and execute() calls, and allows validation of expected behavior.
   */
  private static class VerifiableCommand extends Command {
    private static final int EXPECTED_EXECUTION_COUNT = 4;
    private int initializedCount = 0;
    private int executionCount = 0;

    /** Verifies that initialize and execute were called as expected. */
    void verify() {
      assertWithMessage("initialize() should be called").that(initializedCount).isGreaterThan(0);
      assertWithMessage("initialize() should not be called more than once")
          .that(initializedCount)
          .isLessThan(2);
      assertWithMessage("execute() should be called until isFinished() returns false")
          .that(executionCount)
          .isEqualTo(EXPECTED_EXECUTION_COUNT);
    }

    @Override
    public void initialize() {
      initializedCount++;
    }

    @Override
    public void execute() {
      executionCount++;
    }

    @Override
    public boolean isFinished() {
      return executionCount >= EXPECTED_EXECUTION_COUNT;
    }
  }
}
