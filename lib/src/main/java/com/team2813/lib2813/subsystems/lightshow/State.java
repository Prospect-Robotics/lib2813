package com.team2813.lib2813.subsystems.lightshow;

import edu.wpi.first.wpilibj.util.Color;

/**
 * Represents a Lightshow state with a color and activation condition.
 *
 * <p>Each {@link State} provides a color and a condition indicating whether it should currently be
 * applied.
 *
 * @author Team 2813
 */
public interface State {

  /**
   * Gets the color of this state.
   *
   * @return the color to display for this state
   */
  Color color();

  /**
   * Checks if this state should currently be applied.
   *
   * @return {@code true} if the state should be active, {@code false} otherwise
   */
  boolean apply();
}
