package com.team2813.lib2813.robot;

/** Provides APIs for getting the current state of the robot. */
public interface RobotState {

  /**
   * Determines if the Robot is currently enabled.
   *
   * @return {@code True} if the Robot is currently enabled by the Driver Station.
   */
  boolean isEnabled();

  /**
   * Determines if the Robot is currently disabled.
   *
   * @return {@code True} if the Robot is currently disabled by the Driver Station.
   */
  default boolean isDisabled() {
    return !isEnabled();
  }

  /**
   * Determines if the robot is currently in Autonomous mode as determined by the Driver Station.
   *
   * @return {@code True} if the robot is currently operating autonomously.
   */
  boolean isAutonomous();

  /**
   * Determine sif the robot is currently in Test mode as determined by the Driver Station.
   *
   * @return {@code True} if the robot is currently operating in Test mode.
   */
  boolean isTest();

  /**
   * Determine if the robot is currently in Operator Control mode as determined by the Driver
   * Station.
   *
   * @return {@code True} if the robot is currently operating in Operator Control mode.
   */
  boolean isTeleop();
}
