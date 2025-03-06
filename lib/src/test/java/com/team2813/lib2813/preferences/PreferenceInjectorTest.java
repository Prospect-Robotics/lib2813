package com.team2813.lib2813.preferences;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import edu.wpi.first.wpilibj.Preferences;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

public final class PreferenceInjectorTest {
  final PreferenceInjector injector =
      new PreferenceInjector("com.team2813.lib2813.preferences.PreferenceInjectorTest.");
  final Set<String> originalKeys = new HashSet<>();

  @Rule public final IsolatedPreferences isolatedPreferences = new IsolatedPreferences();
  @Rule public final ErrorCollector errorCollector = new ErrorCollector();

  @Before
  public void copyOriginalKeys() {
    originalKeys.addAll(Preferences.getKeys());
    assertThat(originalKeys).hasSize(1);
  }

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
    assertThat(newPreferenceKeys()).containsExactly(key1, key2);
    boolean value = Preferences.getBoolean(key1, false);
    assertThat(value).isTrue();
    value = Preferences.getBoolean(key2, true);
    assertThat(value).isFalse();

    // Arrange: Update preferences
    Preferences.setBoolean(key1, false);
    Preferences.setBoolean(key2, true);

    // Act
    injected = injector.injectPreferences(defaults);

    // Assert
    assertThat(injected).isEqualTo(new ContainsBooleans(false, true));
    assertThat(newPreferenceKeys()).containsExactly(key1, key2);
    value = Preferences.getBoolean(key1, true);
    assertThat(value).isFalse();
    value = Preferences.getBoolean(key2, false);
    assertThat(value).isTrue();
  }

  @Test
  public void injectsBooleans_existingPreferences() {
    // Arrange
    String key1 = "ContainsBooleans.first";
    String key2 = "ContainsBooleans.second";
    Preferences.initBoolean(key1, false);
    Preferences.initBoolean(key2, true);
    Map<String, Long> lastChanges = lastChanges();
    var defaults = new ContainsBooleans(true, false);

    // Act
    ContainsBooleans injected = injector.injectPreferences(defaults);

    // Assert
    assertThat(injected).isEqualTo(new ContainsBooleans(false, true));
    assertHasNoChangesSince(lastChanges);
  }

  private void assertHasNoChangesSince(Map<String, Long> previousSnapshot) {
    var lastChanges = lastChanges();
    assertWithMessage("Unexpected changes to preference values")
        .that(lastChanges)
        .isEqualTo(previousSnapshot);
  }

  private Set<String> newPreferenceKeys() {
    Set<String> keys = new HashSet<>(Preferences.getKeys());
    keys.removeAll(originalKeys);
    return keys;
  }

  private Map<String, Long> lastChanges() {
    Map<String, Long> map = new HashMap<>();
    var table = isolatedPreferences.getNetworkTableInstance();
    for (String key : Preferences.getKeys()) {
      if (!key.startsWith(".")) { // Preferences adds a ".type" key
        map.put(key, table.getEntry(key).getLastChange());
      }
    }
    return map;
  }
}
