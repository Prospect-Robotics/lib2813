package com.team2813.lib2813.util.preferences;

import static com.google.common.truth.Truth.assertThat;

import edu.wpi.first.wpilibj.Preferences;
import org.junit.After;
import org.junit.Test;

public final class BooleanPreferenceTest {

  private enum BooleanPref implements BooleanPreference {
    HAS_DEFAULT_TRUE(true),
    HAS_DEFAULT_FALSE();

    BooleanPref(boolean defaultValue) {
      this.defaultValue = defaultValue;
    }

    BooleanPref() {
      this(false);
    }

    private final boolean defaultValue;

    @Override
    public boolean defaultValue() {
      return defaultValue;
    }
  }

  @After
  public void removePreferences() {
    for (var preference : BooleanPref.values()) {
      Preferences.remove(preference.key());
    }
  }

  @Test
  public void key() {
    assertThat(BooleanPref.HAS_DEFAULT_TRUE.key())
        .isEqualTo("lib2813.util.preferences.BooleanPreferenceTest.BooleanPref.HAS_DEFAULT_TRUE");
  }

  @Test
  public void getValue_defaultTrue() {
    // Act
    boolean value = BooleanPref.HAS_DEFAULT_TRUE.getAsBoolean();

    // Assert
    assertThat(value).isTrue();

    // Act
    value = BooleanPref.HAS_DEFAULT_TRUE.get();

    // Assert
    assertThat(value).isTrue();

    // Act
    value = Preferences.getBoolean(BooleanPref.HAS_DEFAULT_TRUE.key(), false);

    // Assert
    assertThat(value).isTrue();
  }

  @Test
  public void getValue_defaultFalse() {
    // Act
    boolean value = BooleanPref.HAS_DEFAULT_FALSE.getAsBoolean();

    // Assert
    assertThat(value).isFalse();

    // Act
    value = BooleanPref.HAS_DEFAULT_FALSE.get();

    // Assert
    assertThat(value).isFalse();

    value = Preferences.getBoolean(BooleanPref.HAS_DEFAULT_FALSE.key(), true);
    assertThat(value).isFalse();
  }

  @Test
  public void setValue_defaultTrue() {
    // Act
    BooleanPref.HAS_DEFAULT_TRUE.set(false);

    // Assert
    assertThat(BooleanPref.HAS_DEFAULT_TRUE.getAsBoolean()).isFalse();
    var value = Preferences.getBoolean(BooleanPref.HAS_DEFAULT_TRUE.key(), true);
    assertThat(value).isFalse();

    // Act
    BooleanPref.HAS_DEFAULT_TRUE.set(true);

    // Assert
    assertThat(BooleanPref.HAS_DEFAULT_TRUE.getAsBoolean()).isTrue();
  }

  @Test
  public void setValue_defaulFalse() {
    // Act
    BooleanPref.HAS_DEFAULT_FALSE.set(true);

    // Assert
    assertThat(BooleanPref.HAS_DEFAULT_FALSE.getAsBoolean()).isTrue();
    var value = Preferences.getBoolean(BooleanPref.HAS_DEFAULT_FALSE.key(), false);
    assertThat(value).isTrue();

    // Act
    BooleanPref.HAS_DEFAULT_FALSE.set(false);

    // Assert
    assertThat(BooleanPref.HAS_DEFAULT_FALSE.getAsBoolean()).isFalse();
  }
}
