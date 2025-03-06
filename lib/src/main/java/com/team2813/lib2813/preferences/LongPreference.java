package com.team2813.lib2813.preferences;

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
 *   MAX_SHEEP_COUNT(2546167841),
 *   LOG_MESSAGES_TO_KEEP(28132321690);
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
public interface LongPreference extends Preference {

  /** Returns the value that should be provided if no value is stored in NetworkTables. */
  long defaultValue();

  /** Returns a supplier that can be used to access this preference. */
  default LongSupplier asSupplier() {
    initialize();
    var key = key();

    return () -> Preferences.getLong(key, 0);
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
