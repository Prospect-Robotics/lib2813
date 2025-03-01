package com.team2813.lib2813.util.preferences;

import edu.wpi.first.wpilibj.Preferences;
import java.util.function.DoubleSupplier;

/**
 * Accessor for double values stored in {@link Preferences}.
 *
 * <p>Robots usually have on enum that implements this interface, using it to access all double
 * values stored in {@link Preferences}.
 */
public interface DoublePreference extends DoubleSupplier, Preference {

  /** Returns the value that should be provided if no value is stored in NetworkTables. */
  int defaultValue();

  @Override
  default double getAsDouble() {
    return get();
  }

  /**
   * Returns the double at the given key. If this table does not have a value the key for this
   * preference, then the value provided by {@link #defaultValue()} will be returned.
   */
  default double get() {
    var key = key();
    var defaultValue = defaultValue();
    if (!Preferences.containsKey(key)) {
      Preferences.initDouble(key, defaultValue);
    }
    return Preferences.getDouble(key, defaultValue);
  }

  /** Puts the given double into the preferences table. */
  default void set(double value) {
    Preferences.setDouble(key(), value);
  }
}
