package com.team2813.lib2813.util.preferences;

import edu.wpi.first.wpilibj.Preferences;
import java.util.function.BooleanSupplier;

/**
 * Accessor for boolean values stored in {@link Preferences}.
 *
 * <p>Robots usually have on enum that implements this interface, using it to access all boolean
 * values stored in {@link Preferences}.
 */
public interface BooleanPreference extends BooleanSupplier, PreferenceKey {

  default boolean defaultValue() {
    return false;
  }

  @Override
  default boolean getAsBoolean() {
    return get();
  }

  default boolean get() {
    var key = key();
    var defaultValue = defaultValue();
    if (!Preferences.containsKey(key)) {
      Preferences.initBoolean(key, defaultValue);
    }
    return Preferences.getBoolean(key, defaultValue);
  }

  default void set(boolean value) {
    Preferences.setBoolean(key(), value);
  }
}
