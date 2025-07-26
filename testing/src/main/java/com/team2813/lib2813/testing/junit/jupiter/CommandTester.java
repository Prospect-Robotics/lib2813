package com.team2813.lib2813.testing.junit.jupiter;

import edu.wpi.first.wpilibj2.command.Command;

/**
 * Allows tests to run commands.
 *
 * <p>Tests can get an instance by using {@link WPILibExtension}.
 */
public interface CommandTester {

  /** Schedules the provided command and runs it until it completes. */
  void runUntilComplete(Command command);
}
