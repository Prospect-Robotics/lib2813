/*
Copyright 2025-2026 Prospect Robotics SWENext Club

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
package com.team2813.lib2813.testing.junit.jupiter;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static com.team2813.lib2813.testing.junit.jupiter.ExtensionAssertions.assertHasNoFailures;
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

/** Tests for {@link InitWPILibExtension}. */
public class InitWPILibExtensionTest {

  static final Command FAKE_COMMAND = new Command() {};

  @BeforeAll
  static void initializeHal() {
    if (!HAL.initialize(500, 0)) {
      throw new IllegalStateException("Could not initialize Hardware Abstraction Layer");
    }
  }

  @ExtendWith(InitWPILibExtension.class)
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

  @Test
  void verifyExtension() {
    // Arrange
    withDriverStationTemporarilyEnabled(
        () -> {
          // Schedule FAKE_COMMAND
          CommandScheduler commandScheduler = CommandScheduler.getInstance();
          commandScheduler.enable();
          commandScheduler.schedule(FAKE_COMMAND);
          boolean isScheduled = commandScheduler.isScheduled(FAKE_COMMAND);
          commandScheduler.disable();
          assertThat(isScheduled).isTrue();
        });

    // Act
    EngineExecutionResults results =
        EngineTestKit.engine("junit-jupiter").selectors(selectClass(SampleTest.class)).execute();

    // Assert
    assertHasNoFailures(results);
  }

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

  private static class VerifiableCommand extends Command {
    private static final int EXPECTED_EXECUTION_COUNT = 4;
    private int initializedCount = 0;
    private int executionCount = 0;

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
