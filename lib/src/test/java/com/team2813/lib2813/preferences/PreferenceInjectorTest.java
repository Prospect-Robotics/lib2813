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
    final boolean defaultValue;
    final String booleanValueKey = keyForFieldName(RecordWithBooleans.class, "booleanValue");
    final String booleanSupplierKey = keyForFieldName(RecordWithBooleans.class, "booleanSupplier");
    final String booleanBooleanKey = keyForFieldName(RecordWithBooleans.class, "supplierBoolean");
    final Set<String> allKeys = Set.of(booleanValueKey, booleanSupplierKey, booleanBooleanKey);

    @Parameters(name = "defaultValue={0}")
    public static Object[] data() {
      return new Object[] {true, false};
    }

    public BooleanPreferencesTest(boolean defaultValue) {
      this.defaultValue = defaultValue;
    }

    /** Test record for testing classes that contain boolean fields. */
    record RecordWithBooleans(
        boolean booleanValue, BooleanSupplier booleanSupplier, Supplier<Boolean> supplierBoolean) {

      public RecordWithBooleans(boolean defaultValue) {
        this(defaultValue, () -> defaultValue, () -> Boolean.valueOf(defaultValue));
      }
    }

    @Test
    public void withoutExistingPreferences() {
      // Arrange
      var recordWithDefaults = new RecordWithBooleans(defaultValue);

      // Act
      var recordWithPreferences = injector.injectPreferences(recordWithDefaults);

      // Assert: Preferences injected
      assertThat(recordWithPreferences.booleanValue()).isEqualTo(defaultValue);
      assertThat(recordWithPreferences.booleanSupplier().getAsBoolean()).isEqualTo(defaultValue);
      assertThat(recordWithPreferences.supplierBoolean().get())
          .isEqualTo(Boolean.valueOf(defaultValue));

      // Assert: Default values set
      assertThat(preferenceKeys())
          .containsExactly(booleanValueKey, booleanSupplierKey, booleanBooleanKey);
      for (String key : allKeys) {
        assertThat(Preferences.getBoolean(key, !defaultValue)).isEqualTo(defaultValue);
      }

      // Arrange: Update preferences
      boolean configuredValue = !defaultValue;
      for (String key : allKeys) {
        Preferences.setBoolean(key, configuredValue);
      }
      var preferenceValues = preferenceValues();

      // Act
      var newRecordWithPreferences = injector.injectPreferences(recordWithDefaults);

      // Assert: Preferences injected
      assertThat(newRecordWithPreferences.booleanValue()).isEqualTo(configuredValue);
      assertThat(newRecordWithPreferences.booleanSupplier().getAsBoolean())
          .isEqualTo(configuredValue);
      assertThat(recordWithPreferences.booleanSupplier().getAsBoolean()).isEqualTo(configuredValue);
      assertThat(newRecordWithPreferences.supplierBoolean().get()).isEqualTo(configuredValue);
      assertThat(recordWithPreferences.supplierBoolean().get()).isEqualTo(configuredValue);
      assertHasNoChangesSince(preferenceValues);
    }

    @Test
    public void withExistingPreferences() {
      // Arrange
      var recordWithDefaults = new RecordWithBooleans(defaultValue);
      boolean configuredValue = !defaultValue;
      for (String key : allKeys) {
        Preferences.setBoolean(key, configuredValue);
      }
      var preferenceValues = preferenceValues();

      // Act
      var recordWithPreferences = injector.injectPreferences(recordWithDefaults);

      // Assert: Preferences injected
      assertThat(recordWithPreferences.booleanValue()).isEqualTo(configuredValue);
      assertThat(recordWithPreferences.booleanSupplier().getAsBoolean()).isEqualTo(configuredValue);
      assertThat(recordWithPreferences.supplierBoolean().get())
          .isEqualTo(Boolean.valueOf(configuredValue));
      assertHasNoChangesSince(preferenceValues);

      // Arrange: Update preferences
      configuredValue = defaultValue;
      for (String key : allKeys) {
        Preferences.setBoolean(key, configuredValue);
      }
      preferenceValues = preferenceValues();

      // Act
      var newRecordWithPreferences = injector.injectPreferences(recordWithDefaults);

      // Assert: Preferences injected
      assertThat(newRecordWithPreferences.booleanValue()).isEqualTo(configuredValue);
      assertThat(newRecordWithPreferences.booleanSupplier().getAsBoolean())
          .isEqualTo(configuredValue);
      assertThat(recordWithPreferences.booleanSupplier().getAsBoolean()).isEqualTo(configuredValue);
      assertThat(newRecordWithPreferences.supplierBoolean().get()).isEqualTo(configuredValue);
      assertThat(recordWithPreferences.supplierBoolean().get()).isEqualTo(configuredValue);
      assertHasNoChangesSince(preferenceValues);
    }
  }

  public static class LongPreferencesTest extends PreferenceInjectorTestCase {
    final String longValueKey = keyForFieldName(RecordWithLongs.class, "longValue");
    final String longSupplierKey = keyForFieldName(RecordWithLongs.class, "longSupplier");
    final String supplierLongKey = keyForFieldName(RecordWithLongs.class, "supplierLong");

    /** Test record for testing classes that contain long fields. */
    record RecordWithLongs(long longValue, LongSupplier longSupplier, Supplier<Long> supplierLong) {

      RecordWithLongs(long longValue, long longSupplierValue, long supplierLongValue) {
        this(longValue, () -> longSupplierValue, () -> supplierLongValue);
      }
    }

    @Test
    public void withoutExistingPreferences() {
      // Arrange
      var recordWithDefaults = new RecordWithLongs(1, 2, 3);

      // Act
      var recordWithPreferences = injector.injectPreferences(recordWithDefaults);

      // Assert: Preferences injected
      assertThat(recordWithPreferences.longValue()).isEqualTo(1);
      assertThat(recordWithPreferences.longSupplier.getAsLong()).isEqualTo(2);
      assertThat(recordWithPreferences.supplierLong().get()).isEqualTo(Long.valueOf(3));

      // Assert: Default values set
      assertThat(preferenceKeys()).containsExactly(longValueKey, longSupplierKey, supplierLongKey);
      assertThat(Preferences.getLong(longValueKey, -1)).isEqualTo(1);
      assertThat(Preferences.getLong(longSupplierKey, -1)).isEqualTo(2);
      assertThat(Preferences.getLong(supplierLongKey, -1)).isEqualTo(3);

      // Arrange: Update preferences
      Preferences.setLong(longValueKey, 101);
      Preferences.setLong(longSupplierKey, 102);
      Preferences.setLong(supplierLongKey, 103);
      var preferenceValues = preferenceValues();

      // Act
      var newRecordWithPreferences = injector.injectPreferences(recordWithDefaults);

      // Assert: Preferences injected
      assertThat(newRecordWithPreferences.longValue()).isEqualTo(101);
      assertThat(recordWithPreferences.longSupplier.getAsLong()).isEqualTo(102);
      assertThat(newRecordWithPreferences.longSupplier.getAsLong()).isEqualTo(102);
      assertThat(recordWithPreferences.supplierLong().get()).isEqualTo(Long.valueOf(103));
      assertThat(newRecordWithPreferences.supplierLong().get()).isEqualTo(Long.valueOf(103));
      assertHasNoChangesSince(preferenceValues);
    }

    @Test
    public void withExistingPreferences() {
      // Arrange
      Preferences.setLong(longValueKey, 201);
      Preferences.setLong(longSupplierKey, 202);
      Preferences.setLong(supplierLongKey, 203);
      var preferenceValues = preferenceValues();
      var recordWithDefaults = new RecordWithLongs(-1, -2, -3);

      // Act
      var recordWithPreferences = injector.injectPreferences(recordWithDefaults);

      // Assert: Preferences injected
      assertThat(recordWithPreferences.longValue()).isEqualTo(201);
      assertThat(recordWithPreferences.longSupplier.getAsLong()).isEqualTo(202);
      assertThat(recordWithPreferences.supplierLong().get()).isEqualTo(Long.valueOf(203));
      assertHasNoChangesSince(preferenceValues);

      // Arrange: Update preferences
      Preferences.setLong(longValueKey, 301);
      Preferences.setLong(longSupplierKey, 302);
      Preferences.setLong(supplierLongKey, 303);
      preferenceValues = preferenceValues();

      // Act
      var newRecordWithPreferences = injector.injectPreferences(recordWithDefaults);

      // Assert: Preferences injected
      assertThat(newRecordWithPreferences.longValue()).isEqualTo(301);
      assertThat(recordWithPreferences.longSupplier.getAsLong()).isEqualTo(302);
      assertThat(newRecordWithPreferences.longSupplier.getAsLong()).isEqualTo(302);
      assertThat(recordWithPreferences.supplierLong().get()).isEqualTo(Long.valueOf(303));
      assertThat(newRecordWithPreferences.supplierLong().get()).isEqualTo(Long.valueOf(303));
      assertHasNoChangesSince(preferenceValues);
    }
  }

  public static class StringPreferencesTest extends PreferenceInjectorTestCase {
    final String stringValueKey = keyForFieldName(RecordWithStrings.class, "stringValue");
    final String stringSupplierKey = keyForFieldName(RecordWithStrings.class, "stringSupplier");

    record RecordWithStrings(String stringValue, Supplier<String> stringSupplier) {

      RecordWithStrings(String stringValue, String stringSupplierValue) {
        this(stringValue, () -> stringSupplierValue);
      }
    }

    @Test
    public void withoutExistingPreferences() {
      // Arrange
      var recordWithDefaults = new RecordWithStrings("chicken", "bus");

      // Act
      var recordWithPreferences = injector.injectPreferences(recordWithDefaults);

      // Assert: Preferences injected
      assertThat(recordWithPreferences.stringValue()).isEqualTo("chicken");
      assertThat(recordWithPreferences.stringSupplier().get()).isEqualTo("bus");

      // Assert: Default values set
      assertThat(preferenceKeys()).containsExactly(stringValueKey, stringSupplierKey);
      assertThat(Preferences.getString(stringValueKey, "")).isEqualTo("chicken");
      assertThat(Preferences.getString(stringSupplierKey, "")).isEqualTo("bus");

      // Arrange: Update preferences
      Preferences.setString(stringValueKey, "Gear");
      Preferences.setString(stringSupplierKey, "Heads");
      var preferenceValues = preferenceValues();

      // Act
      var newRecordWithPreferences = injector.injectPreferences(recordWithDefaults);

      // Assert: Preferences injected
      assertThat(newRecordWithPreferences.stringValue()).isEqualTo("Gear");
      assertThat(recordWithPreferences.stringSupplier.get()).isEqualTo("Heads");
      assertThat(newRecordWithPreferences.stringSupplier.get()).isEqualTo("Heads");
      assertHasNoChangesSince(preferenceValues);
    }

    @Test
    public void withExistingPreferences() {
      // Arrange
      Preferences.initString(stringValueKey, "chicken");
      Preferences.initString(stringSupplierKey, "bus");
      var preferenceValues = preferenceValues();
      var recordWithDefaults = new RecordWithStrings("default1", "default2");

      // Act
      var recordWithPreferences = injector.injectPreferences(recordWithDefaults);

      // Assert: Preferences injected
      assertThat(recordWithPreferences.stringValue()).isEqualTo("chicken");
      assertThat(recordWithPreferences.stringSupplier().get()).isEqualTo("bus");
      assertHasNoChangesSince(preferenceValues);

      // Arrange: Update preferences
      Preferences.setString(stringValueKey, "Gear");
      Preferences.setString(stringSupplierKey, "Heads");
      preferenceValues = preferenceValues();

      // Act
      var newRecordWithPreferences = injector.injectPreferences(recordWithDefaults);

      // Assert: Preferences injected
      assertThat(newRecordWithPreferences.stringValue()).isEqualTo("Gear");
      assertThat(recordWithPreferences.stringSupplier().get()).isEqualTo("Heads");
      assertThat(newRecordWithPreferences.stringSupplier().get()).isEqualTo("Heads");
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

    protected static String keyForFieldName(Class<? extends Record> recordClass, String fieldName) {
      return recordClass.getSimpleName() + "." + fieldName;
    }

    protected final void assertHasNoChangesSince(Map<String, Object> previousValues) {
      var preferenceValues = preferenceValues();
      assertWithMessage("Unexpected no changes to preference values")
          .that(preferenceValues)
          .isEqualTo(previousValues);
    }

    protected final Map<String, Object> preferenceValues() {
      NetworkTable table = isolatedPreferences.getPreferencesTable();
      return preferenceKeys().stream()
          .collect(toMap(Function.identity(), key -> table.getEntry(key).getValue().getValue()));
    }

    protected final Set<String> preferenceKeys() {
      // Preferences adds a ".type" key; we filter it out here.
      return Preferences.getKeys().stream().filter(key -> !key.startsWith(".")).collect(toSet());
    }
  }
}
