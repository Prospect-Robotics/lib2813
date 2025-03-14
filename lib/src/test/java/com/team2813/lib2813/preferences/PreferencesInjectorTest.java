package com.team2813.lib2813.preferences;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.wpilibj.Preferences;
import java.util.Map;
import java.util.Set;
import java.util.function.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/** Tests for {@link PreferencesInjector}. */
@RunWith(Enclosed.class)
public final class PreferencesInjectorTest {
  private static final double EPSILON = 0.001;

  @RunWith(Parameterized.class)
  public static class BooleanPreferencesTest extends PreferencesInjectorTestCase {
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

  public static class IntPreferencesTest extends PreferencesInjectorTestCase {
    final String intValueKey = keyForFieldName(RecordWithInts.class, "intValue");
    final String intSupplierKey = keyForFieldName(RecordWithInts.class, "intSupplier");
    final String supplierIntKey = keyForFieldName(RecordWithInts.class, "supplierInt");

    /** Test record for testing classes that contain int fields. */
    record RecordWithInts(int intValue, IntSupplier intSupplier, Supplier<Integer> supplierInt) {

      RecordWithInts(int intValue, int intSupplierValue, int supplierIntValue) {
        this(intValue, () -> intSupplierValue, () -> supplierIntValue);
      }
    }

    @Test
    public void withoutExistingPreferences() {
      // Arrange
      var recordWithDefaults = new RecordWithInts(1, 2, 3);

      // Act
      var recordWithPreferences = injector.injectPreferences(recordWithDefaults);

      // Assert: Preferences injected
      assertThat(recordWithPreferences.intValue()).isEqualTo(1);
      assertThat(recordWithPreferences.intSupplier.getAsInt()).isEqualTo(2);
      assertThat(recordWithPreferences.supplierInt().get()).isEqualTo(Integer.valueOf(3));

      // Assert: Default values set
      assertThat(preferenceKeys()).containsExactly(intValueKey, intSupplierKey, supplierIntKey);
      assertThat(Preferences.getInt(intValueKey, -1)).isEqualTo(1);
      assertThat(Preferences.getInt(intSupplierKey, -1)).isEqualTo(2);
      assertThat(Preferences.getInt(supplierIntKey, -1)).isEqualTo(3);

      // Arrange: Update preferences
      Preferences.setInt(intValueKey, 101);
      Preferences.setInt(intSupplierKey, 102);
      Preferences.setInt(supplierIntKey, 103);
      var preferenceValues = preferenceValues();

      // Act
      var newRecordWithPreferences = injector.injectPreferences(recordWithDefaults);

      // Assert: Preferences injected
      assertThat(newRecordWithPreferences.intValue()).isEqualTo(101);
      assertThat(recordWithPreferences.intSupplier.getAsInt()).isEqualTo(102);
      assertThat(newRecordWithPreferences.intSupplier.getAsInt()).isEqualTo(102);
      assertThat(recordWithPreferences.supplierInt().get()).isEqualTo(Integer.valueOf(103));
      assertThat(newRecordWithPreferences.supplierInt().get()).isEqualTo(Integer.valueOf(103));
      assertHasNoChangesSince(preferenceValues);
    }

    @Test
    public void withExistingPreferences() {
      // Arrange
      Preferences.setInt(intValueKey, 201);
      Preferences.setInt(intSupplierKey, 202);
      Preferences.setInt(supplierIntKey, 203);
      var preferenceValues = preferenceValues();
      var recordWithDefaults = new RecordWithInts(-1, -2, -3);

      // Act
      var recordWithPreferences = injector.injectPreferences(recordWithDefaults);

      // Assert: Preferences injected
      assertThat(recordWithPreferences.intValue()).isEqualTo(201);
      assertThat(recordWithPreferences.intSupplier.getAsInt()).isEqualTo(202);
      assertThat(recordWithPreferences.supplierInt().get()).isEqualTo(Integer.valueOf(203));
      assertHasNoChangesSince(preferenceValues);

      // Arrange: Update preferences
      Preferences.setInt(intValueKey, 301);
      Preferences.setInt(intSupplierKey, 302);
      Preferences.setInt(supplierIntKey, 303);
      preferenceValues = preferenceValues();

      // Act
      var newRecordWithPreferences = injector.injectPreferences(recordWithDefaults);

      // Assert: Preferences injected
      assertThat(newRecordWithPreferences.intValue()).isEqualTo(301);
      assertThat(recordWithPreferences.intSupplier.getAsInt()).isEqualTo(302);
      assertThat(newRecordWithPreferences.intSupplier.getAsInt()).isEqualTo(302);
      assertThat(recordWithPreferences.supplierInt().get()).isEqualTo(Integer.valueOf(303));
      assertThat(newRecordWithPreferences.supplierInt().get()).isEqualTo(Integer.valueOf(303));
      assertHasNoChangesSince(preferenceValues);
    }
  }

