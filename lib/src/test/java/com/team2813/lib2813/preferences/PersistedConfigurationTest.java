package com.team2813.lib2813.preferences;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static java.util.stream.Collectors.toMap;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.Topic;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.Preferences;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/** Tests for {@link PersistedConfiguration}. */
@RunWith(Enclosed.class)
public final class PersistedConfigurationTest {
  private static final double EPSILON = 0.001;

  /** Base class for all nested classes of {@link PersistedConfigurationTest}. */
  abstract static class PreferencesRegistryTestCase<T extends Record> {
    private final String preferenceName;
    private final Class<T> recordClass;

    @Rule public final IsolatedPreferences isolatedPreferences = new IsolatedPreferences();
    @Rule public final ErrorCollector errorCollector = new ErrorCollector();

    protected PreferencesRegistryTestCase(String preferenceName, Class<T> recordClass) {
      this.preferenceName = preferenceName;
      this.recordClass = recordClass;
    }

    @Before
    public final void setTestGlobals() {
      PersistedConfiguration.throwExceptions = true;
      PersistedConfiguration.errorReporter =
          message ->
              errorCollector.addError(
                  new AssertionError("Unexpected warning: \"" + message + "\""));
    }

    @After
    public final void resetTestGlobals() {
      PersistedConfiguration.throwExceptions = false;
      PersistedConfiguration.errorReporter = DataLogManager::log;
    }

    protected enum ValuesKind {
      INITIAL_VALUES,
      UPDATED_VALUES
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
      NetworkTable table = isolatedPreferences.getPreferencesTable();
      Set<String> keys = new HashSet<>();
      collectKeys(table, keys);
      return Set.copyOf(keys);
    }

    private void collectKeys(NetworkTable table, Set<String> result) {
      for (String key : table.getSubTables()) {
        collectKeys(table.getSubTable(key), result);
      }
      for (Topic topic : table.getTopics()) {
        String topicName = topic.getName().substring(13); // Remove "/Preferences/" prefix

        // Preferences adds a ".type" key; we filter it out here.
        if (!topicName.startsWith(".") && !topicName.contains("/.")) {
          result.add(topicName);
        }
      }
    }

    /**
     * Creates an instance of the record class with all values set to default values.
     *
     * <p>The values for the record components should all be unique, and none of them should be the
     * Java default value for the type.
     */
    protected abstract T createRecordWithConfiguredDefaults();

    /**
     * Verifies that the given record has the same values as one created by {@link
     * #createRecordWithConfiguredDefaults()}.
     */
    protected abstract void assertHasConfiguredDefaults(T record);

    /**
     * Verifies that preferences have the same values as one created by {@link
     * #createRecordWithConfiguredDefaults()}.
     */
    protected abstract void assertPreferencesHaveConfiguredDefaults();

    /** Verifies that all values of the given record equal the Java default value for the type. */
    protected abstract void assertHasJavaDefaults(T record);

    /** Verifies that all preferences have values equal to the Java default value of the type. */
    protected abstract void assertPreferencesHaveJavaDefaults();

    /** Updates the values of all preferences. */
    protected abstract void updatePreferenceValues(ValuesKind kind);

    /**
     * Verifies that all values of the given record equal the values returned by {@link
     * #updatePreferenceValues(ValuesKind)}.
     */
    protected abstract void assertHasUpdatedValues(ValuesKind kind, T record);

    /**
     * Verifies that all suppliers contained in the given record have values equal the values
     * returned by {@link #updatePreferenceValues(ValuesKind)}.
     */
    protected abstract void assertSuppliersHaveUpdatedValues(T record);

    @Test
    public void withoutExistingPreferences() {
      // Arrange
      T recordWithDefaults = createRecordWithConfiguredDefaults();

      // Act
      T recordWithPreferences =
          PersistedConfiguration.fromPreferences(preferenceName, recordWithDefaults);

      // Assert: Preferences injected
      assertHasConfiguredDefaults(recordWithPreferences);
      // Ensure the suppliers return the same value if called multiple times.
      assertHasConfiguredDefaults(recordWithPreferences);

      // Assert: Default values set
      assertPreferencesHaveConfiguredDefaults();

      // Arrange: Update preferences
      updatePreferenceValues(ValuesKind.UPDATED_VALUES);
      var preferenceValues = preferenceValues();

      // Act
      T newRecordWithPreferences =
          PersistedConfiguration.fromPreferences(preferenceName, recordWithDefaults);

      // Assert: Preferences injected
      assertHasUpdatedValues(ValuesKind.UPDATED_VALUES, newRecordWithPreferences);
      assertSuppliersHaveUpdatedValues(recordWithPreferences);
      assertHasNoChangesSince(preferenceValues);
      // Ensure the suppliers return the same value if called multiple times.
      assertHasUpdatedValues(ValuesKind.UPDATED_VALUES, newRecordWithPreferences);
    }

