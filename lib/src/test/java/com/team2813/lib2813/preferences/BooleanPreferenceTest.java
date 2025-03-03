package com.team2813.lib2813.preferences;

import static com.google.common.truth.Truth.assertThat;

import edu.wpi.first.wpilibj.Preferences;
import java.util.function.BooleanSupplier;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

public final class BooleanPreferenceTest {
  @Rule public final TestRule isolatedPreferences = new IsolatedPreferences();

  private enum BooleanPref implements BooleanPreference {
    HAS_DEFAULT_TRUE(true),
    HAS_DEFAULT_FALSE();

    BooleanPref(boolean defaultValue) {
      this.defaultValue = defaultValue;
      initialize();
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

  @Test
  public void key() {
    assertThat(BooleanPref.HAS_DEFAULT_TRUE.key())
        .isEqualTo("lib2813.preferences.BooleanPreferenceTest.BooleanPref.HAS_DEFAULT_TRUE");
  }

  @Test
  public void getValue_defaultTrue() {
    // Act
    boolean value = BooleanPref.HAS_DEFAULT_TRUE.get();

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
    boolean value = BooleanPref.HAS_DEFAULT_FALSE.get();

    // Assert
    assertThat(value).isFalse();

    value = Preferences.getBoolean(BooleanPref.HAS_DEFAULT_FALSE.key(), true);
    assertThat(value).isFalse();
  }

  @Test
  public void asSupplier_defaultTrue() {
    // Act
    BooleanSupplier supplier = BooleanPref.HAS_DEFAULT_TRUE.asSupplier();

    // Assert
    assertThat(supplier.getAsBoolean()).isTrue();

    // Act
    BooleanPref.HAS_DEFAULT_TRUE.set(false);

    // Assert
    assertThat(supplier.getAsBoolean()).isFalse();

    // Reset
    BooleanPref.HAS_DEFAULT_TRUE.set(true);

    // Act - get value directly from Network Tables
    boolean value = Preferences.getBoolean(BooleanPref.HAS_DEFAULT_TRUE.key(), false);

    // Assert
    assertThat(value).isTrue();
  }

  @Test
  public void asSupplier_defaultFalse() {
    // Act
    BooleanSupplier supplier = BooleanPref.HAS_DEFAULT_FALSE.asSupplier();

    // Assert
    assertThat(supplier.getAsBoolean()).isFalse();

    // Act
    BooleanPref.HAS_DEFAULT_FALSE.set(true);

    // Assert
    assertThat(supplier.getAsBoolean()).isTrue();

    // Reset
    BooleanPref.HAS_DEFAULT_FALSE.set(false);

    // Act - get value directly from Network Tables
    boolean value = Preferences.getBoolean(BooleanPref.HAS_DEFAULT_FALSE.key(), false);

    // Assert
    assertThat(value).isFalse();
  }

  @Test
  public void setValue_defaultTrue() {
    // Act
    BooleanPref.HAS_DEFAULT_TRUE.set(false);

    // Assert
    assertThat(BooleanPref.HAS_DEFAULT_TRUE.get()).isFalse();
    var value = Preferences.getBoolean(BooleanPref.HAS_DEFAULT_TRUE.key(), true);
    assertThat(value).isFalse();

    // Act
    BooleanPref.HAS_DEFAULT_TRUE.set(true);

    // Assert
    assertThat(BooleanPref.HAS_DEFAULT_TRUE.get()).isTrue();
  }

  @Test
  public void setValue_defaultFalse() {
    // Act
    BooleanPref.HAS_DEFAULT_FALSE.set(true);

    // Assert
    assertThat(BooleanPref.HAS_DEFAULT_FALSE.get()).isTrue();
    var value = Preferences.getBoolean(BooleanPref.HAS_DEFAULT_FALSE.key(), false);
    assertThat(value).isTrue();

    // Act
    BooleanPref.HAS_DEFAULT_FALSE.set(false);

    // Assert
    assertThat(BooleanPref.HAS_DEFAULT_FALSE.get()).isFalse();
  }
}