  public static class LongPreferencesTest extends PreferencesInjectorTestCase {
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

  public static class DoublePreferencesTest extends PreferencesInjectorTestCase {
    final String doubleValueKey = keyForFieldName(RecordWithDoubles.class, "doubleValue");
    final String doubleSupplierKey = keyForFieldName(RecordWithDoubles.class, "doubleSupplier");
    final String supplierDoubleKey = keyForFieldName(RecordWithDoubles.class, "supplierDouble");

    /** Test record for testing classes that contain double fields. */
    record RecordWithDoubles(
        double doubleValue, DoubleSupplier doubleSupplier, Supplier<Double> supplierDouble) {

      RecordWithDoubles(
          double doubleValue, double doubleSupplierValue, double supplierDoubleValue) {
        this(doubleValue, () -> doubleSupplierValue, () -> supplierDoubleValue);
      }
    }

    @Test
    public void withoutExistingPreferences() {
      // Arrange
      var recordWithDefaults = new RecordWithDoubles(3.14159, 2.71828, 6.28318);

      // Act
      var recordWithPreferences = injector.injectPreferences(recordWithDefaults);

      // Assert: Preferences injected
      assertThat(recordWithPreferences.doubleValue()).isWithin(EPSILON).of(3.14159);
      assertThat(recordWithPreferences.doubleSupplier.getAsDouble()).isWithin(EPSILON).of(2.71828);
      assertThat(recordWithPreferences.supplierDouble().get()).isWithin(EPSILON).of(6.28318);

      // Assert: Default values set
      assertThat(preferenceKeys())
          .containsExactly(doubleValueKey, doubleSupplierKey, supplierDoubleKey);
      assertThat(Preferences.getDouble(doubleValueKey, -1)).isWithin(EPSILON).of(3.14159);
      assertThat(Preferences.getDouble(doubleSupplierKey, -1)).isWithin(EPSILON).of(2.71828);
      assertThat(Preferences.getDouble(supplierDoubleKey, -1)).isWithin(EPSILON).of(6.28318);

      // Arrange: Update preferences
      Preferences.setDouble(doubleValueKey, 1.23);
      Preferences.setDouble(doubleSupplierKey, 4.56);
      Preferences.setDouble(supplierDoubleKey, 7.89);
      var preferenceValues = preferenceValues();

      // Act
      var newRecordWithPreferences = injector.injectPreferences(recordWithDefaults);

      // Assert: Preferences injected
      assertThat(newRecordWithPreferences.doubleValue()).isWithin(EPSILON).of(1.23);
      assertThat(recordWithPreferences.doubleSupplier.getAsDouble()).isWithin(EPSILON).of(4.56);
      assertThat(newRecordWithPreferences.doubleSupplier.getAsDouble()).isWithin(EPSILON).of(4.56);
      assertThat(recordWithPreferences.supplierDouble().get()).isWithin(EPSILON).of(7.89);
      assertThat(newRecordWithPreferences.supplierDouble().get()).isWithin(EPSILON).of(7.89);
      assertHasNoChangesSince(preferenceValues);
    }

    @Test
    public void withExistingPreferences() {
      // Arrange
      Preferences.setDouble(doubleValueKey, 3.1415990);
      Preferences.setDouble(doubleSupplierKey, 2.71828);
      Preferences.setDouble(supplierDoubleKey, 6.28318);
      var preferenceValues = preferenceValues();
      var recordWithDefaults = new RecordWithDoubles(-1, -2, -3);

      // Act
      var recordWithPreferences = injector.injectPreferences(recordWithDefaults);

      // Assert: Preferences injected
      assertThat(Preferences.getDouble(doubleValueKey, -1)).isWithin(EPSILON).of(3.14159);
      assertThat(Preferences.getDouble(doubleSupplierKey, -1)).isWithin(EPSILON).of(2.71828);
      assertThat(Preferences.getDouble(supplierDoubleKey, -1)).isWithin(EPSILON).of(6.28318);
      assertHasNoChangesSince(preferenceValues);

      // Arrange: Update preferences
      Preferences.setDouble(doubleValueKey, 3.21);
      Preferences.setDouble(doubleSupplierKey, 6.54);
      Preferences.setDouble(supplierDoubleKey, 9.87);
      preferenceValues = preferenceValues();

      // Act
      var newRecordWithPreferences = injector.injectPreferences(recordWithDefaults);

      // Assert: Preferences injected
      assertThat(newRecordWithPreferences.doubleValue()).isWithin(EPSILON).of(3.21);
      assertThat(recordWithPreferences.doubleSupplier.getAsDouble()).isWithin(EPSILON).of(6.54);
      assertThat(newRecordWithPreferences.doubleSupplier.getAsDouble()).isWithin(EPSILON).of(6.54);
      assertThat(recordWithPreferences.supplierDouble().get()).isWithin(EPSILON).of(9.87);
      assertThat(newRecordWithPreferences.supplierDouble().get()).isWithin(EPSILON).of(9.87);
      assertHasNoChangesSince(preferenceValues);
    }
  }

