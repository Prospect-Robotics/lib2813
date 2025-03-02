package com.team2813.lib2813.util.preferences;

import static com.google.common.truth.Truth.assertThat;

import edu.wpi.first.wpilibj.Preferences;
import java.util.function.Supplier;
import org.junit.After;
import org.junit.Test;

public final class StringPreferenceTest {
  private static final String DEFAULT_VALUE1 = "Chicken";

  private enum StringPref implements StringPreference {
    CONFIGURABLE_NAME(DEFAULT_VALUE1);

    StringPref(String defaultValue) {
      this.defaultValue = defaultValue;
      initialize();
    }

    private final String defaultValue;

    @Override
    public String defaultValue() {
      return defaultValue;
    }
  }

  @After
  public void removePreferences() {
    for (var preference : StringPref.values()) {
      Preferences.remove(preference.key());
    }
  }

  @Test
  public void key() {
    assertThat(StringPref.CONFIGURABLE_NAME.key())
        .isEqualTo("lib2813.util.preferences.StringPreferenceTest.StringPref.CONFIGURABLE_NAME");
  }

  @Test
  public void getValue() {
    // Act
    String value = StringPref.CONFIGURABLE_NAME.get();

    // Assert
    assertThat(value).isEqualTo(DEFAULT_VALUE1);

    // Act
    value = Preferences.getString(StringPref.CONFIGURABLE_NAME.key(), DEFAULT_VALUE1 + 10);

    // Assert
    assertThat(value).isEqualTo(DEFAULT_VALUE1);
  }

  @Test
  public void asSupplier() {
    // Act
    Supplier<String> supplier = StringPreferenceTest.StringPref.CONFIGURABLE_NAME.asSupplier();

    // Assert
    assertThat(supplier.get()).isEqualTo(DEFAULT_VALUE1);

    // Act
    StringPreferenceTest.StringPref.CONFIGURABLE_NAME.set("Monkey");

    // Assert
    assertThat(supplier.get()).isEqualTo("Monkey");

    // Reset
    StringPreferenceTest.StringPref.CONFIGURABLE_NAME.set(DEFAULT_VALUE1);

    // Act - get value directly from Network Tables
    String value =
        Preferences.getString(StringPreferenceTest.StringPref.CONFIGURABLE_NAME.key(), "");

    // Assert
    assertThat(value).isEqualTo(DEFAULT_VALUE1);
  }

  @Test
  public void setValue() {
    // Act
    var newValue = "GearHead";
    StringPref.CONFIGURABLE_NAME.set(newValue);

    // Assert
    assertThat(StringPref.CONFIGURABLE_NAME.get()).isEqualTo(newValue);
    var value = Preferences.getString(StringPref.CONFIGURABLE_NAME.key(), "");
    assertThat(value).isEqualTo(newValue);

    // Act
    StringPref.CONFIGURABLE_NAME.set(DEFAULT_VALUE1);

    // Assert
    assertThat(StringPref.CONFIGURABLE_NAME.get()).isEqualTo(DEFAULT_VALUE1);
  }
}
