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
package com.team2813.lib2813.testing.junit.jupiter;

import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Time;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import org.junit.jupiter.api.Test;

@InitWPILib(periodicPeriod = CommandsTesterTest.PERIOD_SECS)
public final class CommandsTesterTest {
  static final double PERIOD_SECS =
      TimedRobot.kDefaultPeriod * 2; // a value that isn't the default period
  static final Time ONE_CYCLE = Units.Seconds.of(PERIOD_SECS);

  @Test
  public void withTimeout_completesBeforeTimeout_testCompletes(CommandsTester tester) {
    var timeout = Units.Seconds.of(10);
    FakeCommand command = FakeCommand.withRuntime(timeout.minus(ONE_CYCLE));

    tester.withTimeout(timeout).runUntilComplete(command);

    assertWithMessage("Command should run to completion").that(command.ranUntilCompletion).isTrue();
  }

  @Test
  public void withTimeout_completesExactlyAtTimeout_testCompletes(CommandsTester tester) {
    var timeout = Units.Seconds.of(10);
    FakeCommand command = FakeCommand.withRuntime(timeout);

    tester.withTimeout(timeout).runUntilComplete(command);

    assertWithMessage("Command should run to completion").that(command.ranUntilCompletion).isTrue();
  }

  @Test
  public void withTimeout_completesOneCycleAfterTimeout_throwsAssertionError(
      CommandsTester tester) {
    var timeout = Units.Seconds.of(10);
    FakeCommand command = FakeCommand.withRuntime(timeout.plus(ONE_CYCLE));

    // The command would run to completion, but the test will fail because the timeout completed at
    // the same time.
    assertThrows(AssertionError.class, () -> tester.withTimeout(timeout).runUntilComplete(command));
  }

  @Test
  public void withTimeout_notCompleteOneCycleAfterTimeout_throwsAssertionError(
      CommandsTester tester) {
    var timeout = Units.Seconds.of(10);
    Time commandRunTime = timeout.plus(ONE_CYCLE.times(2));
    FakeCommand command = new FakeCommand(commandRunTime);

    // The command would not run to completion, and the test will fail.
    assertThrows(AssertionError.class, () -> tester.withTimeout(timeout).runUntilComplete(command));
    assertWithMessage("Command should not run to completion")
        .that(command.ranUntilCompletion)
        .isFalse();
  }

  static class FakeCommand extends WaitCommand {
    boolean ranUntilCompletion;

    static FakeCommand withRuntime(Time runTime) {
      return new FakeCommand(runTime);
    }

    private FakeCommand(Time runTime) {
      super(runTime);
    }

    @Override
    public void execute() {
      super.execute();
      ranUntilCompletion = isFinished();
    }
  }
}