    @Test
    public void withoutExistingPreferences_defaultsNotSpecified() {
      // Act
      var recordWithPreferences =
          PersistedConfiguration.fromPreferences(preferenceName, recordClass);

      // Assert: Preferences injected
      assertHasJavaDefaults(recordWithPreferences);

      // Assert: Default values set
      assertPreferencesHaveJavaDefaults();
      // Ensure the suppliers return the same value if called multiple times.
      assertPreferencesHaveJavaDefaults();

      // Arrange: Update preferences
      updatePreferenceValues(ValuesKind.UPDATED_VALUES);
      var preferenceValues = preferenceValues();

      // Act
      var newRecordWithPreferences =
          PersistedConfiguration.fromPreferences(preferenceName, recordClass);

      // Assert: Preferences injected
      assertHasUpdatedValues(ValuesKind.UPDATED_VALUES, newRecordWithPreferences);
      assertSuppliersHaveUpdatedValues(recordWithPreferences);
      assertHasNoChangesSince(preferenceValues);
      // Ensure the suppliers return the same value if called multiple times.
      assertHasUpdatedValues(ValuesKind.UPDATED_VALUES, newRecordWithPreferences);
    }

    @Test
    public void withExistingPreferences() {
      // Arrange
      updatePreferenceValues(ValuesKind.INITIAL_VALUES);
      var preferenceValues = preferenceValues();
      T recordWithDefaults = createRecordWithConfiguredDefaults();

      // Act
      T recordWithPreferences =
          PersistedConfiguration.fromPreferences(preferenceName, recordWithDefaults);

      // Assert: Preferences injected
      assertHasUpdatedValues(ValuesKind.INITIAL_VALUES, recordWithPreferences);
      assertHasNoChangesSince(preferenceValues);
      // Ensure the suppliers return the same value if called multiple times.
      assertHasUpdatedValues(ValuesKind.INITIAL_VALUES, recordWithPreferences);

      // Arrange: Update preferences
      updatePreferenceValues(ValuesKind.UPDATED_VALUES);
      preferenceValues = preferenceValues();

      // Act
      var newRecordWithPreferences =
          PersistedConfiguration.fromPreferences(preferenceName, recordWithDefaults);

      // Assert: Preferences injected
      assertHasUpdatedValues(ValuesKind.UPDATED_VALUES, newRecordWithPreferences);
      assertSuppliersHaveUpdatedValues(recordWithPreferences);
      assertHasNoChangesSince(preferenceValues);
      // Ensure the suppliers return the same value if called multiple times.
      assertHasUpdatedValues(ValuesKind.UPDATED_VALUES, newRecordWithPreferences);
    }

    @Test
    public void withExistingPreferences_defaultsNotSpecified() {
      // Arrange
      updatePreferenceValues(ValuesKind.INITIAL_VALUES);
      var preferenceValues = preferenceValues();

      // Act
      var recordWithPreferences =
          PersistedConfiguration.fromPreferences(preferenceName, recordClass);

      // Assert: Preferences injected
      assertHasUpdatedValues(ValuesKind.INITIAL_VALUES, recordWithPreferences);
      assertHasNoChangesSince(preferenceValues);
      // Ensure the suppliers return the same value if called multiple times.
      assertHasUpdatedValues(ValuesKind.INITIAL_VALUES, recordWithPreferences);

      // Arrange: Update preferences
      updatePreferenceValues(ValuesKind.UPDATED_VALUES);
      preferenceValues = preferenceValues();

      // Act
      var newRecordWithPreferences =
          PersistedConfiguration.fromPreferences(preferenceName, recordClass);

      // Assert: Preferences injected
      assertHasUpdatedValues(ValuesKind.UPDATED_VALUES, newRecordWithPreferences);
      assertSuppliersHaveUpdatedValues(recordWithPreferences);
      assertHasNoChangesSince(preferenceValues);
      // Ensure the suppliers return the same value if called multiple times.
      assertHasUpdatedValues(ValuesKind.UPDATED_VALUES, newRecordWithPreferences);
    }
  }

