package com.team2813.lib2813.util.preferences;

/**
 * A mixed-in interface for enum classes that are used to access {@link
 * edu.wpi.first.wpilibj.Preferences}.
 */
interface Preference {

  /** Implemented via {@code Enum}. */
  String name();

  default String key() {
    String prefix = getClass().getCanonicalName();
    if (prefix.startsWith("com.team2813.")) {
      prefix = prefix.substring(13);
    }
    return prefix + "." + name();
  }
}
