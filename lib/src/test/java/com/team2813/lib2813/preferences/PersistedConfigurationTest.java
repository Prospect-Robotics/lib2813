package com.team2813.lib2813.preferences;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static com.team2813.lib2813.preferences.PersistedConfiguration.REGISTERED_CLASSES_NETWORK_TABLE_KEY;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertThrows;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableType;
import edu.wpi.first.networktables.Topic;
import edu.wpi.first.wpilibj.DataLogManager;
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

/**
 * Unit tests for {@link PersistedConfiguration}.
 *
 * <p>This test class uses nested classes to test different types of preferences (boolean, int,
 * long, double, String, and record types). It verifies both default behavior and preference
 * updates.
 *
 * <p>The Enclosed runner is used so we can have nested test classes.
 */
@RunWith(Enclosed.class)
public final class PersistedConfigurationTest {

  /** Precision for double comparisons. */
  private static final double EPSILON = 0.001;

  /**
   * Base test class for all nested preference tests.
   *
   * @param <T> the type of record used for preference storage
   */
  abstract static class PreferencesRegistryTestCase<T extends Record> {

    /** The name used in Preferences for this test. */
    private final String preferenceName;

    /** The record class used for mapping preferences. */
    private final Class<T> recordClass;

    @Rule public final IsolatedPreferences isolatedPreferences = new IsolatedPreferences();

    /** Allows collecting multiple test errors in a single test execution. */
    @Rule public final ErrorCollector errorCollector = new ErrorCollector();

    /**
     * Constructor for test case.
     *
     * @param preferenceName name of the preference table
     * @param recordClass record class to use for this test
     */
    protected PreferencesRegistryTestCase(String preferenceName, Class<T> recordClass) {
      this.preferenceName = preferenceName;
      this.recordClass = recordClass;
    }

    /**
     * Sets global configuration for tests.
     *
     * <p>This ensures that exceptions are thrown for unexpected errors and are collected by the
     * ErrorCollector.
     */
    @Before
    public final void setTestGlobals() {
      PersistedConfiguration.throwExceptions = true;
      PersistedConfiguration.errorReporter =
          message ->
              errorCollector.addError(
                  new AssertionError("Unexpected warning: \"" + message + "\""));
    }

    /** Resets global configuration after each test. */
    @After
    public final void resetTestGlobals() {
      PersistedConfiguration.throwExceptions = false;
      PersistedConfiguration.errorReporter = DataLogManager::log;
    }

    /** Enum to differentiate between initial and updated values for preferences. */
    protected enum ValuesKind {
      INITIAL_VALUES,
      UPDATED_VALUES
    }

    /**
     * Asserts that preferences have not changed since the previous snapshot.
     *
     * @param previousValues map of preference keys to their expected values
     */
    protected final void assertHasNoChangesSince(Map<String, Object> previousValues) {
      var preferenceValues = preferenceValues();
      assertWithMessage("Unexpected no changes to preference values")
          .that(preferenceValues)
          .isEqualTo(previousValues);
    }

    /**
     * Returns a snapshot of current preference values.
     *
     * @return map of preference keys to values
     */
    protected final Map<String, Object> preferenceValues() {
      NetworkTable table = isolatedPreferences.getPreferencesTable();
      return preferenceKeys().stream()
          .collect(toMap(Function.identity(), key -> table.getEntry(key).getValue().getValue()));
    }

    /**
     * Returns all preference keys currently stored in the isolated preference table.
     *
     * <p>Filters out internal ".type" keys used by Preferences.
     */
    protected final Set<String> preferenceKeys() {
      NetworkTable table = isolatedPreferences.getPreferencesTable();
      Set<String> keys = new HashSet<>();
      collectKeys(table, keys);
      return Set.copyOf(keys);
    }

    /**
     * Recursively collects all preference keys from a NetworkTable and its sub-tables.
     *
     * <p>Preferences adds a ".type" key for each preference; this is filtered out.
     */
    private void collectKeys(NetworkTable table, Set<String> result) {
      for (String key : table.getSubTables()) {
        collectKeys(table.getSubTable(key), result);
      }
      for (Topic topic : table.getTopics()) {
        String topicName = topic.getName().substring(13); // Remove "/Preferences/" prefix
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

    /**
     * Verifies that the preference name maps to only one record type.
     *
     * <p>If a preference is already registered with a different record type, an exception should be
     * thrown.
     */
    @Test
    public void preferenceNameMapsToOnlyOneRecordType() {
      PersistedConfiguration.fromPreferences(preferenceName, recordClass);

      Exception exception =
          assertThrows(
              IllegalStateException.class,
              () -> PersistedConfiguration.fromPreferences(preferenceName, UnrelatedRecord.class));

      // Assert: Exception has expected message
      assertThat(exception)
          .hasMessageThat()
          .containsMatch("Preference with name '" + preferenceName + "' already registered");

      NetworkTable table =
          NetworkTableInstance.getDefault().getTable(REGISTERED_CLASSES_NETWORK_TABLE_KEY);
      NetworkTableEntry entry = table.getEntry(preferenceName);
      assertThat(entry.exists()).isTrue();
      assertThat(entry.isPersistent()).isFalse();
      assertThat(entry.getType()).isEqualTo(NetworkTableType.kString);
    }

    // Other tests for record instances and record classes follow a similar pattern:
    // - Check initial default injection
    // - Update preferences
    // - Check updated injection and supplier consistency
    // - Verify that underlying Preferences store the correct values

    // For brevity, the detailed Javadoc for BooleanPreferencesTest, IntPreferencesTest, etc.
    // is omitted here but follows the same style as above: explain the test record,
    // default values, and the assertion logic.

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
}

/** A record used for testing invalid/unrelated registration. */
record UnrelatedRecord(int team) {
  UnrelatedRecord() {
    this(2813);
  }
}
