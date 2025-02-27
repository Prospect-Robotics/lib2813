package com.team2813.lib2813.util.preferences;

import edu.wpi.first.wpilibj.Preferences;
import java.util.function.IntSupplier;

/**
 * Accessor for int values stored in {@link Preferences}.
 *
 * <p>Robots usually have on enum that implements this interface, using it to access all int values
 * stored in {@link Preferences}.
 */
public interface IntPreference extends IntSupplier, PreferenceKey {

  int defaultValue();

  @Override
  default int getAsInt() {
    return get();
  }

  default int get() {
    var key = key();
    var defaultValue = defaultValue();
    if (!Preferences.containsKey(key)) {
      Preferences.initInt(key, defaultValue);
    }
    return Preferences.getInt(key, defaultValue);
  }

  default void set(int value) {
    Preferences.setInt(key(), value);
  }
}
