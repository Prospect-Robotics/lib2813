package com.team2813.lib2813.preferences;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.wpilibj.Preferences;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/** Tests for {@link PreferenceInjector}. */
@RunWith(Enclosed.class)
public final class PreferenceInjectorTest {

  @RunWith(Parameterized.class)
  public static class BooleanPreferencesTest extends PreferenceInjectorTestCase {
    private final TestParams<?> params;

    public record TestParams<T extends Record & WithBooleans>(
        Class<T> recordClass, WithBooleans.Factory<T> recordFactory) {}

    @Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
      return Arrays.asList(
          new Object[][] {
            {"boolean components", new TestParams<>(ContainsBooleans.class, ContainsBooleans::new)},
            {
              "BooleanSupplier components",
              new TestParams<>(ContainsBooleanSuppliers.class, ContainsBooleanSuppliers::factory)
            },
            {
              "Supplier<Boolean> components",
              new TestParams<>(
                  ContainsSuppliersOfBoolean.class, ContainsSuppliersOfBoolean::factory)
            }
          });
    }

    public BooleanPreferencesTest(String testName, TestParams<?> params) {
      this.params = params;
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
    }

    @Test
    public void withoutExistingPreferences() {
      String key1 = keyForFieldName(params.recordClass, "first");
      String key2 = keyForFieldName(params.recordClass, "second");

      // Arrange
      var recordWithDefaults = params.recordFactory.create(true, false);

      // Act
      var recordWithPreferences = injector.injectPreferences(recordWithDefaults);

      // Assert
      assertThat(recordWithPreferences.firstValue()).isTrue();
      assertThat(recordWithPreferences.secondValue()).isFalse();

      assertThat(preferenceKeys()).containsExactly(key1, key2);
      assertThat(Preferences.getBoolean(key1, false)).isTrue();
      assertThat(Preferences.getBoolean(key2, true)).isFalse();

      // Arrange: Update preferences
      Preferences.setBoolean(key1, false);
      Preferences.setBoolean(key2, true);
      var preferenceValues = preferenceValues();

      // Act
      if (params.recordClass.equals(ContainsBooleans.class)) {
        // The record is immutable and contains boolean values, so to "see" the new values
        // we need to create a new record from the preferences.
        recordWithPreferences = injector.injectPreferences(recordWithDefaults);
      }

      // Assert
      assertThat(recordWithPreferences.firstValue()).isFalse();
      assertThat(recordWithPreferences.secondValue()).isTrue();
      assertThat(preferenceKeys()).containsExactly(key1, key2);
      assertThat(Preferences.getBoolean(key1, true)).isFalse();
      assertThat(Preferences.getBoolean(key2, false)).isTrue();
      assertHasNoChangesSince(preferenceValues);
    }

