package com.team2813.lib2813.util.preferences;

import edu.wpi.first.wpilibj.Preferences;
import java.util.function.Supplier;

/**
 * Accessor for string values stored in {@link Preferences}.
 *
 * <p>Robots usually have on enum that implements this Stringerface, using it to access all String
 * values stored in {@link Preferences}. Example use:
 *
 * <pre>
 * public enum StringPref implements StringPreference {
 *   NUM_CHICKENS(2);
 *
 *   StringPref(String defaultValue) {
 *     this.defaultValue = defaultValue;
 *     initialize();
 *   }
 *
 *   private final String defaultValue;
 *
 *   &#064;Override
 *   public String defaultValue() {
 *     return defaultValue;
 *   }
 * }
 * </pre>
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
    initialize();
    return Preferences.getString(key(), "");
  }

  default void initialize() {
    var key = key();
    if (!Preferences.containsKey(key)) {
      Preferences.initString(key, defaultValue());
    }
  }
}
