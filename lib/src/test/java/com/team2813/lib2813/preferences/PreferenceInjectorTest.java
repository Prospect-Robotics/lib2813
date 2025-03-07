package com.team2813.lib2813.preferences;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.wpilibj.Preferences;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
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

  /**
   * Common interface for test record classes that contain two boolean values. The component names
   * must be "first" and "second".
   */
  interface WithBooleans {
    boolean firstValue();

    boolean secondValue();

    default boolean isEqual(Object other) {
      if (this == other) {
        return true;
      }
      if (other == null) {
        return false;
      }
      if (!other.getClass().equals(getClass())) {
        return false;
      }
      WithBooleans that = (WithBooleans) other;
      return this.firstValue() == that.firstValue() && this.secondValue() == that.secondValue();
    }
  }

  record ContainsBooleans(boolean first, boolean second) implements WithBooleans {

    static ContainsBooleans factory(Boolean first, Boolean second) {
      return new ContainsBooleans(first, second);
    }

    @Override
    public boolean firstValue() {
      return first;
    }

    @Override
    public boolean secondValue() {
      return second;
    }
  }

  record ContainsBooleanSuppliers(BooleanSupplier first, BooleanSupplier second)
      implements WithBooleans {

    static ContainsBooleanSuppliers factory(Boolean firstValue, Boolean secondValue) {
      return new ContainsBooleanSuppliers(firstValue::booleanValue, secondValue::booleanValue);
    }

    @Override
    public boolean firstValue() {
      return first.getAsBoolean();
    }

    @Override
    public boolean secondValue() {
      return second.getAsBoolean();
    }

    @Override
    public boolean equals(Object other) {
      return isEqual(other);
    }
  }

  record ContainsSuppliersOfBoolean(Supplier<Boolean> first, Supplier<Boolean> second)
      implements WithBooleans {

    static ContainsSuppliersOfBoolean factory(Boolean firstValue, Boolean secondValue) {
      return new ContainsSuppliersOfBoolean(() -> firstValue, () -> secondValue);
    }

    @Override
    public boolean firstValue() {
      return first.get();
    }

    @Override
    public boolean secondValue() {
      return second.get();
    }

    @Override
    public boolean equals(Object other) {
      return isEqual(other);
    }
  }

  @Test
  public void injectsBooleans_newPreferences() {
    var tester = new BooleanTester<>(ContainsBooleans.class);
    tester.test(ContainsBooleans::factory);
  }

  @Test
  public void injectsBooleanSuppliers_newPreferences() {
    var tester = new BooleanTester<>(ContainsBooleanSuppliers.class);
    tester.test(ContainsBooleanSuppliers::factory);
  }

  @Test
  public void injectsSuppliersOfBoolean_newPreferences() {
    var tester = new BooleanTester<>(ContainsSuppliersOfBoolean.class);
    tester.test(ContainsSuppliersOfBoolean::factory);
  }

  @Test
  public void injectsBooleans_existingPreferences() {
    var tester = new BooleanTester<>(ContainsBooleans.class);
    tester.existingPreferences = true;
    tester.test(ContainsBooleans::factory);
  }

  @Test
  public void injectsBooleanSuppliers_existingPreferences() {
    var tester = new BooleanTester<>(ContainsBooleanSuppliers.class);
    tester.existingPreferences = true;
    tester.test(ContainsBooleanSuppliers::factory);
  }

  @Test
  public void injectsSuppliersBoolean_existingPreferences() {
    var tester = new BooleanTester<>(ContainsSuppliersOfBoolean.class);
    tester.existingPreferences = true;
    tester.test(ContainsSuppliersOfBoolean::factory);
  }

  private class BooleanTester<T extends Record & WithBooleans> {
    final String prefix;
    boolean existingPreferences = false;

    BooleanTester(Class<T> recordClass) {
      prefix = recordClass.getSimpleName();
    }

    /**
     * Runs the test.
     *
     * @param defaultInstanceFactory Factory which creates a record, passing the first and second
     *     values as params.
     */
    void test(BiFunction<Boolean, Boolean, T> defaultInstanceFactory) {
      // Arrange
      boolean default1 = !existingPreferences;
      boolean default2 = !default1;
      String key1 = prefix + ".first";
      String key2 = prefix + ".second";

      if (existingPreferences) {
        Preferences.initBoolean(key1, true);
        Preferences.initBoolean(key2, false);
      }
      T defaults = defaultInstanceFactory.apply(default1, default2);

      // Act
      T injected = injector.injectPreferences(defaults);

      // Assert
      if (!existingPreferences) {
        assertThat(injected).isEqualTo(defaults);
      }
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
      if (injected.getClass().equals(ContainsBooleans.class)) {
        // The record is immutable and contains boolean values, so to "see" the new values
        // we need to create a new record from the prefereces.
        injected = injector.injectPreferences(defaults);
      }

      // Assert
      T expected = defaultInstanceFactory.apply(Boolean.FALSE, Boolean.TRUE);
      assertThat(injected).isEqualTo(expected);
      assertThat(preferenceKeys()).containsExactly(key1, key2);
      value = Preferences.getBoolean(key1, true);
      assertThat(value).isFalse();
      value = Preferences.getBoolean(key2, false);
      assertThat(value).isTrue();
      assertHasNoChangesSince(preferenceValues);
    }
  }

  private <T extends Record & WithBooleans> void withBooleanSuppliers(
      boolean withExistingPreferences, BiFunction<Boolean, Boolean, T> defaultInstanceFactory) {
    // Arrange
    T defaults = defaultInstanceFactory.apply(Boolean.FALSE, Boolean.TRUE);
    String prefix = defaults.getClass().getSimpleName();
    String key1 = prefix + ".first";
    String key2 = prefix + ".second";

    if (withExistingPreferences) {
      Preferences.initBoolean(key1, true);
      Preferences.initBoolean(key2, false);
    }

    // Act
    T injected = injector.injectPreferences(defaults);

    // Assert
    assertThat(injected).isEqualTo(defaults);
    assertThat(preferenceKeys()).containsExactly(key1, key2);
    boolean value = Preferences.getBoolean(key1, false);
    assertThat(value).isTrue();
    value = Preferences.getBoolean(key2, true);
    assertThat(value).isFalse();

    // Arrange: Update preferences
    Preferences.setBoolean(key1, false);
    Preferences.setBoolean(key2, true);
    var preferenceValues = preferenceValues();

    // Assert
    T expected = defaultInstanceFactory.apply(Boolean.FALSE, Boolean.TRUE);
    assertThat(injected).isEqualTo(expected);
    assertThat(preferenceKeys()).containsExactly(key1, key2);
    value = Preferences.getBoolean(key1, true);
    assertThat(value).isFalse();
    value = Preferences.getBoolean(key2, false);
    assertThat(value).isTrue();
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
    return preferenceKeys().stream()
        .collect(toMap(Function.identity(), key -> table.getEntry(key).getValue().getValue()));
  }

  private Set<String> preferenceKeys() {
    // Preferences adds a ".type" key; we filter it out here.
    return Preferences.getKeys().stream().filter(key -> !key.startsWith(".")).collect(toSet());
  }
}
