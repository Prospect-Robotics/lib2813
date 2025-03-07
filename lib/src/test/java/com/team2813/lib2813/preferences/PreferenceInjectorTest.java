package com.team2813.lib2813.preferences;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.wpilibj.Preferences;
import java.util.Map;
import java.util.Set;
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

    @FunctionalInterface
    interface Factory<T extends Record & WithBooleans> {
      T create(boolean firstDefaultValue, boolean secondDefaultValue);
    }

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

  /** Test record for testing classes that contain boolean fields. */
  record ContainsBooleans(boolean first, boolean second) implements WithBooleans {

    @Override
    public boolean firstValue() {
      return first;
    }

    @Override
    public boolean secondValue() {
      return second;
    }
  }

  /** Test record for testing classes that contain {@code BooleanSupplier} fields. */
  record ContainsBooleanSuppliers(BooleanSupplier first, BooleanSupplier second)
      implements WithBooleans {

    static ContainsBooleanSuppliers factory(boolean firstValue, boolean secondValue) {
      return new ContainsBooleanSuppliers(() -> firstValue, () -> secondValue);
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

  /** Test record for testing classes that contain {@code Supplier<Boolean>} fields. */
  record ContainsSuppliersOfBoolean(Supplier<Boolean> first, Supplier<Boolean> second)
      implements WithBooleans {

    static ContainsSuppliersOfBoolean factory(boolean firstValue, boolean secondValue) {
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
    tester.test(ContainsBooleans::new);
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
  public void injectsBooleans_withExistingPreferences() {
    var tester = new BooleanTester<>(ContainsBooleans.class);
    tester.withExistingPreferences = true;
    tester.test(ContainsBooleans::new);
  }

  @Test
  public void injectsBooleanSuppliers_withExistingPreferences() {
    var tester = new BooleanTester<>(ContainsBooleanSuppliers.class);
    tester.withExistingPreferences = true;
    tester.test(ContainsBooleanSuppliers::factory);
  }

  @Test
  public void injectsSuppliersBoolean_withExistingPreferences() {
    var tester = new BooleanTester<>(ContainsSuppliersOfBoolean.class);
    tester.withExistingPreferences = true;
    tester.test(ContainsSuppliersOfBoolean::factory);
  }

  private class BooleanTester<T extends Record & WithBooleans> {
    final String prefix;

    /** Whether the Preferences should exist before the test is run. */
    boolean withExistingPreferences = false;

    BooleanTester(Class<T> recordClass) {
      prefix = recordClass.getSimpleName();
    }

    /**
     * Runs the test.
     *
     * @param defaultInstanceFactory Factory which creates a record, passing the first and second
     *     values as params.
     */
    void test(WithBooleans.Factory<T> defaultInstanceFactory) {
      boolean expectedInjectedValue1 = true;
      boolean expectedInjectedValue2 = false;
      String key1 = prefix + ".first";
      String key2 = prefix + ".second";

      // Arrange
      T defaults; // Record with values set to their defaults
      if (withExistingPreferences) {
        // Set preference values to what we expected to get after inject() is called (see
        // expectedInjectedRecord, above). The record passed to injectPreferences() should have the
        // opposite values.
        Preferences.initBoolean(key1, expectedInjectedValue1);
        Preferences.initBoolean(key2, expectedInjectedValue2);
        defaults = defaultInstanceFactory.create(!expectedInjectedValue1, !expectedInjectedValue2);
      } else {
        // No preloaded preferences; the default values in the record should be the expected values.
        defaults = defaultInstanceFactory.create(expectedInjectedValue1, expectedInjectedValue2);
      }

      // Act
      T injected = injector.injectPreferences(defaults);

      // Assert
      T expected = defaultInstanceFactory.create(expectedInjectedValue1, expectedInjectedValue2);
      assertThat(injected).isEqualTo(expected);

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
      expected = defaultInstanceFactory.create(false, true);
      assertThat(injected).isEqualTo(expected);
      assertThat(preferenceKeys()).containsExactly(key1, key2);
      value = Preferences.getBoolean(key1, true);
      assertThat(value).isFalse();
      value = Preferences.getBoolean(key2, false);
      assertThat(value).isTrue();
      assertHasNoChangesSince(preferenceValues);
    }
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