  @RunWith(Parameterized.class)
  public static class BooleanPreferencesTest
      extends PreferencesRegistryTestCase<BooleanPreferencesTest.RecordWithBooleans> {
    static final String PREFERENCE_NAME = "Booleans";
    static final String BOOLEAN_VALUE_KEY = "Booleans/booleanValue";
    static final String BOOLEAN_SUPPLIER_KEY = "Booleans/booleanSupplier";
    static final String SUPPLIER_BOOLEAN_KEY = "Booleans/supplierBoolean";
    static final Set<String> ALL_KEYS =
        Set.of(BOOLEAN_VALUE_KEY, BOOLEAN_SUPPLIER_KEY, SUPPLIER_BOOLEAN_KEY);
    final boolean defaultValue;

    @Parameters(name = "defaultValue={0}")
    public static Object[] data() {
      return new Object[] {true, false};
    }

    public BooleanPreferencesTest(boolean defaultValue) {
      super(PREFERENCE_NAME, RecordWithBooleans.class);
      this.defaultValue = defaultValue;
    }

    /** Test record for testing classes that contain boolean fields. */
    private record RecordWithBooleans(
        boolean booleanValue, BooleanSupplier booleanSupplier, Supplier<Boolean> supplierBoolean) {

      static RecordWithBooleans withDefaultValue(boolean defaultValue) {
        return new RecordWithBooleans(
            defaultValue, () -> defaultValue, () -> Boolean.valueOf(defaultValue));
      }
    }

    @Override
    protected RecordWithBooleans createRecordWithConfiguredDefaults() {
      return RecordWithBooleans.withDefaultValue(defaultValue);
    }

    @Override
    protected void assertHasConfiguredDefaults(RecordWithBooleans record) {
      assertThat(record.booleanValue()).isEqualTo(defaultValue);
      assertThat(record.booleanSupplier().getAsBoolean()).isEqualTo(defaultValue);
      assertThat(record.supplierBoolean().get()).isEqualTo(Boolean.valueOf(defaultValue));
    }

    @Override
    protected void assertPreferencesHaveConfiguredDefaults() {
      assertThat(preferenceKeys())
          .containsExactly(BOOLEAN_VALUE_KEY, BOOLEAN_SUPPLIER_KEY, SUPPLIER_BOOLEAN_KEY);
      for (String key : ALL_KEYS) {
        assertThat(Preferences.getBoolean(key, !defaultValue)).isEqualTo(defaultValue);
      }
    }

    @Override
    protected void assertHasJavaDefaults(RecordWithBooleans record) {
      assertThat(record.booleanValue()).isFalse();
      assertThat(record.booleanSupplier().getAsBoolean()).isFalse();
      assertThat(record.supplierBoolean().get()).isFalse();
    }

    @Override
    protected void assertPreferencesHaveJavaDefaults() {
      assertThat(preferenceKeys())
          .containsExactly(BOOLEAN_VALUE_KEY, BOOLEAN_SUPPLIER_KEY, SUPPLIER_BOOLEAN_KEY);
      for (String key : ALL_KEYS) {
        assertThat(Preferences.getBoolean(key, true)).isFalse();
      }
    }

    private boolean getUpdatedValue(ValuesKind kind) {
      return switch (kind) {
        case INITIAL_VALUES -> !defaultValue;
        case UPDATED_VALUES -> defaultValue;
      };
    }

    @Override
    protected void updatePreferenceValues(ValuesKind kind) {
      boolean newValue = getUpdatedValue(kind);
      for (String key : ALL_KEYS) {
        Preferences.setBoolean(key, newValue);
      }
    }

    @Override
    protected void assertHasUpdatedValues(ValuesKind kind, RecordWithBooleans record) {
      boolean expectedValue = getUpdatedValue(kind);
      assertThat(record.booleanValue()).isEqualTo(expectedValue);
      assertThat(record.booleanSupplier().getAsBoolean()).isEqualTo(expectedValue);
      assertThat(record.supplierBoolean().get()).isEqualTo(Boolean.valueOf(expectedValue));
    }

    @Override
    protected void assertSuppliersHaveUpdatedValues(RecordWithBooleans record) {
      boolean expectedValue = getUpdatedValue(ValuesKind.UPDATED_VALUES);
      assertThat(record.booleanSupplier().getAsBoolean()).isEqualTo(expectedValue);
      assertThat(record.supplierBoolean().get()).isEqualTo(Boolean.valueOf(expectedValue));
    }
  }

