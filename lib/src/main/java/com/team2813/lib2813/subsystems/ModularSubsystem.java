package com.team2813.lib2813.subsystems;

import com.team2813.lib2813.robot.PeriodicRegistry;
import com.team2813.lib2813.robot.SimplePeriodicRegistry;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

/**
 * A base for subsystems that has APIs to support modular code.
 *
 * <p>Provides access to {@link PeriodicRegistry}, which can be passed to reusable components.
 */
public abstract class ModularSubsystem extends SubsystemBase {
  private final SimplePeriodicRegistry periodicRegistry = new SimplePeriodicRegistry();

  /** Constructor. Telemetry/log name defaults to the classname. */
  protected ModularSubsystem() {
    super();
  }

  /**
   * Constructor.
   *
   * @param name Name of the subsystem for telemetry and logging.
   */
  protected ModularSubsystem(String name) {
    super(name);
  }

  /**
   * Gets a periodic registry for registering functions to periodically be called by this subsystem.
   */
  protected final PeriodicRegistry getPeriodicRegistry() {
    return periodicRegistry;
  }

  /**
   * Called periodically by the {@link CommandScheduler}.
   *
   * <p>This is intentionally marked as final; subclasses should use {@link #getPeriodicRegistry()}.
   */
  @Override
  public final void periodic() {
    periodicRegistry.callPeriodicFunctions();
  }

  /**
   * Called periodically by the {@link CommandScheduler} when in simulation mode.
   *
   * <p>This is intentionally marked as final; subclasses should use {@link #getPeriodicRegistry()}.
   */
  @Override
  public final void simulationPeriodic() {
    periodicRegistry.callSimulationPeriodicFunctions();
  }
}
