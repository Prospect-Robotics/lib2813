package com.team2813.lib2813.util.preferences;

import static com.team2813.lib2813.util.preferences.PreferenceUtil.fullKey;

import edu.wpi.first.wpilibj.Preferences;

/**
 * Base class for type-safe {@link Preferences} accessors.
 *
 * @param <T> An enum used to identify keys.
 */
abstract class Preference<T extends Enum<T> & PreferenceKey> {
  private static final String REMOVE_PREFIX = "com.team2813.";
  private static final int REMOVE_PREFIX_LEN = REMOVE_PREFIX.length();
  public final String key;

  protected Preference(T key) {
    this.key = fullKey(key);
    if (Preferences.containsKey(this.key)) {
      throw new IllegalArgumentException(
          String.format("Already a Preference instance created for key=%s", this.key));
    }
  }
}
