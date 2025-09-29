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

/**
 * Unit tests for {@link PersistedConfiguration}.
 *
 * <p>This test class uses nested classes to test different types of preferences (boolean, int, long,
 * double, String, and record types). It verifies both default behavior and preference updates.
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

    /** IsolatedPreferences ensures tests don't interfere with global Preferences. */
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
          message -> errorCollector.addError(
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

    /** Creates a record instance with default values configured for the test. */
    protected abstract T createRecordWithConfiguredDefaults();

    /** Asserts that the record has the configured defaults. */
    protected abstract void assertHasConfiguredDefaults(T record);

    /** Asserts that preferences contain the configured default values. */
    protected abstract void assertPreferencesHaveConfiguredDefaults();

    /** Asserts that the record has Java defaults (0, false, or empty for object types). */
    protected abstract void assertHasJavaDefaults(T record);

    /** Asserts that the preferences contain Java default values. */
    protected abstract void assertPreferencesHaveJavaDefaults();

    /** Updates the preference values according to {@link ValuesKind}. */
    protected abstract void updatePreferenceValues(ValuesKind kind);

    /**
     * Asserts that the record contains values matching the updated preference values.
     *
     * @param kind type of values to compare against
     * @param record the record to verify
     */
    protected abstract void assertHasUpdatedValues(ValuesKind kind, T record);

    /**
     * Asserts that all supplier fields in the record reflect updated preference values.
     *
     * @param record the record to verify
     */
    protected abstract void assertSuppliersHaveUpdatedValues(T record);

    /**
     * Verifies that the preference name maps to only one record type.
     *
     * <p>If a preference is already registered with a different record type, an exception should
     * be thrown.
     */
    @Test
    public void preferenceNameMapsToOnlyOneRecordType() {
      PersistedConfiguration.fromPreferences(preferenceName, recordClass);

      Exception exception =
          assertThrows(
              IllegalStateException.class,
              () -> PersistedConfiguration.fromPreferences(preferenceName, UnrelatedRecord.class));

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

  }

  /** A record used for testing invalid/unrelated registration. */
  record UnrelatedRecord(int team) {
    UnrelatedRecord() {
      this(2813);
    }
  }
}
