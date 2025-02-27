package com.team2813.lib2813.util.preferences;

import static com.google.common.truth.Truth.assertThat;

import edu.wpi.first.wpilibj.Preferences;
import org.junit.After;
import org.junit.Test;

public final class LongPreferenceTest {

  private enum Key implements PreferenceKey {
    KEY;
  }

  private static final long DEFAULT_VALUE = 123;
  private static final LongPreference<Key> PREFERENCE =
      new LongPreference<>(Key.KEY, DEFAULT_VALUE);

  @After
  public void removePreferences() {
    Preferences.remove(PREFERENCE.key);
  }

  @Test
  public void getValue() {
    // Act
    long value = PREFERENCE.getAsLong();

    // Assert
    assertThat(value).isEqualTo(DEFAULT_VALUE);

    // Act
    value = PREFERENCE.get();

    // Assert
    assertThat(value).isEqualTo(DEFAULT_VALUE);

    // Act
    value = Preferences.getLong(PREFERENCE.key, DEFAULT_VALUE + 10);

    // Assert
    assertThat(value).isEqualTo(DEFAULT_VALUE + 10);
    assertThat(PREFERENCE.key).isEqualTo("lib2813.util.preferences.LongPreferenceTest.Key.KEY");
  }

  @Test
  public void setValue() {
    // Act
    var newValue = DEFAULT_VALUE + 1;
    PREFERENCE.set(newValue);

    // Assert
    assertThat(PREFERENCE.getAsLong()).isEqualTo(newValue);
    var value = Preferences.getLong(PREFERENCE.key, newValue + 10);
    assertThat(value).isEqualTo(newValue);
  }
}
