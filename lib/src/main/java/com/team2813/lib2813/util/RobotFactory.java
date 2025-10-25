package com.team2813.lib2813.util;

import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import java.util.function.Function;

/**
 * Utility class for constructing and starting robot instances with additional input objects.
 *
 * <p>This factory allows you to provide extra inputs (such as Shuffleboard tabs) to the robot
 * constructor while still using the standard {@link RobotBase#startRobot} entry point.
 *
 * <p>The class is non-instantiable and all functionality is static.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * RobotFactory.startRobot(inputs -> new MyRobot(inputs.shuffleboard.getTab("Drive")));
 * }</pre>
 *
 * <p>Here, the factory lambda receives an {@link Inputs} object which can provide access to
 * Shuffleboard tabs or other injected dependencies.
 *
 * @author Team 2813
 */
public final class RobotFactory {

  /** Holder for external inputs to be injected into the robot constructor. */
  public record Inputs(ShuffleboardTabs shuffleboard) {}

  /**
   * Starts the robot with a factory function that receives the {@link Inputs}.
   *
   * <p>The factory function should construct and return an instance of a subclass of {@link
   * RobotBase}, e.g., your main robot class.
   *
   * @param factory a function that receives {@link Inputs} and returns a {@link RobotBase} instance
   * @param <T> the type of RobotBase subclass
   */
  public static <T extends RobotBase> void startRobot(Function<Inputs, T> factory) {
    var inputs = new Inputs(new RealShuffleboardTabs());
    RobotBase.startRobot(() -> factory.apply(inputs));
  }

  /** Prevent instantiation of this utility class. */
  private RobotFactory() {
    throw new AssertionError("Not instantiable!");
  }

  /** Real implementation of {@link ShuffleboardTabs} that delegates to WPILib Shuffleboard. */
  private static class RealShuffleboardTabs implements ShuffleboardTabs {
    @Override
    public ShuffleboardTab getTab(String title) {
      return Shuffleboard.getTab(title);
    }

    @Override
    public void selectTab(String title) {
      Shuffleboard.selectTab(title);
    }
  }
}
