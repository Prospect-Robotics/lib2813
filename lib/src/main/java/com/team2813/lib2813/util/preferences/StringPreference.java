package com.team2813.lib2813.util.preferences;

import edu.wpi.first.wpilibj.Preferences;
import java.util.function.Supplier;

/**
 * Accessor for string values stored in {@link Preferences}.
 *
 * <p>Robots usually have on enum that implements this interface, using it to access all String
 * values stored in {@link Preferences}.
 */
public interface StringPreference extends Supplier<String>, Preference {

  /** Returns the value that should be provided if no value is stored in NetworkTables. */
  String defaultValue();

  /**
   * Returns the String from the preferences table. If the table does not have a value for the key
   * for this preference, then the value returned by {@link #defaultValue()} will be returned.
   */
  @Override
  default String get() {
    var key = key();
    var defaultValue = defaultValue();
    if (!Preferences.containsKey(key)) {
      Preferences.initString(key, defaultValue);
    }
    return Preferences.getString(key, defaultValue);
  }
}