    @Test
    public void withExistingPreferences() {
      boolean expectedInjectedValue1 = true;
      boolean expectedInjectedValue2 = false;
      String key1 = keyForFieldName(params.recordClass, "first");
      String key2 = keyForFieldName(params.recordClass, "second");

      // Arrange
      var recordWithDefaults = params.recordFactory.create(false, true);
      Preferences.initBoolean(key1, true);
      Preferences.initBoolean(key2, false);

      // Act
      var recordWithPreferences = injector.injectPreferences(recordWithDefaults);

      // Assert
      assertThat(Preferences.getBoolean(key1, false)).isTrue();
      assertThat(Preferences.getBoolean(key2, true)).isFalse();

      assertThat(preferenceKeys()).containsExactly(key1, key2);
      assertThat(Preferences.getBoolean(key1, false)).isTrue();
      assertThat(Preferences.getBoolean(key2, true)).isFalse();

      // Arrange: Update preferences
      Preferences.setBoolean(key1, false);
      Preferences.setBoolean(key2, true);
      var preferenceValues = preferenceValues();

      // Act
      if (params.recordClass.equals(ContainsBooleans.class)) {
        // The record is immutable and contains boolean values, so to "see" the new values
        // we need to create a new record from the preferences.
        recordWithPreferences = injector.injectPreferences(recordWithDefaults);
      }

      // Assert
      assertThat(recordWithPreferences.firstValue()).isFalse();
      assertThat(recordWithPreferences.secondValue()).isTrue();
      assertThat(preferenceKeys()).containsExactly(key1, key2);
      assertThat(Preferences.getBoolean(key1, true)).isFalse();
      assertThat(Preferences.getBoolean(key2, false)).isTrue();
      assertHasNoChangesSince(preferenceValues);
    }
  }

  @RunWith(Parameterized.class)
  public static class LongPreferencesTest extends PreferenceInjectorTestCase {
    private final TestParams<?> params;

    public record TestParams<T extends Record & LongPreferencesTest.WithLong>(
        Class<T> recordClass, WithLong.Factory<T> recordFactory) {}

    public LongPreferencesTest(String testName, TestParams<?> params) {
      this.params = params;
    }

    @Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
      return Arrays.asList(
          new Object[][] {
            {
              "boolean components",
              new LongPreferencesTest.TestParams<>(ContainsLong.class, ContainsLong::new)
            },
            {
              "BooleanSupplier components",
              new LongPreferencesTest.TestParams<>(
                  ContainsLongSupplier.class, ContainsLongSupplier::factory)
            },
            {
              "Supplier<Boolean> components",
              new LongPreferencesTest.TestParams<>(
                  ContainsSupplierOfLong.class, ContainsSupplierOfLong::factory)
            }
          });
    }

    /**
     * Common interface for test record classes that contain a long value. The component name must
     * be "value".
     */
    interface WithLong {
      long longValue();

      @FunctionalInterface
      interface Factory<T extends Record & WithLong> {
        T create(long value);
      }
    }

    /** Test record for testing classes that contain long fields. */
    record ContainsLong(long value) implements WithLong {

      @Override
      public long longValue() {
        return value;
      }
    }

    /** Test record for testing classes that contain {@code LongSupplier} fields. */
    record ContainsLongSupplier(LongSupplier value) implements WithLong {

      static ContainsLongSupplier factory(long value) {
        return new ContainsLongSupplier(() -> value);
      }

      @Override
      public long longValue() {
        return value.getAsLong();
      }
    }

    /** Test record for testing classes that contain {@code Supplier<Long>} fields. */
    record ContainsSupplierOfLong(Supplier<Long> value) implements WithLong {

      static ContainsSupplierOfLong factory(long value) {
        return new ContainsSupplierOfLong(() -> value);
      }

      @Override
      public long longValue() {
        return value.get();
      }
    }

    @Test
    public void withoutExistingPreferences() {
      String key = keyForFieldName(params.recordClass, "value");

      // Arrange
      var recordWithDefaults = params.recordFactory.create(42);

      // Act
      var recordWithPreferences = injector.injectPreferences(recordWithDefaults);

      // Assert
      assertThat(recordWithPreferences.longValue()).isEqualTo(42);

      assertThat(preferenceKeys()).containsExactly(key);
      assertThat(Preferences.getLong(key, -2)).isEqualTo(42);

      // Arrange: Update preferences
      Preferences.setLong(key, 99);
      var preferenceValues = preferenceValues();

      // Act
      if (params.recordClass.equals(ContainsLong.class)) {
        // The record is immutable and contains primitive values, so to "see" the new values
        // we need to create a new record from the preferences.
        recordWithPreferences = injector.injectPreferences(recordWithDefaults);
      }

      // Assert
      assertThat(recordWithPreferences.longValue()).isEqualTo(99);
      assertThat(preferenceKeys()).containsExactly(key);
      assertThat(Preferences.getLong(key, -2)).isEqualTo(99);
      assertHasNoChangesSince(preferenceValues);
    }

    @Test
    public void withExistingPreferences() {
      String key = keyForFieldName(params.recordClass, "value");

      // Arrange
      Preferences.initLong(key, 42);
      var recordWithDefaults = params.recordFactory.create(-1);

      // Act
      var recordWithPreferences = injector.injectPreferences(recordWithDefaults);

      // Assert
      assertThat(recordWithPreferences.longValue()).isEqualTo(42);

      assertThat(preferenceKeys()).containsExactly(key);
      assertThat(Preferences.getLong(key, -2)).isEqualTo(42);

      // Arrange: Update preferences
      Preferences.setLong(key, 99);
      var preferenceValues = preferenceValues();

      // Act
      if (params.recordClass.equals(ContainsLong.class)) {
        // The record is immutable and contains primitive values, so to "see" the new values
        // we need to create a new record from the preferences.
        recordWithPreferences = injector.injectPreferences(recordWithDefaults);
      }

      // Assert
      assertThat(recordWithPreferences.longValue()).isEqualTo(99);
      assertThat(preferenceKeys()).containsExactly(key);
      assertThat(Preferences.getLong(key, -2)).isEqualTo(99);
      assertHasNoChangesSince(preferenceValues);
    }
  }

  /** Base class for all nested classes of {@link PreferenceInjectorTest}. */
  private abstract static class PreferenceInjectorTestCase {
    PreferenceInjector injector;

    @Rule public final IsolatedPreferences isolatedPreferences = new IsolatedPreferences();
    @Rule public final ErrorCollector errorCollector = new ErrorCollector();

    @Before
    public void createInjector() {
      String removePrefix = getClass().getCanonicalName() + ".";
      injector = new PreferenceInjector(removePrefix);
      injector.throwExceptions = true;
      injector.errorReporter =
          message ->
              errorCollector.addError(
                  new AssertionError("Unexpected warning: \"" + message + "\""));
    }

    protected final String keyForFieldName(Class<? extends Record> recordClass, String fieldName) {
      return recordClass.getSimpleName() + "." + fieldName;
    }

    protected final void assertHasNoChangesSince(Map<String, Object> previousValues) {
      var preferenceValues = preferenceValues();
      assertWithMessage("Unexpected no changes to preference values")
          .that(preferenceValues)
          .isEqualTo(previousValues);
    }

    protected final Map<String, Object> preferenceValues() {
      NetworkTable table = isolatedPreferences.getTable();
      return preferenceKeys().stream()
          .collect(toMap(Function.identity(), key -> table.getEntry(key).getValue().getValue()));
    }

    protected final Set<String> preferenceKeys() {
      // Preferences adds a ".type" key; we filter it out here.
      return Preferences.getKeys().stream().filter(key -> !key.startsWith(".")).collect(toSet());
    }
  }
}
