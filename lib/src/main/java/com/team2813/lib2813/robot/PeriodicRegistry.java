package com.team2813.lib2813.robot;

import java.util.function.Consumer;

/**
 * Service for registering callbacks that need to run periodically.
 *
 * <p>>Subsystems can extend {@link com.team2813.lib2813.subsystems.ModularSubsystem} to get access
 * to an instance of {@link PeriodicRegistry}. This will ensure that time spent in the periodic
 * methods are associated with the subsystem.
 */
public interface PeriodicRegistry {

  /**
   * Add a callback to run for each iteration of the event loop.
   *
   * @param callback The callback to run.
   */
  void addPeriodic(Consumer<RobotState> callback);

  /**
   * Add a callback to run in simulation mode for each iteration of the event loop.
   *
   * @param callback The callback to run.
   */
  void addSimulationPeriodic(Consumer<RobotState> callback);
}