  public static class IntPreferencesTest
      extends PreferencesRegistryTestCase<IntPreferencesTest.RecordWithInts> {
    static final String PREFERENCE_NAME = "Integers";
    static final String INT_VALUE_KEY = "Integers/intValue";
    static final String INT_SUPPLIER_KEY = "Integers/intSupplier";
    static final String SUPPLIER_INT_KEY = "Integers/supplierInt";

    public IntPreferencesTest() {
      super(PREFERENCE_NAME, RecordWithInts.class);
    }

    /** Test record for testing classes that contain int fields. */
    private record RecordWithInts(
        int intValue, IntSupplier intSupplier, Supplier<Integer> supplierInt) {

      static RecordWithInts withDefaultValues(
          int intValue, int intSupplierValue, int supplierIntValue) {
        return new RecordWithInts(intValue, () -> intSupplierValue, () -> supplierIntValue);
      }
    }

    @Override
    protected RecordWithInts createRecordWithConfiguredDefaults() {
      return RecordWithInts.withDefaultValues(1, 2, 3);
    }

    @Override
    protected void assertHasConfiguredDefaults(RecordWithInts record) {
      assertThat(record.intValue()).isEqualTo(1);
      assertThat(record.intSupplier.getAsInt()).isEqualTo(2);
      assertThat(record.supplierInt().get()).isEqualTo(Integer.valueOf(3));
    }

    @Override
    protected void assertPreferencesHaveConfiguredDefaults() {
      assertThat(preferenceKeys())
          .containsExactly(INT_VALUE_KEY, INT_SUPPLIER_KEY, SUPPLIER_INT_KEY);
      assertThat(Preferences.getInt(INT_VALUE_KEY, -1)).isEqualTo(1);
      assertThat(Preferences.getInt(INT_SUPPLIER_KEY, -1)).isEqualTo(2);
      assertThat(Preferences.getInt(SUPPLIER_INT_KEY, -1)).isEqualTo(3);
    }

    @Override
    protected void assertHasJavaDefaults(RecordWithInts record) {
      assertThat(record.intValue()).isEqualTo(0);
      assertThat(record.intSupplier.getAsInt()).isEqualTo(0);
      assertThat(record.supplierInt().get()).isEqualTo(Integer.valueOf(0));
    }

    @Override
    protected void assertPreferencesHaveJavaDefaults() {
      assertThat(preferenceKeys())
          .containsExactly(INT_VALUE_KEY, INT_SUPPLIER_KEY, SUPPLIER_INT_KEY);
      assertThat(Preferences.getInt(INT_VALUE_KEY, -1)).isEqualTo(0);
      assertThat(Preferences.getInt(INT_SUPPLIER_KEY, -1)).isEqualTo(0);
      assertThat(Preferences.getInt(SUPPLIER_INT_KEY, -1)).isEqualTo(0);
    }

    @Override
    protected void updatePreferenceValues(ValuesKind kind) {
      switch (kind) {
        case INITIAL_VALUES -> {
          Preferences.setInt(INT_VALUE_KEY, 101);
          Preferences.setInt(INT_SUPPLIER_KEY, 102);
          Preferences.setInt(SUPPLIER_INT_KEY, 103);
        }
        case UPDATED_VALUES -> {
          Preferences.setInt(INT_VALUE_KEY, 201);
          Preferences.setInt(INT_SUPPLIER_KEY, 202);
          Preferences.setInt(SUPPLIER_INT_KEY, 203);
        }
      }
    }

    @Override
    protected void assertHasUpdatedValues(ValuesKind kind, RecordWithInts record) {
      switch (kind) {
        case INITIAL_VALUES -> {
          assertThat(record.intValue()).isEqualTo(101);
          assertThat(record.intSupplier.getAsInt()).isEqualTo(102);
          assertThat(record.supplierInt().get()).isEqualTo(Integer.valueOf(103));
        }
        case UPDATED_VALUES -> {
          assertThat(record.intValue()).isEqualTo(201);
          assertThat(record.intSupplier.getAsInt()).isEqualTo(202);
          assertThat(record.supplierInt().get()).isEqualTo(Integer.valueOf(203));
        }
      }
    }

    @Override
    protected void assertSuppliersHaveUpdatedValues(RecordWithInts record) {
      assertThat(record.intSupplier.getAsInt()).isEqualTo(202);
      assertThat(record.supplierInt().get()).isEqualTo(Integer.valueOf(203));
    }
  }

