package com.team2813.lib2813.preferences;

import static com.google.common.truth.Truth.assertThat;

import edu.wpi.first.wpilibj.Preferences;
import java.util.function.DoubleSupplier;
import org.junit.After;
import org.junit.Test;

public final class DoublePreferenceTest {
  private static final int DEFAULT_VALUE1 = 42;

  private enum DoublePref implements DoublePreference {
    CONFIGURABLE_VALUE1(DEFAULT_VALUE1),
    CONFIGURABLE_VALUE2(123);

    DoublePref(int defaultValue) {
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
    for (var preference : DoublePref.values()) {
      Preferences.remove(preference.key());
    }
  }

  @Test
  public void key() {
    assertThat(DoublePref.CONFIGURABLE_VALUE1.key())
        .isEqualTo("lib2813.preferences.DoublePreferenceTest.DoublePref.CONFIGURABLE_VALUE1");
  }

  @Test
  public void getValue() {
    // Act
    double value = DoublePref.CONFIGURABLE_VALUE1.get();

    // Assert
    assertThat(value).isEqualTo(DEFAULT_VALUE1);

    // Act
    value = Preferences.getDouble(DoublePref.CONFIGURABLE_VALUE1.key(), -1);

    // Assert
    assertThat(value).isEqualTo(DEFAULT_VALUE1);
  }

  @Test
  public void asSupplier() {
    // Act
    DoubleSupplier supplier = DoublePreferenceTest.DoublePref.CONFIGURABLE_VALUE1.asSupplier();

    // Assert
    assertThat(supplier.getAsDouble()).isEqualTo(DEFAULT_VALUE1);

    // Act
    DoublePreferenceTest.DoublePref.CONFIGURABLE_VALUE1.set(DEFAULT_VALUE1 + 2);

    // Assert
    assertThat(supplier.getAsDouble()).isEqualTo(DEFAULT_VALUE1 + 2);

    // Reset
    DoublePreferenceTest.DoublePref.CONFIGURABLE_VALUE1.set(DEFAULT_VALUE1);

    // Act - get value directly from Network Tables
    double value =
        Preferences.getDouble(DoublePreferenceTest.DoublePref.CONFIGURABLE_VALUE1.key(), -1);

    // Assert
    assertThat(value).isEqualTo(DEFAULT_VALUE1);
  }

  @Test
  public void setValue() {
    // Act
    var newValue = 1024;
    DoublePref.CONFIGURABLE_VALUE1.set(newValue);

    // Assert
    assertThat(DoublePref.CONFIGURABLE_VALUE1.get()).isEqualTo(newValue);
    var value = Preferences.getDouble(DoublePref.CONFIGURABLE_VALUE1.key(), newValue + 10);
    assertThat(value).isEqualTo(newValue);

    // Act
    DoublePref.CONFIGURABLE_VALUE1.set(DEFAULT_VALUE1);

    // Assert
    assertThat(DoublePref.CONFIGURABLE_VALUE1.get()).isEqualTo(DEFAULT_VALUE1);
  }
}
