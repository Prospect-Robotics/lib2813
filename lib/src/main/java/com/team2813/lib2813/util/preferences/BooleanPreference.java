package com.team2813.lib2813.util.preferences;

import edu.wpi.first.wpilibj.Preferences;
import java.util.function.BooleanSupplier;

/**
 * Accessor for boolean values stored in {@link Preferences}.
 *
 * <p>Robots usually have on enum that implements this interface, using it to access all boolean
 * values stored in {@link Preferences}.
 */
public interface BooleanPreference extends BooleanSupplier, Preference {

  /** Returns the value that should be provided if no value is stored in NetworkTables. */
  default boolean defaultValue() {
    return false;
  }

  /**
   * Returns the boolean from the preferences table. If the table does not have a value for the key
   * for this preference, then the value provided by {@link #defaultValue()} will be returned.
   */
  @Override
  default boolean getAsBoolean() {
    return get();
  }

  /**
   * Returns the boolean from the preferences table. If the table does not have a value for the key
   * for this preference, then the value returned by {@link #defaultValue()} will be returned.
   */
  default boolean get() {
    var key = key();
    var defaultValue = defaultValue();
    if (!Preferences.containsKey(key)) {
      Preferences.initBoolean(key, defaultValue);
    }
    return Preferences.getBoolean(key, defaultValue);
  }

  /** Puts the given boolean into the preferences table. */
  default void set(boolean value) {
    Preferences.setBoolean(key(), value);
  }
}