  public static class LongPreferencesTest
      extends PreferencesRegistryTestCase<LongPreferencesTest.RecordWithLongs> {
    static final String PREFERENCE_NAME = "Longs";
    static final String LONG_VALUE_KEY = "Longs/longValue";
    static final String LONG_SUPPLIER_KEY = "Longs/longSupplier";
    static final String SUPPLIER_LONG_KEY = "Longs/supplierLong";

    public LongPreferencesTest() {
      super(PREFERENCE_NAME, RecordWithLongs.class);
    }

    /** Test record for testing classes that contain long fields. */
    private record RecordWithLongs(
        long longValue, LongSupplier longSupplier, Supplier<Long> supplierLong) {

      static RecordWithLongs withDefaultValues(
          long longValue, long longSupplierValue, long supplierLongValue) {
        return new RecordWithLongs(longValue, () -> longSupplierValue, () -> supplierLongValue);
      }
    }

    @Override
    protected RecordWithLongs createRecordWithConfiguredDefaults() {
      return RecordWithLongs.withDefaultValues(1, 2, 3);
    }

    @Override
    protected void assertHasConfiguredDefaults(RecordWithLongs record) {
      assertThat(record.longValue()).isEqualTo(1);
      assertThat(record.longSupplier.getAsLong()).isEqualTo(2);
      assertThat(record.supplierLong().get()).isEqualTo(Long.valueOf(3));
    }

    @Override
    protected void assertPreferencesHaveConfiguredDefaults() {
      assertThat(preferenceKeys())
          .containsExactly(LONG_VALUE_KEY, LONG_SUPPLIER_KEY, SUPPLIER_LONG_KEY);
      assertThat(Preferences.getLong(LONG_VALUE_KEY, -1)).isEqualTo(1);
      assertThat(Preferences.getLong(LONG_SUPPLIER_KEY, -1)).isEqualTo(2);
      assertThat(Preferences.getLong(SUPPLIER_LONG_KEY, -1)).isEqualTo(3);
    }

    @Override
    protected void assertHasJavaDefaults(RecordWithLongs record) {
      assertThat(record.longValue()).isEqualTo(0);
      assertThat(record.longSupplier.getAsLong()).isEqualTo(0);
      assertThat(record.supplierLong().get()).isEqualTo(Long.valueOf(0));
    }

    @Override
    protected void assertPreferencesHaveJavaDefaults() {
      assertThat(preferenceKeys())
          .containsExactly(LONG_VALUE_KEY, LONG_SUPPLIER_KEY, SUPPLIER_LONG_KEY);
      assertThat(Preferences.getLong(LONG_VALUE_KEY, -1)).isEqualTo(0);
      assertThat(Preferences.getLong(LONG_SUPPLIER_KEY, -1)).isEqualTo(0);
      assertThat(Preferences.getLong(SUPPLIER_LONG_KEY, -1)).isEqualTo(0);
    }

    @Override
    protected void updatePreferenceValues(ValuesKind kind) {
      switch (kind) {
        case INITIAL_VALUES -> {
          Preferences.setLong(LONG_VALUE_KEY, 10);
          Preferences.setLong(LONG_SUPPLIER_KEY, 20);
          Preferences.setLong(SUPPLIER_LONG_KEY, 30);
        }
        case UPDATED_VALUES -> {
          Preferences.setLong(LONG_VALUE_KEY, 100);
          Preferences.setLong(LONG_SUPPLIER_KEY, 200);
          Preferences.setLong(SUPPLIER_LONG_KEY, 300);
        }
      }
    }

    @Override
    protected void assertHasUpdatedValues(ValuesKind kind, RecordWithLongs record) {
      switch (kind) {
        case INITIAL_VALUES -> {
          assertThat(record.longValue()).isEqualTo(10);
          assertThat(record.longSupplier.getAsLong()).isEqualTo(20);
          assertThat(record.supplierLong().get()).isEqualTo(Long.valueOf(30));
        }
        case UPDATED_VALUES -> {
          assertThat(record.longValue()).isEqualTo(100);
          assertThat(record.longSupplier.getAsLong()).isEqualTo(200);
          assertThat(record.supplierLong().get()).isEqualTo(Long.valueOf(300));
        }
      }
    }

    @Override
    protected void assertSuppliersHaveUpdatedValues(RecordWithLongs record) {
      assertThat(record.longSupplier.getAsLong()).isEqualTo(200);
      assertThat(record.supplierLong().get()).isEqualTo(Long.valueOf(300));
    }
  }

