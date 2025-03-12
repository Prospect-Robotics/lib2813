package com.team2813.lib2813.util;

import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import java.util.function.Function;

public final class RobotFactory {

  public record Inputs(ShuffleboardTabs shuffleboard) {}

  public static <T extends RobotBase> void startRobot(Function<Inputs, T> factory) {
    var inputs = new Inputs(new RealShuffleboardTabs());
    RobotBase.startRobot(() -> factory.apply(inputs));
  }

  private RobotFactory() {
    throw new AssertionError("Not instantiable!");
  }

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