  public static class StringPreferencesTest extends PreferencesInjectorTestCase {
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

  public static class RecordPreferencesTest extends PreferencesInjectorTestCase {
    final String recordValueKey = keyForFieldName(RecordWithRecords.class, "recordValue");
    final String longValueKey = recordValueKey + ".longValue";
    final String stringValueKey = recordValueKey + ".stringValue";

    record RecordWithPrimitives(long longValue, String stringValue) {}

    record RecordWithRecords(RecordWithPrimitives recordValue) {}

    @Test
    public void withoutExistingPreferences() {
      // Arrange
      var recordWithDefaults = new RecordWithRecords(new RecordWithPrimitives(42, "The Answer"));

      // Act
      var recordWithPreferences = injector.injectPreferences(recordWithDefaults);

      // Assert: Preferences injected
      assertThat(recordWithPreferences.recordValue.stringValue()).isEqualTo("The Answer");
      assertThat(recordWithPreferences.recordValue.longValue()).isEqualTo(42);

      // Assert: Default values set
      assertThat(preferenceKeys()).containsExactly(stringValueKey, longValueKey);
      assertThat(Preferences.getString(stringValueKey, "")).isEqualTo("The Answer");
      assertThat(Preferences.getLong(longValueKey, -1)).isEqualTo(42);

      // Arrange: Update preferences
      Preferences.setString(stringValueKey, "Gear Heads");
      Preferences.setLong(longValueKey, 2813);
      var preferenceValues = preferenceValues();

      // Act
      var newRecordWithPreferences = injector.injectPreferences(recordWithDefaults);

      // Assert: Preferences injected
      assertThat(newRecordWithPreferences.recordValue.longValue()).isEqualTo(2813);
      assertThat(newRecordWithPreferences.recordValue.stringValue()).isEqualTo("Gear Heads");
      ;
      assertHasNoChangesSince(preferenceValues);
    }

    @Test
    public void withExistingPreferences() {
      // Arrange
      Preferences.initString(stringValueKey, "Agent");
      Preferences.initLong(longValueKey, 99);
      var preferenceValues = preferenceValues();
      var recordWithDefaults = new RecordWithRecords(new RecordWithPrimitives(-1, ""));

      // Act
      var recordWithPreferences = injector.injectPreferences(recordWithDefaults);

      // Assert: Preferences injected
      assertThat(recordWithPreferences.recordValue.stringValue()).isEqualTo("Agent");
      assertThat(recordWithPreferences.recordValue.longValue()).isEqualTo(99);
      assertHasNoChangesSince(preferenceValues);

      // Arrange: Update preferences
      Preferences.setString(stringValueKey, "Gear Heads");
      Preferences.setLong(longValueKey, 2813);
      preferenceValues = preferenceValues();

      // Act
      var newRecordWithPreferences = injector.injectPreferences(recordWithDefaults);

      // Assert: Preferences injected
      assertThat(newRecordWithPreferences.recordValue.longValue()).isEqualTo(2813);
      assertThat(newRecordWithPreferences.recordValue.stringValue()).isEqualTo("Gear Heads");
      ;
      assertHasNoChangesSince(preferenceValues);
    }
  }

  /** Base class for all nested classes of {@link PreferencesInjectorTest}. */
  private abstract static class PreferencesInjectorTestCase {
    PreferencesInjector injector;

    @Rule public final IsolatedPreferences isolatedPreferences = new IsolatedPreferences();
    @Rule public final ErrorCollector errorCollector = new ErrorCollector();

    @Before
    public void createInjector() {
      String removePrefix = getClass().getCanonicalName() + ".";
      injector = new PreferencesInjector(removePrefix);
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
