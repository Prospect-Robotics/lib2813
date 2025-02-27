package com.team2813.lib2813.util.preferences;

import edu.wpi.first.wpilibj.Preferences;
import java.util.function.DoubleSupplier;

/**
 * Accessor for double values stored in {@link Preferences}.
 *
 * <p>Instances of this class should be stored in static final values. You cannot create more than
 * one instance per enum value.
 *
 * @param <T> An enum used to identify keys.
 */
public final class DoublePreference<T extends Enum<T> & PreferenceKey> extends Preference<T>
    implements DoubleSupplier {
  private final double defaultValue;

  public DoublePreference(T key, double defaultValue) {
    super(key);
    this.defaultValue = defaultValue;
    Preferences.initDouble(this.key, defaultValue);
  }

  @Override
  public double getAsDouble() {
    return get();
  }

  public double get() {
    return Preferences.getDouble(this.key, defaultValue);
  }

  public void set(double value) {
    Preferences.setDouble(this.key, value);
  }
}
