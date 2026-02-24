/*
Copyright 2025-2026 Prospect Robotics SWENext Club

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.team2813.lib2813.preferences;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static com.team2813.lib2813.preferences.PersistedConfiguration.REGISTERED_CLASSES_NETWORK_TABLE_KEY;
import static java.util.stream.Collectors.toMap;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import com.team2813.lib2813.testing.junit.jupiter.ProvideUniqueNetworkTableInstance;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableType;
import edu.wpi.first.networktables.Topic;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.Preferences;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests for {@link PersistedConfiguration}. */
@ProvideUniqueNetworkTableInstance(replacePreferencesNetworkTable = true)
public final class PersistedConfigurationTest {
  private static final double EPSILON = 0.001;

  /** Base class for all nested classes of {@link PersistedConfigurationTest}. */
  @Nested
  @Execution(ExecutionMode.SAME_THREAD) // Test updates static state
  abstract class PreferencesRegistryTestCase<T extends Record> {
    private final List<Executable> collectedErrors = new ArrayList<>();
    private final String preferenceName;
    private final Class<T> recordClass;
    private NetworkTableInstance ntInstance;
    private NetworkTable preferencesTable;

    protected PreferencesRegistryTestCase(String preferenceName, Class<T> recordClass) {
      this.preferenceName = preferenceName;
      this.recordClass = recordClass;
    }

    @BeforeEach
    public final void injectNetworkTableInstance(NetworkTableInstance ntInstance) {
      this.ntInstance = ntInstance;
      preferencesTable = ntInstance.getTable("Preferences");
    }

    @BeforeEach
    public final void setTestGlobals() {
      PersistedConfiguration.throwExceptions = true;
      PersistedConfiguration.errorReporter =
          message -> collectedErrors.add(() -> fail("Unexpected warning: \"" + message + "\""));
    }

    @AfterEach
    public final void resetTestGlobals() {
      PersistedConfiguration.throwExceptions = false;
      PersistedConfiguration.errorReporter = DataLogManager::log;
      assertAll(collectedErrors);
    }

    protected enum ValuesKind {
      INITIAL_VALUES,
      UPDATED_VALUES
    }

    private NetworkTableEntry getTableEntry(String key, NetworkTableType expectedType) {
      NetworkTableEntry entry = preferencesTable.getEntry(key);
      assertThat(entry.getType()).isEqualTo(expectedType);
      return entry;
    }

    protected final boolean getBooleanValue(String key, boolean defaultValue) {
      return getTableEntry(key, NetworkTableType.kBoolean).getBoolean(defaultValue);
    }

    protected final long getIntegerValue(String key) {
      return getTableEntry(key, NetworkTableType.kInteger).getInteger(-1);
    }

    protected final double getDoubleValue(String key) {
      return getTableEntry(key, NetworkTableType.kDouble).getDouble(-1);
    }

    protected final String getStringValue(String key) {
      return getTableEntry(key, NetworkTableType.kString).getString("defaultValue");
    }

    protected final void setIntegerValue(String key, int value) {
      NetworkTableEntry entry = preferencesTable.getEntry(key);
      entry.setInteger(value);
      entry.setPersistent();
    }

    protected final void assertHasNoChangesSince(Map<String, Object> previousValues) {
      var preferenceValues = preferenceValues();
      assertWithMessage("Unexpected no changes to preference values")
          .that(preferenceValues)
          .isEqualTo(previousValues);
    }

    protected final Map<String, Object> preferenceValues() {
      return preferenceKeys().stream()
          .collect(
              toMap(
                  Function.identity(),
                  key -> preferencesTable.getEntry(key).getValue().getValue()));
    }