  public static class DoublePreferencesTest
      extends PreferencesRegistryTestCase<DoublePreferencesTest.RecordWithDoubles> {
    static final String PREFERENCE_NAME = "Doubles";
    static final String DOUBLE_VALUE_KEY = "Doubles/doubleValue";
    static final String DOUBLE_SUPPLIER_KEY = "Doubles/doubleSupplier";
    static final String SUPPLIER_DOUBLE_KEY = "Doubles/supplierDouble";

    public DoublePreferencesTest() {
      super(PREFERENCE_NAME, RecordWithDoubles.class);
    }

    /** Test record for testing classes that contain double fields. */
    private record RecordWithDoubles(
        double doubleValue, DoubleSupplier doubleSupplier, Supplier<Double> supplierDouble) {

      static RecordWithDoubles withDefaultValues(
          double doubleValue, double doubleSupplierValue, double supplierDoubleValue) {
        return new RecordWithDoubles(
            doubleValue, () -> doubleSupplierValue, () -> supplierDoubleValue);
      }
    }

    @Override
    protected RecordWithDoubles createRecordWithConfiguredDefaults() {
      return RecordWithDoubles.withDefaultValues(3.14159, 2.71828, 6.28318);
    }

    @Override
    protected void assertHasConfiguredDefaults(RecordWithDoubles record) {
      assertThat(record.doubleValue()).isWithin(EPSILON).of(3.14159);
      assertThat(record.doubleSupplier.getAsDouble()).isWithin(EPSILON).of(2.71828);
      assertThat(record.supplierDouble().get()).isWithin(EPSILON).of(6.28318);
    }

    @Override
    protected void assertPreferencesHaveConfiguredDefaults() {
      assertThat(preferenceKeys())
          .containsExactly(DOUBLE_VALUE_KEY, DOUBLE_SUPPLIER_KEY, SUPPLIER_DOUBLE_KEY);
      assertThat(Preferences.getDouble(DOUBLE_VALUE_KEY, -1)).isWithin(EPSILON).of(3.14159);
      assertThat(Preferences.getDouble(DOUBLE_SUPPLIER_KEY, -1)).isWithin(EPSILON).of(2.71828);
      assertThat(Preferences.getDouble(SUPPLIER_DOUBLE_KEY, -1)).isWithin(EPSILON).of(6.28318);
    }

    @Override
    protected void assertHasJavaDefaults(RecordWithDoubles record) {
      assertThat(record.doubleValue()).isWithin(EPSILON).of(0);
      assertThat(record.doubleSupplier.getAsDouble()).isWithin(EPSILON).of(0);
      assertThat(record.supplierDouble().get()).isWithin(EPSILON).of(0);
    }

    @Override
    protected void assertPreferencesHaveJavaDefaults() {
      assertThat(preferenceKeys())
          .containsExactly(DOUBLE_VALUE_KEY, DOUBLE_SUPPLIER_KEY, SUPPLIER_DOUBLE_KEY);
      assertThat(Preferences.getDouble(DOUBLE_VALUE_KEY, -1)).isWithin(EPSILON).of(0);
      assertThat(Preferences.getDouble(DOUBLE_SUPPLIER_KEY, -1)).isWithin(EPSILON).of(0);
      assertThat(Preferences.getDouble(SUPPLIER_DOUBLE_KEY, -1)).isWithin(EPSILON).of(0);
    }

    @Override
    protected void updatePreferenceValues(ValuesKind kind) {
      switch (kind) {
        case INITIAL_VALUES -> {
          Preferences.setDouble(DOUBLE_VALUE_KEY, 1.23);
          Preferences.setDouble(DOUBLE_SUPPLIER_KEY, 4.56);
          Preferences.setDouble(SUPPLIER_DOUBLE_KEY, 7.89);
        }
        case UPDATED_VALUES -> {
          Preferences.setDouble(DOUBLE_VALUE_KEY, 10.23);
          Preferences.setDouble(DOUBLE_SUPPLIER_KEY, 40.56);
          Preferences.setDouble(SUPPLIER_DOUBLE_KEY, 70.89);
        }
      }
    }

    @Override
    protected void assertHasUpdatedValues(ValuesKind kind, RecordWithDoubles record) {
      switch (kind) {
        case INITIAL_VALUES -> {
          assertThat(record.doubleValue()).isWithin(EPSILON).of(1.23);
          assertThat(record.doubleSupplier.getAsDouble()).isWithin(EPSILON).of(4.56);
          assertThat(record.supplierDouble().get()).isWithin(EPSILON).of(7.89);
        }
        case UPDATED_VALUES -> {
          assertThat(record.doubleValue()).isWithin(EPSILON).of(10.23);
          assertThat(record.doubleSupplier.getAsDouble()).isWithin(EPSILON).of(40.56);
          assertThat(record.supplierDouble().get()).isWithin(EPSILON).of(70.89);
        }
      }
    }

    @Override
    protected void assertSuppliersHaveUpdatedValues(RecordWithDoubles record) {
      assertThat(record.doubleSupplier.getAsDouble()).isWithin(EPSILON).of(40.56);
      assertThat(record.supplierDouble().get()).isWithin(EPSILON).of(70.89);
    }
  }

