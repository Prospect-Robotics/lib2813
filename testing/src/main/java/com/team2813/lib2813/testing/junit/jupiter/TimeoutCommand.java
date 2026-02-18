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

import static org.junit.jupiter.api.Assertions.assertFalse;

import edu.wpi.first.units.measure.Time;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.WaitCommand;

/** Variant of {@link WaitCommand} that remembers if the timeout occurred. */
class TimeoutCommand extends WaitCommand {
  private final Command wrappedCommand;
  private boolean timeoutReached;

  TimeoutCommand(Time timeout, Command wrappedCommand) {
    super(timeout);
    this.wrappedCommand = wrappedCommand;
  }

  void assertTimeoutNotReached() {
    assertFalse(
        timeoutReached,
        () -> String.format("Command '%s' timed out before completion", wrappedCommand.getName()));
  }

  @Override
  public void end(boolean interrupted) {
    timeoutReached = isFinished();
    super.end(interrupted);
  }
}
