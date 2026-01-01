package com.team2813.lib2813.control.motors;

/** Functional interface for clamping a value between low and high boundaries. */
@FunctionalInterface
public interface Clamper {

  /**
   * Returns value clamped between low and high boundaries.
   *
   * @param value the value to clamp.
   * @return the clamped value.
   */
  double clampValue(double value);
}
