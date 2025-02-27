package com.team2813.lib2813.util.preferences;

import static com.team2813.lib2813.util.preferences.PreferenceUtil.fullKey;

/**
 * A mixed-in interface for enum classes used for subclasses of {@link Preference}.
 *
 * <p>Robots usually have a single enum class that implements this interface, and use it for all
 * access to data in {@link edu.wpi.first.wpilibj.Preferences}.
 */
public interface PreferenceKey {

  /** Implemented via {@code Enum}. */
  String name();

  default String key() {
    return fullKey(this);
  }
}