  public static class StringPreferencesTest
      extends PreferencesRegistryTestCase<StringPreferencesTest.RecordWithStrings> {
    static final String PREFERENCE_NAME = "Strings";
    static final String STRING_VALUE_KEY = "Strings/stringValue";
    static final String SUPPLIER_STRING_KEY = "Strings/supplierString";

    public StringPreferencesTest() {
      super(PREFERENCE_NAME, RecordWithStrings.class);
    }

    private record RecordWithStrings(String stringValue, Supplier<String> supplierString) {

      static RecordWithStrings withDefaultValues(String stringValue, String supplierStringValue) {
        return new RecordWithStrings(stringValue, () -> supplierStringValue);
      }
    }

    @Override
    protected RecordWithStrings createRecordWithConfiguredDefaults() {
      return RecordWithStrings.withDefaultValues("chicken", "bus");
    }

    @Override
    protected void assertHasConfiguredDefaults(RecordWithStrings record) {
      assertThat(record.stringValue()).isEqualTo("chicken");
      assertThat(record.supplierString().get()).isEqualTo("bus");
    }

    @Override
    protected void assertPreferencesHaveConfiguredDefaults() {
      assertThat(preferenceKeys()).containsExactly(STRING_VALUE_KEY, SUPPLIER_STRING_KEY);
      assertThat(Preferences.getString(STRING_VALUE_KEY, "")).isEqualTo("chicken");
      assertThat(Preferences.getString(SUPPLIER_STRING_KEY, "")).isEqualTo("bus");
    }

    @Override
    protected void assertHasJavaDefaults(RecordWithStrings record) {
      assertThat(record.stringValue()).isEmpty();
      assertThat(record.supplierString().get()).isEmpty();
    }

    @Override
    protected void assertPreferencesHaveJavaDefaults() {
      assertThat(preferenceKeys()).containsExactly(STRING_VALUE_KEY, SUPPLIER_STRING_KEY);
      assertThat(Preferences.getString(STRING_VALUE_KEY, "default")).isEmpty();
      assertThat(Preferences.getString(SUPPLIER_STRING_KEY, "default")).isEmpty();
    }

    @Override
    protected void updatePreferenceValues(ValuesKind kind) {
      switch (kind) {
        case INITIAL_VALUES -> {
          Preferences.setString(STRING_VALUE_KEY, "Gear");
          Preferences.setString(SUPPLIER_STRING_KEY, "Heads");
        }
        case UPDATED_VALUES -> {
          Preferences.setString(STRING_VALUE_KEY, "Blue");
          Preferences.setString(SUPPLIER_STRING_KEY, "White");
        }
      }
    }

    @Override
    protected void assertHasUpdatedValues(ValuesKind kind, RecordWithStrings record) {
      switch (kind) {
        case INITIAL_VALUES -> {
          assertThat(record.stringValue()).isEqualTo("Gear");
          assertThat(record.supplierString().get()).isEqualTo("Heads");
        }
        case UPDATED_VALUES -> {
          assertThat(record.stringValue()).isEqualTo("Blue");
          assertThat(record.supplierString().get()).isEqualTo("White");
        }
      }
    }

    @Override
    protected void assertSuppliersHaveUpdatedValues(RecordWithStrings record) {
      assertThat(record.supplierString().get()).isEqualTo("White");
    }
  }

