package com.team2813.lib2813.preferences;

import edu.wpi.first.wpilibj.Preferences;
import java.util.function.IntSupplier;

/**
 * Accessor for int values stored in {@link Preferences}.
 *
 * <p>Robots usually have on enum that implements this interface, using it to access all int values
 * stored in {@link Preferences}. Example use:
 *
 * <pre>
 * public enum IntPref implements IntPreference {
 *   NUM_CHICKENS(2);
 *
 *   IntPref(int defaultValue) {
 *     this.defaultValue = defaultValue;
 *     initialize();
 *   }
 *
 *   private final int defaultValue;
 *
 *   &#064;Override
 *   public int defaultValue() {
 *     return defaultValue;
 *   }
 * }
 * </pre>
 */
public interface IntPreference extends Preference {

  /** Returns the value that should be provided if no value is stored in NetworkTables. */
  int defaultValue();

  /** Returns a supplier that can be used to access this preference. */
  default IntSupplier asSupplier() {
    initialize();
    var key = key();

    return () -> Preferences.getInt(key, 0);
  }

  /**
   * Returns the int from the preferences table. If the table does not have a value for the key for
   * this preference, then the value provided by {@link #defaultValue()} will be returned.
   */
  default int get() {
    initialize();
    return Preferences.getInt(key(), 0);
  }

  /** Puts the given int into the preferences table. */
  default void set(int value) {
    Preferences.setInt(key(), value);
  }

  default void initialize() {
    var key = key();
    if (!Preferences.containsKey(key)) {
      Preferences.initInt(key, defaultValue());
    }
  }
}
