package com.team2813.lib2813.preferences;

import edu.wpi.first.wpilibj.Preferences;
import java.util.function.BooleanSupplier;

/**
 * Accessor for boolean values stored in {@link Preferences}.
 *
 * <p>Robots usually have on enum that implements this interface, using it to access all boolean
 * values stored in {@link Preferences}. Example use:
 *
 * <pre>
 * public enum BooleanPref implements BooleanPreference {
 *   ENABLE_LIMELIGHT(true),
 *   ENABLE_SUPER_PURSUIT_MODE(false);
 *
 *   BooleanPref(boolean defaultValue) {
 *     this.defaultValue = defaultValue;
 *     initialize();
 *   }
 *
 *   BooleanPref() {
 *     this(false);
 *   }
 *
 *   private final boolean defaultValue;
 *
 *   &#064;Override
 *   public boolean defaultValue() {
 *     return defaultValue;
 *   }
 * }
 * </pre>
 */
public interface BooleanPreference extends Preference {

  /** Returns the value that should be provided if no value is stored in NetworkTables. */
  default boolean defaultValue() {
    return false;
  }

  /** Returns a supplier that can be used to access this preference. */
  default BooleanSupplier asSupplier() {
    initialize();
    var key = key();

    return () -> Preferences.getBoolean(key, false);
  }

  /**
   * Returns the boolean from the preferences table. If the table does not have a value for the key
   * for this preference, then the value returned by {@link #defaultValue()} will be returned.
   */
  default boolean get() {
    initialize();
    return Preferences.getBoolean(key(), false);
  }

  /** Puts the given boolean into the preferences table. */
  default void set(boolean value) {
    Preferences.setBoolean(key(), value);
  }

  default void initialize() {
    var key = key();
    if (!Preferences.containsKey(key)) {
      Preferences.initBoolean(key, defaultValue());
    }
  }
}
