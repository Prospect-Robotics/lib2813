package com.team2813.lib2813.preferences;

import edu.wpi.first.wpilibj.Preferences;

/**
 * Initializes the fields of a Record Class from values stored in {@link Preferences}.
 *
 * <p>Example use:
 *
 * <pre>
 * public final class Drive {
 *
 *   public record DriveConfiguration(
 *       boolean addLimelightMeasurement, long robotWeight,
 *       DoubleSupplier powerMultiplier) {
 *
 *     public static DriveConfiguration fromPreferences() {
 *       DriveConfiguration defaultConfig = new DriveConfiguration(
 *           true, 137, () -> 3.14);
 *       return PreferencesInjector.DEFAULT_INSTANCE.injectPreferences(defaultConfig);
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>In the above example, values will be stored in NetworkTables under the "Preferences" table for
 * each of the components of the {@code DriveConfiguration} record. The keys would be:
 *
 * <ul>
 *   <li>"Drive.DriveConfiguration.addLimelightMeasurement"
 *   <li>"Drive.DriveConfiguration.robotWeight"
 *   <li>"Drive.DriveConfiguration.powerMultiplier"
 * </ul>
 *
 * <p>The default values of for these Preference values will be the values provided to {@link
 * PreferencesInjector#injectPreferences(Record)}. The values can be updated in the SmartDashboard
 * and/or Shuffleboard UI; updated values will be stored in the flash storage for the robot.
 *
 * @deprecated Use {@link PersistedConfiguration}
 */
@Deprecated(since = "2.0.0", forRemoval = true)
public class PreferencesInjector {
  private static final char PATH_SEPARATOR = '.';

  /**
   * Injector instance that removes "com.team2813." from class names when creating prefence key
   * names.
   */
  public static final PreferencesInjector DEFAULT_INSTANCE =
      new PreferencesInjector("com.team2813.");

  private final String removePrefix;
  private final int removePrefixLen;

  /**
   * Creates an instance of the injector.
   *
   * <p>Takes in a prefix string, which is used when converting class names to Preference keys. When
   * a record class is passed to the injector, the code generates the Preference keys by
   * concatenating the canonical name of the class with the name of the record component (aka
   * "record field"). This can result in long keys; to avoid this, callers can pass in a prefix into
   * this constructor, and if the generated keys start with that prefix, the prefix will be removed
   * when generating the key.
   *
   * @param removePrefix String to remove (if present) from the start of generated keys.
   */
  public PreferencesInjector(String removePrefix) {
    this.removePrefix = removePrefix;
    this.removePrefixLen = removePrefix.length();
  }

  /**
   * Creates an instance of the given record class with all fields populated from Preferences.
   *
   * <p>To be stored in preferences, the type of the record components can be any of the following:
   *
   * <ul>
   *   <li>{@code boolean} or {@code BooleanSupplier} or {@code Supplier<Boolean>}
   *   <li>{@code int} or {@code IntSupplier} or {@code Supplier<Integer>}
   *   <li>{@code long} or {@code LongSupplier} or {@code Supplier<Long>}
   *   <li>{@code double} or {@code DoubleSupplier} or {@code Supplier<Double>}
   *   <li>{@code String} or {@code Supplier<String>}
   *   <li>{@code Record} following the above rules
   * </ul>
   *
   * <p>The values for the components for the passed-in instance will be used as the default value
   * for the preference. If a component is a supplier, the supplier will be called at most once to
   * get the default instance. Suppliers cannot return {@code null}.
   *
   * @param configWithDefaults Record instance with all values set to their preferred default
   *     values.
   */
  public final <T extends java.lang.Record> T injectPreferences(T configWithDefaults) {
    String prefix = createKey(configWithDefaults.getClass());
    return PersistedConfiguration.fromPreferences(prefix, configWithDefaults, PATH_SEPARATOR);
  }

  /**
   * Creates a preference key for the given record class.
   *
   * <p>This method is protected and non-final so subclasses can change the default behavior.
   */
  protected String createKey(Class<? extends Record> recordClass) {
    String recordName = recordClass.getCanonicalName();
    if (recordName == null) {
      recordName = recordClass.getName();
    }
    if (recordName.startsWith(this.removePrefix)) {
      String adjustedName = recordName.substring(this.removePrefixLen);
      if (!adjustedName.isEmpty()) {
        recordName = adjustedName;
      }
    }
    return recordName;
  }
}
