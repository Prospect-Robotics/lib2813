package com.team2813.lib2813.preferences;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static java.util.stream.Collectors.toSet;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.wpilibj.Preferences;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

public final class PreferenceInjectorTest {
  final PreferenceInjector injector =
      new PreferenceInjector("com.team2813.lib2813.preferences.PreferenceInjectorTest.");

  @Rule public final IsolatedPreferences isolatedPreferences = new IsolatedPreferences();
  @Rule public final ErrorCollector errorCollector = new ErrorCollector();

  @Before
  public void configureInjector() {
    injector.throwExceptions = true;
    injector.errorReporter =
        message ->
            errorCollector.addError(new AssertionError("Unexpected warning: \"" + message + "\""));
  }

  record ContainsBooleans(boolean first, boolean second) {}

  @Test
  public void injectsBooleans_newPreferences() {
    // Arrange
    var defaults = new ContainsBooleans(true, false);

    // Act
    ContainsBooleans injected = injector.injectPreferences(defaults);

    // Assert
    assertThat(injected).isEqualTo(defaults);
    String key1 = "ContainsBooleans.first";
    String key2 = "ContainsBooleans.second";
    assertThat(preferenceKeys()).containsExactly(key1, key2);
    boolean value = Preferences.getBoolean(key1, false);
    assertThat(value).isTrue();
    value = Preferences.getBoolean(key2, true);
    assertThat(value).isFalse();

    // Arrange: Update preferences
    Preferences.setBoolean(key1, false);
    Preferences.setBoolean(key2, true);
    var preferenceValues = preferenceValues();

    // Act
    injected = injector.injectPreferences(defaults);

    // Assert
    assertThat(injected).isEqualTo(new ContainsBooleans(false, true));
    assertThat(preferenceKeys()).containsExactly(key1, key2);
    value = Preferences.getBoolean(key1, true);
    assertThat(value).isFalse();
    value = Preferences.getBoolean(key2, false);
    assertThat(value).isTrue();
    assertHasNoChangesSince(preferenceValues);
  }

  @Test
  public void injectsBooleans_existingPreferences() {
    // Arrange
    String key1 = "ContainsBooleans.first";
    String key2 = "ContainsBooleans.second";
    Preferences.initBoolean(key1, false);
    Preferences.initBoolean(key2, true);
    var preferenceValues = preferenceValues();
    var defaults = new ContainsBooleans(true, false);

    // Act
    ContainsBooleans injected = injector.injectPreferences(defaults);

    // Assert
    assertThat(injected).isEqualTo(new ContainsBooleans(false, true));
    assertHasNoChangesSince(preferenceValues);
  }

  private void assertHasNoChangesSince(Map<String, Object> previousValues) {
    var preferenceValues = preferenceValues();
    assertWithMessage("Unexpected no changes to preference values")
        .that(preferenceValues)
        .isEqualTo(previousValues);
  }

  private Map<String, Object> preferenceValues() {
    NetworkTable table = isolatedPreferences.getTable();
    Map<String, Object> map = new HashMap<>();
    for (String key : preferenceKeys()) {
      Object value = table.getEntry(key).getValue().getValue();
      map.put(key, value);
    }
    return map;
  }

  private Set<String> preferenceKeys() {
    // Preferences adds a ".type" key; we filter it out here.
    return Preferences.getKeys().stream().filter(key -> !key.startsWith(".")).collect(toSet());
  }
}