  public static class RecordPreferencesTest
      extends PreferencesRegistryTestCase<RecordPreferencesTest.RecordWithRecords> {
    static final String PREFERENCE_NAME = "Records";
    static final String recordValueKey = "Records/recordValue";
    static final String longValueKey = recordValueKey + "/longValue";
    static final String stringValueKey = recordValueKey + "/stringValue";

    public RecordPreferencesTest() {
      super(PREFERENCE_NAME, RecordWithRecords.class);
    }

    private record RecordWithPrimitives(long longValue, String stringValue) {}

    /** Test record for testing classes that contain record fields. */
    private record RecordWithRecords(RecordWithPrimitives recordValue) {}

    @Override
    protected RecordWithRecords createRecordWithConfiguredDefaults() {
      return new RecordWithRecords(new RecordWithPrimitives(42, "The Answer"));
    }

    @Override
    protected void assertHasConfiguredDefaults(RecordWithRecords record) {
      assertThat(record.recordValue.stringValue()).isEqualTo("The Answer");
      assertThat(record.recordValue.longValue()).isEqualTo(42);
    }

    @Override
    protected void assertPreferencesHaveConfiguredDefaults() {
      assertThat(preferenceKeys()).containsExactly(stringValueKey, longValueKey);
      assertThat(Preferences.getString(stringValueKey, "")).isEqualTo("The Answer");
      assertThat(Preferences.getLong(longValueKey, -1)).isEqualTo(42);
    }

    @Override
    protected void assertHasJavaDefaults(RecordWithRecords record) {
      assertThat(record.recordValue.stringValue()).isEmpty();
      assertThat(record.recordValue.longValue()).isEqualTo(0);
    }

    @Override
    protected void assertPreferencesHaveJavaDefaults() {
      assertThat(preferenceKeys()).containsExactly(stringValueKey, longValueKey);
      assertThat(Preferences.getString(stringValueKey, "default")).isEmpty();
      assertThat(Preferences.getLong(longValueKey, -1)).isEqualTo(0);
    }

    @Override
    protected void updatePreferenceValues(ValuesKind kind) {
      switch (kind) {
        case INITIAL_VALUES -> {
          Preferences.setString(stringValueKey, "Agent");
          Preferences.setLong(longValueKey, 99);
        }
        case UPDATED_VALUES -> {
          Preferences.setString(stringValueKey, "Gear Heads");
          Preferences.setLong(longValueKey, 2813);
        }
      }
    }

    @Override
    protected void assertHasUpdatedValues(ValuesKind kind, RecordWithRecords record) {
      switch (kind) {
        case INITIAL_VALUES -> {
          assertThat(record.recordValue.stringValue()).isEqualTo("Agent");
          assertThat(record.recordValue.longValue()).isEqualTo(99);
        }
        case UPDATED_VALUES -> {
          assertThat(record.recordValue.stringValue()).isEqualTo("Gear Heads");
          assertThat(record.recordValue.longValue()).isEqualTo(2813);
        }
      }
    }

    @Override
    protected void assertSuppliersHaveUpdatedValues(RecordWithRecords record) {
      // Supplier<Record> is not supported, so nothing to do here.
    }
  }
}
