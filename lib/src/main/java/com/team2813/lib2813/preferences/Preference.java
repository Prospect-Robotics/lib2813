package com.team2813.lib2813.preferences;

/**
 * A mixed-in interface for enum classes that are used to access {@link
 * edu.wpi.first.wpilibj.Preferences}.
 */
public interface Preference {

  /** Implemented via {@code Enum}. */
  String name();

  default String key() {
    return keyFactory().createKey(this, name());
  }

  default KeyFactory keyFactory() {
    return KeyFactory.DEFAULT_INSTANCE;
  }
}
