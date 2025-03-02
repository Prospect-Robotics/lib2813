package com.team2813.lib2813.util.preferences;

import static com.google.common.truth.Truth.assertThat;

import edu.wpi.first.wpilibj.Preferences;
import java.util.function.IntSupplier;
import org.junit.After;
import org.junit.Test;

public final class IntPreferenceTest {
  private static final int DEFAULT_VALUE1 = 42;

  private enum IntPref implements IntPreference {
    CONFIGURABLE_VALUE1(DEFAULT_VALUE1),
    CONFIGURABLE_VALUE2(123);

    IntPref(int defaultValue) {
      this.defaultValue = defaultValue;
      initialize();
    }

    private final int defaultValue;

    @Override
    public int defaultValue() {
      return defaultValue;
    }
  }

  @After
  public void removePreferences() {
    for (var preference : IntPref.values()) {
      Preferences.remove(preference.key());
    }
  }

  @Test
  public void key() {
    assertThat(IntPref.CONFIGURABLE_VALUE1.key())
        .isEqualTo("lib2813.util.preferences.IntPreferenceTest.IntPref.CONFIGURABLE_VALUE1");
  }

  @Test
  public void getValue() {
    // Act
    long value = IntPref.CONFIGURABLE_VALUE1.get();

    // Assert
    assertThat(value).isEqualTo(DEFAULT_VALUE1);

    // Act
    value = Preferences.getInt(IntPref.CONFIGURABLE_VALUE1.key(), DEFAULT_VALUE1 + 10);

    // Assert
    assertThat(value).isEqualTo(DEFAULT_VALUE1);
  }

  @Test
  public void asSupplier() {
    // Act
    IntSupplier supplier = IntPreferenceTest.IntPref.CONFIGURABLE_VALUE1.asSupplier();

    // Assert
    assertThat(supplier.getAsInt()).isEqualTo(DEFAULT_VALUE1);

    // Act
    IntPreferenceTest.IntPref.CONFIGURABLE_VALUE1.set(DEFAULT_VALUE1 + 2);

    // Assert
    assertThat(supplier.getAsInt()).isEqualTo(DEFAULT_VALUE1 + 2);

    // Reset
    IntPreferenceTest.IntPref.CONFIGURABLE_VALUE1.set(DEFAULT_VALUE1);

    // Act - get value directly from Network Tables
    int value = Preferences.getInt(IntPreferenceTest.IntPref.CONFIGURABLE_VALUE1.key(), -1);

    // Assert
    assertThat(value).isEqualTo(DEFAULT_VALUE1);
  }

  @Test
  public void setValue() {
    // Act
    var newValue = 1024;
    IntPref.CONFIGURABLE_VALUE1.set(newValue);

    // Assert
    assertThat(IntPref.CONFIGURABLE_VALUE1.get()).isEqualTo(newValue);
    var value = Preferences.getInt(IntPref.CONFIGURABLE_VALUE1.key(), newValue + 10);
    assertThat(value).isEqualTo(newValue);

    // Act
    IntPref.CONFIGURABLE_VALUE1.set(DEFAULT_VALUE1);

    // Assert
    assertThat(IntPref.CONFIGURABLE_VALUE1.get()).isEqualTo(DEFAULT_VALUE1);
  }
}
