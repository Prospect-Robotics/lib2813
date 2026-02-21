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

import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Time;
import edu.wpi.first.wpilibj.simulation.SimHooks;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

/**
 * Allows tests to run commands.
 *
 * <p>Tests can get an instance by using {@link InitWPILib}.
 *
 * @since 2.1.0
 */
public final class CommandsTester implements CommandTester {
  private final double periodicPeriod;

  CommandsTester(InitWPILib annotation) {
    this(annotation.periodicPeriod());
  }

  CommandsTester(double periodicPeriod) {
    this.periodicPeriod = periodicPeriod;
  }

  /**
   * Returns a command tester that runs all commands with the given timeout.
   *
   * <p>The returned {@link CommandTester} will throw an {@code AssertionError} from {@link
   * #runUntilComplete(Command)} if the timeout is reached before the command finishes.
   *
   * @param timeout time given for each command to run
   */
  public CommandTester withTimeout(Time timeout) {
    Time adjustedTimeout = timeout.plus(Units.Seconds.of(periodicPeriod));
    return command -> {
      var timeoutCommand = new TimeoutCommand(adjustedTimeout, command);
      runUntilComplete(command.raceWith(timeoutCommand));
      timeoutCommand.assertTimeoutNotReached();
    };
  }

  @Override
  public void runUntilComplete(Command command) {
    SimHooks.pauseTiming();
    try {
      CommandScheduler scheduler = CommandScheduler.getInstance();
      scheduler.schedule(command);
      scheduler.run();
      while (scheduler.isScheduled(command)) {
        SimHooks.stepTiming(periodicPeriod);
        scheduler.run();
      }
    } finally {
      SimHooks.resumeTiming();
    }
  }
}
