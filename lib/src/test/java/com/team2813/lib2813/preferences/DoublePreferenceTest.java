package com.team2813.lib2813.preferences;

import static com.google.common.truth.Truth.assertThat;

import edu.wpi.first.wpilibj.Preferences;
import java.util.function.DoubleSupplier;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

public final class DoublePreferenceTest {
  @Rule public final TestRule isolatedPreferences = new IsolatedPreferences();

  private static final double TOLERANCE = 0.01;
  private static final double DEFAULT_VALUE = 3.14159;
  private static final double OTHER_VALUE = 2.71828; // Should be != DEFAULT_VALUE

  private enum DoublePref implements DoublePreference {
    CONFIGURABLE_VALUE(DEFAULT_VALUE);

    DoublePref(double defaultValue) {
      this.defaultValue = defaultValue;
      initialize();
    }

    private final double defaultValue;

    @Override
    public double defaultValue() {
      return defaultValue;
    }
  }

  @Test
  public void key() {
    assertThat(DoublePref.CONFIGURABLE_VALUE.key())
        .isEqualTo("lib2813.preferences.DoublePreferenceTest.DoublePref.CONFIGURABLE_VALUE");
  }

  @Test
  public void getValue() {
    // Act
    double value = DoublePref.CONFIGURABLE_VALUE.get();

    // Assert
    assertThat(value).isEqualTo(DEFAULT_VALUE);

    // Act
    value = Preferences.getDouble(DoublePref.CONFIGURABLE_VALUE.key(), -1);

    // Assert
    assertThat(value).isWithin(TOLERANCE).of(DEFAULT_VALUE);
  }

  @Test
  public void asSupplier() {
    // Act
    DoubleSupplier supplier = DoublePreferenceTest.DoublePref.CONFIGURABLE_VALUE.asSupplier();

    // Assert
    assertThat(supplier.getAsDouble()).isWithin(TOLERANCE).of(DEFAULT_VALUE);

    // Act
    double newValue = OTHER_VALUE;
    DoublePreferenceTest.DoublePref.CONFIGURABLE_VALUE.set(newValue);

    // Assert
    assertThat(supplier.getAsDouble()).isWithin(TOLERANCE).of(newValue);

    // Reset
    DoublePreferenceTest.DoublePref.CONFIGURABLE_VALUE.set(DEFAULT_VALUE);

    // Act - get value directly from Network Tables
    double value =
        Preferences.getDouble(DoublePreferenceTest.DoublePref.CONFIGURABLE_VALUE.key(), -1);

    // Assert
    assertThat(value).isWithin(TOLERANCE).of(DEFAULT_VALUE);
  }

  @Test
  public void setValue() {
    // Act
    double newValue = OTHER_VALUE;
    DoublePref.CONFIGURABLE_VALUE.set(newValue);

    // Assert
    assertThat(DoublePref.CONFIGURABLE_VALUE.get()).isWithin(TOLERANCE).of(newValue);
    var value = Preferences.getDouble(DoublePref.CONFIGURABLE_VALUE.key(), newValue + 10);
    assertThat(value).isWithin(TOLERANCE).of(newValue);

    // Act
    DoublePref.CONFIGURABLE_VALUE.set(DEFAULT_VALUE);

    // Assert
    assertThat(DoublePref.CONFIGURABLE_VALUE.get()).isWithin(TOLERANCE).of(DEFAULT_VALUE);
  }
}
