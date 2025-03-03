package com.team2813.lib2813.preferences;

import edu.wpi.first.wpilibj.Preferences;
import java.util.function.DoubleSupplier;

/**
 * Accessor for double values stored in {@link Preferences}.
 *
 * <p>Robots usually have on enum that implements this interface, using it to access all double
 * values stored in {@link Preferences}. Example use:
 *
 * <pre>
 * public enum DoublePref implements DoublePreference {
 *   MAX_CLIMB_VOLTAGE(0.8d),
 *   SUPER_PURSUIT_MODE_SPEED(2.1d);
 *
 *   DoublePref(double defaultValue) {
 *     this.defaultValue = defaultValue;
 *     initialize();
 *   }
 *
 *   private final double defaultValue;
 *
 *   &#064;Override
 *   public double defaultValue() {
 *     return defaultValue;
 *   }
 * }
 * </pre>
 */
public interface DoublePreference extends Preference {

  /** Returns the value that should be provided if no value is stored in NetworkTables. */
  double defaultValue();

  /** Returns a supplier that can be used to access this preference. */
  default DoubleSupplier asSupplier() {
    initialize();
    var key = key();

    return () -> Preferences.getDouble(key, 0);
  }

  /**
   * Returns the double at the given key. If this table does not have a value the key for this
   * preference, then the value provided by {@link #defaultValue()} will be returned.
   */
  default double get() {
    initialize();
    return Preferences.getDouble(key(), 0);
  }

  /** Puts the given double into the preferences table. */
  default void set(double value) {
    Preferences.setDouble(key(), value);
  }

  default void initialize() {
    var key = key();
    if (!Preferences.containsKey(key)) {
      Preferences.initDouble(key, defaultValue());
    }
  }
}
