package com.team2813.lib2813.testing.junit.jupiter;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * JUnit Jupiter extension for testing robot code that depends on WPILib.
 *
 * <p>This extension:
 * <ul>
 *   <li>Initializes the WPILib {@link HAL} and driver station simulation before tests run.</li>
 *   <li>Configures the {@link CommandScheduler} to ensure a clean environment.</li>
 *   <li>Provides a {@link CommandTester} parameter for tests, which allows running
 *       WPILib commands until completion.</li>
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @ExtendWith(WPILibExtension.class)
 * public final class FlightSubsystemTest {
 *
 *   @Test
 *   public void initiallyNotInAir() {
 *     var flight = new FlightSubsystem();
 *     assertThat(flight.inAir()).isFalse();
 *   }
 *
 *   @Test
 *   public void takesFlight(CommandTester commandTester) {
 *     var flight = new FlightSubsystem();
 *     Command takeOff = flight.createTakeOffCommandCommand();
 *
 *     // Run the command under test
 *     commandTester.runUntilComplete(takeOff);
 *
 *     assertThat(flight.inAir()).isTrue();
 *   }
 * }
 * }</pre>
 */
public final class WPILibExtension
    implements Extension,
        AfterAllCallback,
        AfterEachCallback,
        BeforeAllCallback,
        ParameterResolver {

  /**
   * Initializes WPILib components before all tests.
   *
   * <p>Sets up the HAL, enables the driver station simulation,
   * and resets the {@link CommandScheduler}.
   *
   * @param context the JUnit extension context
   * @throws IllegalStateException if the HAL cannot be initialized
   */
  @Override
  public void beforeAll(ExtensionContext context) {
    // See https://www.chiefdelphi.com/t/driverstation-getalliance-in-gradle-test/
    if (!HAL.initialize(500, 0)) {
      throw new IllegalStateException("Could not initialize Hardware Abstraction Layer");
    }
    DriverStationSim.setEnabled(true);
    DriverStationSim.notifyNewData();
    CommandScheduler.getInstance().enable();
    CommandScheduler.getInstance().cancelAll();
    CommandScheduler.getInstance().unregisterAllSubsystems();
  }

  /**
   * Cleans up after each test by canceling all commands
   * and unregistering all subsystems.
   *
   * @param context the JUnit extension context
   */
  @Override
  public void afterEach(ExtensionContext context) {
    CommandScheduler.getInstance().cancelAll();
    CommandScheduler.getInstance().unregisterAllSubsystems();
  }

  /**
   * Cleans up after all tests by canceling all commands,
   * unregistering all subsystems, disabling the scheduler,
   * and resetting the driver station simulation.
   *
   * @param context the JUnit extension context
   */
  @Override
  public void afterAll(ExtensionContext context) {
    CommandScheduler.getInstance().cancelAll();
    CommandScheduler.getInstance().unregisterAllSubsystems();
    CommandScheduler.getInstance().disable();
    DriverStationSim.setEnabled(false);
    DriverStationSim.notifyNewData();
  }

  /**
   * Checks whether this extension can provide a parameter of type {@link CommandTester}.
   *
   * @param parameterContext the parameter context
   * @param extensionContext the extension context
   * @return true if the parameter type is {@code CommandTester}
   * @throws ParameterResolutionException if parameter resolution fails
   */
  @Override
  public boolean supportsParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return CommandTester.class.equals(parameterContext.getParameter().getType());
  }

  /**
   * Provides a {@link CommandTester} instance for parameter injection.
   *
   * <p>The tester schedules the given command and repeatedly runs
   * the {@link CommandScheduler} until the command completes.
   *
   * @param parameterContext the parameter context
   * @param extensionContext the extension context
   * @return a {@link CommandTester} that executes commands to completion
   */
  @Override
  public CommandTester resolveParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext) {
    return command -> {
      CommandScheduler scheduler = CommandScheduler.getInstance();
      command.schedule();
      do {
        scheduler.run();
      } while (scheduler.isScheduled(command));
    };
  }
}
