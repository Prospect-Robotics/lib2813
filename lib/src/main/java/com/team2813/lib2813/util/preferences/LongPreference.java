package com.team2813.lib2813.util.preferences;

import edu.wpi.first.wpilibj.Preferences;
import java.util.function.LongSupplier;

/**
 * Accessor for long values stored in {@link Preferences}.
 *
 * <p>Instances of this class should be stored in static final values. You cannot create more than
 * one instance per enum value.
 *
 * @param <T> An enum used to identify keys.
 */
public final class LongPreference<T extends Enum<T> & PreferenceKey> extends Preference<T>
    implements LongSupplier {
  private final long defaultValue;

  public LongPreference(T key, long defaultValue) {
    super(key);
    this.defaultValue = defaultValue;
    if (!Preferences.containsKey(this.key)) {
      Preferences.initLong(this.key, defaultValue);
    }
  }

  @Override
  public long getAsLong() {
    return get();
  }

  public long get() {
    return Preferences.getLong(this.key, defaultValue);
  }

  public void set(long value) {
    Preferences.setLong(this.key, value);
  }
}
