package com.team2813.lib2813.util.preferences;

import edu.wpi.first.wpilibj.Preferences;
import java.util.function.LongSupplier;

/**
 * Accessor for long values stored in {@link Preferences}.
 *
 * <p>Robots usually have on enum that implements this interface, using it to access all long *
 * values stored in {@link Preferences}. Example use:
 *
 * <pre>
 * public enum LongPref implements LongPreference {
 *   MAX_CLIMB_VOLTAGE(0.8d),
 *   SUPER_PURSUIT_MODE_SPEED(2.1d);
 *
 *   LongPref(long defaultValue) {
 *     this.defaultValue = defaultValue;
 *     initialize();
 *   }
 *
 *   private final long defaultValue;
 *
 *   &#064;Override
 *   public long defaultValue() {
 *     return defaultValue;
 *   }
 * }
 * </pre>
 */
public interface LongPreference extends LongSupplier, Preference {

  /** Returns the value that should be provided if no value is stored in NetworkTables. */
  long defaultValue();

  /**
   * Returns the long from the preferences table. If the table does not have a value for the key for
   * this preference, then the value provided by {@link #defaultValue()} will be returned.
   */
  @Override
  default long getAsLong() {
    return get();
  }

  /**
   * Returns the long from the preferences table. If the table does not have a value for the key for
   * this preference, then the value provided by {@link #defaultValue()} will be returned.
   */
  default long get() {
    initialize();
    return Preferences.getLong(key(), 0);
  }

  /** Puts the given long into the preferences table. */
  default void set(long value) {
    Preferences.setLong(this.key(), value);
  }

  default void initialize() {
    var key = key();
    if (!Preferences.containsKey(key)) {
      Preferences.initLong(key, defaultValue());
    }
  }
}
