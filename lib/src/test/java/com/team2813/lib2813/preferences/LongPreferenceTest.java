package com.team2813.lib2813.preferences;

import static com.google.common.truth.Truth.assertThat;

import edu.wpi.first.wpilibj.Preferences;
import java.util.function.LongSupplier;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

public final class LongPreferenceTest {
  @Rule public final TestRule isolatedPreferences = new IsolatedPreferences();

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

  @Test
  public void getValue() {
    // Act
    long value = LongPref.HAS_DEFAULT_42.get();

    // Assert
    assertThat(value).isEqualTo(42);

    // Act
    value = Preferences.getLong(LongPref.HAS_DEFAULT_42.key(), 123);

    // Assert
    assertThat(value).isEqualTo(42);
    assertThat(LongPref.HAS_DEFAULT_42.key())
        .isEqualTo("lib2813.preferences.LongPreferenceTest.LongPref.HAS_DEFAULT_42");
  }

  @Test
  public void asSupplier() {
    // Act
    LongSupplier supplier = LongPreferenceTest.LongPref.HAS_DEFAULT_42.asSupplier();

    // Assert
    assertThat(supplier.getAsLong()).isEqualTo(42);

    // Act
    LongPreferenceTest.LongPref.HAS_DEFAULT_42.set(99);

    // Assert
    assertThat(supplier.getAsLong()).isEqualTo(99);

    // Reset
    LongPreferenceTest.LongPref.HAS_DEFAULT_42.set(42);

    // Act - get value directly from Network Tables
    long value = Preferences.getLong(LongPreferenceTest.LongPref.HAS_DEFAULT_42.key(), -1);

    // Assert
    assertThat(value).isEqualTo(42);
  }

  @Test
  public void setValue() {
    // Act
    var newValue = LongPref.HAS_DEFAULT_42.defaultValue() + 100;
    LongPref.HAS_DEFAULT_42.set(newValue);

    // Assert
    assertThat(LongPref.HAS_DEFAULT_42.get()).isEqualTo(newValue);
    var value = Preferences.getLong(LongPref.HAS_DEFAULT_42.key(), newValue + 10);
    assertThat(value).isEqualTo(newValue);
  }
}
