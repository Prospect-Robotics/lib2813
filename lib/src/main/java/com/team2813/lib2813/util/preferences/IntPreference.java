package com.team2813.lib2813.util.preferences;

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
public interface IntPreference extends IntSupplier, Preference {

  /** Returns the value that should be provided if no value is stored in NetworkTables. */
  int defaultValue();

  /**
   * Returns the int from the preferences table. If the table does not have a value for the key for
   * this preference, then the value provided by {@link #defaultValue()} will be returned.
   */
  @Override
  default int getAsInt() {
    return get();
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
