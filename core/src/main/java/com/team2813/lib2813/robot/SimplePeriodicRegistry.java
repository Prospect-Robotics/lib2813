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
package com.team2813.lib2813.robot;

import edu.wpi.first.wpilibj.DriverStation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * {@link PeriodicRegistry} implementation that gets the state from {@link DriverStation}.
 *
 * <p>Most code should not directly use this class. Subsystems can extend {@link
 * com.team2813.lib2813.subsystems.ModularSubsystem} to get access to an instance of {@link
 * PeriodicRegistry}.
 *
 * <p>Alternatively, the main robot code can construct this class, as long as it calls {@link
 * #callPeriodicFunctions()} in {@code robotPeriodic()} and {@link
 * #callSimulationPeriodicFunctions()} in {@code simulationPeriodic()}.
 */
public final class SimplePeriodicRegistry implements PeriodicRegistry {
  private final List<Consumer<RobotState>> periodicFunctions = new ArrayList<>();
  private final List<Consumer<RobotState>> simulationPeriodicFunctions = new ArrayList<>();

  @Override
  public void addPeriodic(Consumer<RobotState> callback) {
    periodicFunctions.add(callback);
  }

  @Override
  public void addSimulationPeriodic(Consumer<RobotState> callback) {
    simulationPeriodicFunctions.add(callback);
  }

  /** Calls all the callbacks added via {@link #addPeriodic(Consumer)}. */
  public void callPeriodicFunctions() {
    RobotState robotState = SimpleRobotState.getInstance();
    periodicFunctions.forEach(fun -> fun.accept(robotState));
  }

  /** Calls all the callbacks added via {@link #addSimulationPeriodic(Consumer)}. */
  public void callSimulationPeriodicFunctions() {
    RobotState robotState = SimpleRobotState.getInstance();
    simulationPeriodicFunctions.forEach(fun -> fun.accept(robotState));
  }

  private static class SimpleRobotState implements RobotState {
    private static final SimpleRobotState INSTANCE = new SimpleRobotState();

    static RobotState getInstance() {
      return INSTANCE;
    }

    @Override
    public boolean isEnabled() {
      return DriverStation.isEnabled();
    }

    @Override
    public boolean isAutonomous() {
      return DriverStation.isAutonomous();
    }

    @Override
    public boolean isTest() {
      return DriverStation.isTest();
    }

    @Override
    public boolean isTeleop() {
      return DriverStation.isTeleop();
    }
  }
}
