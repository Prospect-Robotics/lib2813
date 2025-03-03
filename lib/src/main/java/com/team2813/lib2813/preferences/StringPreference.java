package com.team2813.lib2813.preferences;

import edu.wpi.first.wpilibj.Preferences;
import java.util.function.Supplier;

/**
 * Accessor for string values stored in {@link Preferences}.
 *
 * <p>Robots usually have on enum that implements this interface, using it to access all String
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
public interface StringPreference extends Preference {

  /** Returns the value that should be provided if no value is stored in NetworkTables. */
  String defaultValue();

  /** Returns a supplier that can be used to access this preference. */
  default Supplier<String> asSupplier() {
    initialize();
    var key = key();

    return () -> Preferences.getString(key, "");
  }

  /**
   * Returns the String from the preferences table. If the table does not have a value for the key
   * for this preference, then the value returned by {@link #defaultValue()} will be returned.
   */
  default String get() {
    initialize();
    return Preferences.getString(key(), "");
  }

  /** Puts the given long into the preferences table. */
  default void set(String value) {
    Preferences.setString(this.key(), value);
  }

  default void initialize() {
    var key = key();
    if (!Preferences.containsKey(key)) {
      Preferences.initString(key, defaultValue());
    }
  }
}
