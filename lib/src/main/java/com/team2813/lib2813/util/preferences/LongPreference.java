package com.team2813.lib2813.util.preferences;

import edu.wpi.first.wpilibj.Preferences;
import java.util.function.LongSupplier;

/**
 * Accessor for long values stored in {@link Preferences}.
 *
 * <p>Robots usually have on enum that implements this interface, using it to access all long *
 * values stored in {@link Preferences}.
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
    var key = key();
    var defaultValue = defaultValue();
    if (!Preferences.containsKey(key)) {
      Preferences.initLong(key, defaultValue);
    }
    return Preferences.getLong(key(), defaultValue);
  }

  /** Puts the given long into the preferences table. */
  default void set(long value) {
    Preferences.setLong(this.key(), value);
  }
}
