package com.team2813.lib2813.testing.junit.jupiter;

import edu.wpi.first.wpilibj2.command.Command;

/**
 * Utility interface for running WPILib {@link Command} objects inside tests.
 *
 * <p>Provides a way to schedule and execute commands to completion within a test context.
 * An implementation is provided via {@link WPILibExtension}.
 */
public interface CommandTester {

  /**
   * Schedules the provided {@link Command} and repeatedly runs it
   * until the command reports it has finished.
   *
   * @param command the command to schedule and execute
   */
  void runUntilComplete(Command command);
}
