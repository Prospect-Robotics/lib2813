package com.team2813.lib2813.util.preferences;

import edu.wpi.first.wpilibj.Preferences;
import java.util.function.Supplier;

/**
 * Accessor for string values stored in {@link Preferences}.
 *
 * <p>Instances of this class should be stored in static final values. You cannot create more than
 * one instance per enum value.
 *
 * @param <T> An enum used to identify keys.
 */
public final class StringPreference<T extends Enum<T> & PreferenceKey> extends Preference<T>
    implements Supplier<String> {
  private final String defaultValue;

  public StringPreference(T key, String defaultValue) {
    super(key);
    this.defaultValue = defaultValue;
    Preferences.initString(this.key, defaultValue);
  }

  @Override
  public String get() {
    return Preferences.getString(key, defaultValue);
  }
}