    protected final Set<String> preferenceKeys() {
      Set<String> keys = new HashSet<>();
      collectKeys(preferencesTable, keys);
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
    public void preferenceNameMapsToOnlyOneRecordType() {
      // Arrange
      PersistedConfiguration.fromPreferences(preferenceName, recordClass);

      // Act
      Exception exception =
          assertThrows(
              IllegalStateException.class,
              () -> PersistedConfiguration.fromPreferences(preferenceName, UnrelatedRecord.class));

      // Assert: Exception has expected message
      assertThat(exception)
          .hasMessageThat()
          .containsMatch("Preference with name '" + preferenceName + "' already registered");

      // Assert: topic added under "/PersistedConfiguration", and is not persistent
      NetworkTable table = ntInstance.getTable(REGISTERED_CLASSES_NETWORK_TABLE_KEY);
      NetworkTableEntry entry = table.getEntry(preferenceName);
      assertThat(entry.exists()).isTrue();
      assertThat(entry.isPersistent()).isFalse();
      assertThat(entry.getType()).isEqualTo(NetworkTableType.kString);
    }

    @Test
    public void withoutExistingPreferences_passingRecordInstance() {
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
    public void withoutExistingPreferences_passingRecordClass() {
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
    public void withExistingPreferences_passingRecordInstance() {
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
    public void withExistingPreferences_passingRecordClass() {
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

  @Nested
  @ParameterizedClass(name = "defaultValue={0}")
  @ValueSource(booleans = {true, false})
  public class BooleanPreferencesTest
      extends PreferencesRegistryTestCase<BooleanPreferencesTest.RecordWithBooleans> {
    static final String PREFERENCE_NAME = "Booleans";
    static final String BOOLEAN_VALUE_KEY = "Booleans/booleanValue";
    static final String BOOLEAN_SUPPLIER_KEY = "Booleans/booleanSupplier";
    static final Set<String> ALL_KEYS = Set.of(BOOLEAN_VALUE_KEY, BOOLEAN_SUPPLIER_KEY);
    final boolean defaultValue;

    public BooleanPreferencesTest(boolean defaultValue) {
      super(PREFERENCE_NAME, RecordWithBooleans.class);
      this.defaultValue = defaultValue;
    }

    /** Test record for testing classes that contain boolean fields. */
    private record RecordWithBooleans(boolean booleanValue, BooleanSupplier booleanSupplier) {

      static RecordWithBooleans withDefaultValue(boolean defaultValue) {
        return new RecordWithBooleans(defaultValue, () -> defaultValue);
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
    }

    @Override
    protected void assertPreferencesHaveConfiguredDefaults() {
      assertThat(preferenceKeys()).containsExactly(BOOLEAN_VALUE_KEY, BOOLEAN_SUPPLIER_KEY);
      for (String key : ALL_KEYS) {
        assertThat(getBooleanValue(key, !defaultValue)).isEqualTo(defaultValue);
      }
    }

    @Override
    protected void assertHasJavaDefaults(RecordWithBooleans record) {
      assertThat(record.booleanValue()).isFalse();
      assertThat(record.booleanSupplier().getAsBoolean()).isFalse();
    }

    @Override
    protected void assertPreferencesHaveJavaDefaults() {
      assertThat(preferenceKeys()).containsExactly(BOOLEAN_VALUE_KEY, BOOLEAN_SUPPLIER_KEY);
      for (String key : ALL_KEYS) {
        assertThat(getBooleanValue(key, true)).isFalse();
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
    }

    @Override
    protected void assertSuppliersHaveUpdatedValues(RecordWithBooleans record) {
      boolean expectedValue = getUpdatedValue(ValuesKind.UPDATED_VALUES);
      assertThat(record.booleanSupplier().getAsBoolean()).isEqualTo(expectedValue);
    }
  }

  @Nested
  @ParameterizedClass(name = "storeAsDoubles={0}")
  @ValueSource(booleans = {true, false})
  public class IntPreferencesTest
      extends PreferencesRegistryTestCase<IntPreferencesTest.RecordWithInts> {
    static final String PREFERENCE_NAME = "Integers";
    static final String INT_VALUE_KEY = "Integers/intValue";
    static final String INT_SUPPLIER_KEY = "Integers/intSupplier";
    final boolean storeAsDoubles;

    public IntPreferencesTest(boolean storeAsDoubles) {
      super(PREFERENCE_NAME, RecordWithInts.class);
      this.storeAsDoubles = storeAsDoubles;
    }

    /** Test record for testing classes that contain int fields. */
    private record RecordWithInts(int intValue, IntSupplier intSupplier) {

      static RecordWithInts withDefaultValues(int intValue, int intSupplierValue) {
        return new RecordWithInts(intValue, () -> intSupplierValue);
      }
    }

    @Override
    protected RecordWithInts createRecordWithConfiguredDefaults() {
      return RecordWithInts.withDefaultValues(1, 2);
    }

    @Override
    protected void assertHasConfiguredDefaults(RecordWithInts record) {
      assertThat(record.intValue()).isEqualTo(1);
      assertThat(record.intSupplier.getAsInt()).isEqualTo(2);
    }

    @Override
    protected void assertPreferencesHaveConfiguredDefaults() {
      assertThat(preferenceKeys()).containsExactly(INT_VALUE_KEY, INT_SUPPLIER_KEY);
      assertThat(getIntegerValue(INT_VALUE_KEY)).isEqualTo(1);
      assertThat(getIntegerValue(INT_SUPPLIER_KEY)).isEqualTo(2);
    }

    @Override
    protected void assertHasJavaDefaults(RecordWithInts record) {
      assertThat(record.intValue()).isEqualTo(0);
      assertThat(record.intSupplier.getAsInt()).isEqualTo(0);
    }

    @Override
    protected void assertPreferencesHaveJavaDefaults() {
      assertThat(preferenceKeys()).containsExactly(INT_VALUE_KEY, INT_SUPPLIER_KEY);
      assertThat(getIntegerValue(INT_VALUE_KEY)).isEqualTo(0);
      assertThat(getIntegerValue(INT_SUPPLIER_KEY)).isEqualTo(0);
    }

    @Override
    protected void updatePreferenceValues(ValuesKind kind) {
      if (storeAsDoubles) {
        switch (kind) {
          case INITIAL_VALUES -> {
            Preferences.setDouble(INT_VALUE_KEY, 101);
            Preferences.setDouble(INT_SUPPLIER_KEY, 102);
          }
          case UPDATED_VALUES -> {
            Preferences.setDouble(INT_VALUE_KEY, 201);
            Preferences.setDouble(INT_SUPPLIER_KEY, 202);
          }
        }
      } else {
        switch (kind) {
          case INITIAL_VALUES -> {
            setIntegerValue(INT_VALUE_KEY, 101);
            setIntegerValue(INT_SUPPLIER_KEY, 102);
          }
          case UPDATED_VALUES -> {
            setIntegerValue(INT_VALUE_KEY, 201);
            setIntegerValue(INT_SUPPLIER_KEY, 202);
          }
        }
      }
    }

    @Override
    protected void assertHasUpdatedValues(ValuesKind kind, RecordWithInts record) {
      switch (kind) {
        case INITIAL_VALUES -> {
          assertThat(record.intValue()).isEqualTo(101);
          assertThat(record.intSupplier.getAsInt()).isEqualTo(102);
        }
        case UPDATED_VALUES -> {
          assertThat(record.intValue()).isEqualTo(201);
          assertThat(record.intSupplier.getAsInt()).isEqualTo(202);
        }
      }
    }

    @Override
    protected void assertSuppliersHaveUpdatedValues(RecordWithInts record) {
      assertThat(record.intSupplier.getAsInt()).isEqualTo(202);
    }
  }

  @Nested
  public class LongPreferencesTest
      extends PreferencesRegistryTestCase<LongPreferencesTest.RecordWithLongs> {
    static final String PREFERENCE_NAME = "Longs";
    static final String LONG_VALUE_KEY = "Longs/longValue";
    static final String LONG_SUPPLIER_KEY = "Longs/longSupplier";

    public LongPreferencesTest() {
      super(PREFERENCE_NAME, RecordWithLongs.class);
    }

    /** Test record for testing classes that contain long fields. */
    private record RecordWithLongs(long longValue, LongSupplier longSupplier) {

      static RecordWithLongs withDefaultValues(long longValue, long longSupplierValue) {
        return new RecordWithLongs(longValue, () -> longSupplierValue);
      }
    }

    @Override
    protected RecordWithLongs createRecordWithConfiguredDefaults() {
      return RecordWithLongs.withDefaultValues(1, 2);
    }

    @Override
    protected void assertHasConfiguredDefaults(RecordWithLongs record) {
      assertThat(record.longValue()).isEqualTo(1);
      assertThat(record.longSupplier.getAsLong()).isEqualTo(2);
    }

    @Override
    protected void assertPreferencesHaveConfiguredDefaults() {
      assertThat(preferenceKeys()).containsExactly(LONG_VALUE_KEY, LONG_SUPPLIER_KEY);
      assertThat(getIntegerValue(LONG_VALUE_KEY)).isEqualTo(1);
      assertThat(getIntegerValue(LONG_SUPPLIER_KEY)).isEqualTo(2);
    }

    @Override
    protected void assertHasJavaDefaults(RecordWithLongs record) {
      assertThat(record.longValue()).isEqualTo(0);
      assertThat(record.longSupplier.getAsLong()).isEqualTo(0);
    }

    @Override
    protected void assertPreferencesHaveJavaDefaults() {
      assertThat(preferenceKeys()).containsExactly(LONG_VALUE_KEY, LONG_SUPPLIER_KEY);
      assertThat(getIntegerValue(LONG_VALUE_KEY)).isEqualTo(0);
      assertThat(getIntegerValue(LONG_SUPPLIER_KEY)).isEqualTo(0);
    }

    @Override
    protected void updatePreferenceValues(ValuesKind kind) {
      switch (kind) {
        case INITIAL_VALUES -> {
          Preferences.setLong(LONG_VALUE_KEY, 10);
          Preferences.setLong(LONG_SUPPLIER_KEY, 20);
        }
        case UPDATED_VALUES -> {
          Preferences.setLong(LONG_VALUE_KEY, 100);
          Preferences.setLong(LONG_SUPPLIER_KEY, 200);
        }
      }
    }

    @Override
    protected void assertHasUpdatedValues(ValuesKind kind, RecordWithLongs record) {
      switch (kind) {
        case INITIAL_VALUES -> {
          assertThat(record.longValue()).isEqualTo(10);
          assertThat(record.longSupplier.getAsLong()).isEqualTo(20);
        }
        case UPDATED_VALUES -> {
          assertThat(record.longValue()).isEqualTo(100);
          assertThat(record.longSupplier.getAsLong()).isEqualTo(200);
        }
      }
    }

    @Override
    protected void assertSuppliersHaveUpdatedValues(RecordWithLongs record) {
      assertThat(record.longSupplier.getAsLong()).isEqualTo(200);
    }
  }

  @Nested
  public class DoublePreferencesTest
      extends PreferencesRegistryTestCase<DoublePreferencesTest.RecordWithDoubles> {
    static final String PREFERENCE_NAME = "Doubles";
    static final String DOUBLE_VALUE_KEY = "Doubles/doubleValue";
    static final String DOUBLE_SUPPLIER_KEY = "Doubles/doubleSupplier";

    public DoublePreferencesTest() {
      super(PREFERENCE_NAME, RecordWithDoubles.class);
    }

    /** Test record for testing classes that contain double fields. */
    private record RecordWithDoubles(double doubleValue, DoubleSupplier doubleSupplier) {

      static RecordWithDoubles withDefaultValues(double doubleValue, double doubleSupplierValue) {
        return new RecordWithDoubles(doubleValue, () -> doubleSupplierValue);
      }
    }

    @Override
    protected RecordWithDoubles createRecordWithConfiguredDefaults() {
      return RecordWithDoubles.withDefaultValues(3.14159, 2.71828);
    }

    @Override
    protected void assertHasConfiguredDefaults(RecordWithDoubles record) {
      assertThat(record.doubleValue()).isWithin(EPSILON).of(3.14159);
      assertThat(record.doubleSupplier.getAsDouble()).isWithin(EPSILON).of(2.71828);
    }

    @Override
    protected void assertPreferencesHaveConfiguredDefaults() {
      assertThat(preferenceKeys()).containsExactly(DOUBLE_VALUE_KEY, DOUBLE_SUPPLIER_KEY);
      assertThat(getDoubleValue(DOUBLE_VALUE_KEY)).isWithin(EPSILON).of(3.14159);
      assertThat(getDoubleValue(DOUBLE_SUPPLIER_KEY)).isWithin(EPSILON).of(2.71828);
    }

    @Override
    protected void assertHasJavaDefaults(RecordWithDoubles record) {
      assertThat(record.doubleValue()).isWithin(EPSILON).of(0);
      assertThat(record.doubleSupplier.getAsDouble()).isWithin(EPSILON).of(0);
    }

    @Override
    protected void assertPreferencesHaveJavaDefaults() {
      assertThat(preferenceKeys()).containsExactly(DOUBLE_VALUE_KEY, DOUBLE_SUPPLIER_KEY);
      assertThat(getDoubleValue(DOUBLE_VALUE_KEY)).isWithin(EPSILON).of(0);
      assertThat(getDoubleValue(DOUBLE_SUPPLIER_KEY)).isWithin(EPSILON).of(0);
    }

    @Override
    protected void updatePreferenceValues(ValuesKind kind) {
      switch (kind) {
        case INITIAL_VALUES -> {
          Preferences.setDouble(DOUBLE_VALUE_KEY, 1.23);
          Preferences.setDouble(DOUBLE_SUPPLIER_KEY, 4.56);
        }
        case UPDATED_VALUES -> {
          Preferences.setDouble(DOUBLE_VALUE_KEY, 10.23);
          Preferences.setDouble(DOUBLE_SUPPLIER_KEY, 40.56);
        }
      }
    }

    @Override
    protected void assertHasUpdatedValues(ValuesKind kind, RecordWithDoubles record) {
      switch (kind) {
        case INITIAL_VALUES -> {
          assertThat(record.doubleValue()).isWithin(EPSILON).of(1.23);
          assertThat(record.doubleSupplier.getAsDouble()).isWithin(EPSILON).of(4.56);
        }
        case UPDATED_VALUES -> {
          assertThat(record.doubleValue()).isWithin(EPSILON).of(10.23);
          assertThat(record.doubleSupplier.getAsDouble()).isWithin(EPSILON).of(40.56);
        }
      }
    }

    @Override
    protected void assertSuppliersHaveUpdatedValues(RecordWithDoubles record) {
      assertThat(record.doubleSupplier.getAsDouble()).isWithin(EPSILON).of(40.56);
    }
  }

  @Nested
  public class StringPreferencesTest
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
      assertThat(getStringValue(STRING_VALUE_KEY)).isEqualTo("chicken");
      assertThat(getStringValue(SUPPLIER_STRING_KEY)).isEqualTo("bus");
    }

    @Override
    protected void assertHasJavaDefaults(RecordWithStrings record) {
      assertThat(record.stringValue()).isEmpty();
      assertThat(record.supplierString().get()).isEmpty();
    }

    @Override
    protected void assertPreferencesHaveJavaDefaults() {
      assertThat(preferenceKeys()).containsExactly(STRING_VALUE_KEY, SUPPLIER_STRING_KEY);
      assertThat(getStringValue(STRING_VALUE_KEY)).isEmpty();
      assertThat(getStringValue(SUPPLIER_STRING_KEY)).isEmpty();
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

  @Nested
  public class RecordPreferencesTest
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
      assertThat(getStringValue(stringValueKey)).isEqualTo("The Answer");
      assertThat(getIntegerValue(longValueKey)).isEqualTo(42);
    }

    @Override
    protected void assertHasJavaDefaults(RecordWithRecords record) {
      assertThat(record.recordValue.stringValue()).isEmpty();
      assertThat(record.recordValue.longValue()).isEqualTo(0);
    }

    @Override
    protected void assertPreferencesHaveJavaDefaults() {
      assertThat(preferenceKeys()).containsExactly(stringValueKey, longValueKey);
      assertThat(getStringValue(stringValueKey)).isEmpty();
      assertThat(getIntegerValue(longValueKey)).isEqualTo(0);
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

  record UnrelatedRecord(int team) {

    UnrelatedRecord() {
      this(2813);
    }
  }
}
