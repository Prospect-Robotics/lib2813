package com.team2813.lib2813.util.preferences;

import static com.google.common.truth.Truth.assertThat;

import edu.wpi.first.wpilibj.Preferences;
import org.junit.After;
import org.junit.Test;

public final class LongPreferenceTest {

  private enum LongPref implements LongPreference {
    HAS_DEFAULT_42(42);

    LongPref(long defaultValue) {
      this.defaultValue = defaultValue;
      initialize();
    }

    private final long defaultValue;

    @Override
    public long defaultValue() {
      return defaultValue;
    }
  }

  @After
  public void removePreferences() {
    for (var preference : LongPreferenceTest.LongPref.values()) {
      Preferences.remove(preference.key());
    }
  }

  @Test
  public void getValue() {
    // Act
    long value = LongPref.HAS_DEFAULT_42.getAsLong();

    // Assert
    assertThat(value).isEqualTo(42);

    // Act
    value = LongPref.HAS_DEFAULT_42.get();

    // Assert
    assertThat(value).isEqualTo(42);

    // Act
    value = Preferences.getLong(LongPref.HAS_DEFAULT_42.key(), 123);

    // Assert
    assertThat(value).isEqualTo(42);
    assertThat(LongPref.HAS_DEFAULT_42.key())
        .isEqualTo("lib2813.util.preferences.LongPreferenceTest.LongPref.HAS_DEFAULT_42");
  }

  @Test
  public void setValue() {
    // Act
    var newValue = LongPref.HAS_DEFAULT_42.defaultValue() + 100;
    LongPref.HAS_DEFAULT_42.set(newValue);

    // Assert
    assertThat(LongPref.HAS_DEFAULT_42.getAsLong()).isEqualTo(newValue);
    var value = Preferences.getLong(LongPref.HAS_DEFAULT_42.key(), newValue + 10);
    assertThat(value).isEqualTo(newValue);
  }
}
