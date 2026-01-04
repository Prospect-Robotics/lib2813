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

import static edu.wpi.first.networktables.NetworkTable.PATH_SEPARATOR;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableType;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Preferences;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.*;

/**
 * Initializes the fields of a Record Class from values stored in {@link Preferences}.
 *
 * <p>The Preference values can be updated in Elastic and other dashboards. Updated values will be
 * stored in the flash storage for the robot.
 *
 * <p>Example use:
 *
 * <pre>{@code
 * public final class Drive {
 *
 *   public record DriveConfiguration(
 *       boolean addVisionMeasurements, long robotWeight,
 *       double maxSpeed, String name) {
 *
 *     public static DriveConfiguration fromPreferences() {
 *       return PersistedConfiguration.fromPreferences("Drive", DriveConfiguration.class);
 *     }
 *   }
 * }
 * }</pre>
 *
 * <p>In the above example, {@code fromPreferences()} would return a record instance with the values
 * populated the "Preferences" NetworkTables table. The keys would be:
 *
 * <ul>
 *   <li>{@code "Drive/addVisionMeasurements"}
 *   <li>{@code "Drive/robotWeight"}
 *   <li>{@code "Drive/maxSpeed"}
 *   <li>{@code "Drive/name"}
 * </ul>
 *
 * <p>If no value is stored in Preferences for a key, the default value returned (and initialized in
 * Preferences) would be the default value for the type of the record component. In the above
 * example, if none of the above preference keys existed, preferences will be created with the
 * following values:
 *
 * <ul>
 *   <li>{@code "Drive/addVisionMeasurements"}: {@code false}
 *   <li>{@code "Drive/robotWeight"}: {@code 0}
 *   <li>{@code "Drive/maxSpeed"}: {@code 0.0}
 *   <li>{@code "Drive/name"}: {@code ""}
 * </ul>
 *
 * <p>The record class could also contain suppliers:
 *
 * <pre>{@code
 * public final class Drive {
 *
 *   public record DriveConfiguration(
 *       boolean addVisionMeasurements, LongSupplier robotWeight,
 *       DoubleSupplier powerMultiplier) {
 *
 *     public static DriveConfiguration fromPreferences() {
 *       return PersistedConfiguration.fromPreferences("Drive", DriveConfiguration.class);
 *     }
 *   }
 * }
 * }</pre>
 *
 * <p>In the above example, {@code fromPreferences()} would return a record instance that contained
 * suppliers that, when queried, would return the current value in the "Preferences" NetworkTables
 * table.
 *
 * <p>The caller could specify different default values by passing an instance of the record class:
 *
 * <pre>{@code
 * public final class Drive {
 *
 *   public record DriveConfiguration(
 *       boolean addVisionMeasurements, long robotWeight,
 *       DoubleSupplier maxAngularVelocity) {
 *
 *     public static DriveConfiguration fromPreferences() {
 *       DriveConfiguration defaultConfig = new DriveConfiguration(
 *           true, 2813, () -> 3.14);
 *       return PersistedConfiguration.fromPreferences("Drive", defaultConfig);
 *     }
 *   }
 * }
 * }</pre>
 *
 * <p>In the above example, {@code fromPreferences()} would return a record instance with the values
 * populated the "Preferences" NetworkTables table. The keys and default values would be:
 *
 * <ul>
 *   <li>{@code "Drive/addVisionMeasurements"} (default value: {@code true})
 *   <li>{@code "Drive/robotWeight"} (default value: {@code 2813})
 *   <li>{@code "Drive/maxAngularVelocity"} (default value: {@code 3.14})
 * </ul>
 *
 * <p>Note that {@code PersistedConfiguration} will use the default record constructor to create
 * record instances, so any parameter validation should be done in a custom constructor; see <a
 * href="https://www.baeldung.com/java-records-custom-constructor">Custom Constructor in Java
 * Records</a> for details.
 *
 * <p>For record classes with many component values of the same type, it is strongly recommended
 * that a builder is provided to construct the record, to avoid callers passing the parameters in
 * the wrong order. To make generation of a builder easier, consider using <a
 * href="https://github.com/google/auto/blob/main/value/userguide/autobuilder.md">{@code @AutoBuilder}</a>
 * from Google Auto or <a href="https://projectlombok.org/features/Builder">{@code @Builder}</a>
 * from Project Lombok.
 *
 * @since 2.0.0
 */
public final class PersistedConfiguration {
  static final String REGISTERED_CLASSES_NETWORK_TABLE_KEY = "PersistedConfiguration/registry";
  private static boolean deletedLegacyKeys = false;

  // The below package-scope fields are for the self-tests.
  static boolean throwExceptions = false;
  static Consumer<String> errorReporter = DataLogManager::log;

  /**
   * Creates a record class instance with fields populated from Preferences, using the provided
   * defaults.
   *
   * <p>To be stored in preferences, the type of the record components can be any of the following:
   *
   * <ul>
   *   <li>{@code boolean} or {@code BooleanSupplier}
   *   <li>{@code int} or {@code IntSupplier}
   *   <li>{@code long} or {@code LongSupplier}
   *   <li>{@code double} or {@code DoubleSupplier}
   *   <li>{@code String} or {@code Supplier<String>}
   *   <li>{@code Record} following the above rules
   * </ul>
   *
   * <p>The values for the components for the passed-in instance will be used as the default value
   * for the preference. If a component is a supplier, the supplier will be called at most once to
   * get the default instance. Suppliers cannot return {@code null}.
   *
   * @param preferenceName Preference subtable to use to get the values.
   * @param configWithDefaults Record instance with all values set to their preferred default
   *     values.
   * @return An instance of the record class, populated with data from the Preferences table.
   * @throws IllegalArgumentException If {@code preferenceName} is empty or contains a {@code '/'}.
   * @throws IllegalStateException If {@code preferenceName} was used for a different record class.
   */
  public static <T extends Record> T fromPreferences(String preferenceName, T configWithDefaults) {
    @SuppressWarnings("unchecked")
    Class<T> recordClass = (Class<T>) configWithDefaults.getClass();
    return fromPreferences(preferenceName, recordClass, configWithDefaults);
  }

  /**
   * Creates a record class instance of the provided type, with fields populated from Preferences.
   *
   * <p>To be stored in preferences, the type of the record components can be any of the following:
   *
   * <ul>
   *   <li>{@code boolean} or {@code BooleanSupplier}
   *   <li>{@code int} or {@code IntSupplier}
   *   <li>{@code long} or {@code LongSupplier}
   *   <li>{@code double} or {@code DoubleSupplier}
   *   <li>{@code String} or {@code Supplier<String>}
   *   <li>{@code Record} following the above rules
   * </ul>
   *
   * <p>The default values for the preferences will be Java defaults (for example, zero for
   * integers, an empty string for strings, etc.).
   *
   * @param preferenceName Preference subtable to use to get the values.
   * @param recordClass Type of the record instance to populate from preferences.
   * @return An instance of the record class, populated with data from the Preferences table.
   * @throws IllegalArgumentException If {@code preferenceName} is empty or contains a {@code '/'}.
   * @throws IllegalStateException If {@code preferenceName} was used for a different record class.
   */
  public static <T extends Record> T fromPreferences(String preferenceName, Class<T> recordClass) {
    return fromPreferences(preferenceName, recordClass, null);
  }

  /**
   * Creates a record class instance of the provided type, with fields populated from Preferences.
   *
   * @param preferenceName Preference subtable to use to get the values.
   * @param recordClass Type of the record instance to populate from preferences.
   * @param configWithDefaults Record instance with all values set to their preferred default values
   *     (can be {@code null}).
   * @return An instance of the record class, populated with data from the Preferences table.
   */
  private static <T extends Record> T fromPreferences(
      String preferenceName, Class<T> recordClass, T configWithDefaults) {
    deleteLegacyKeys();
    validatePreferenceName(preferenceName);

    NetworkTableInstance ntInstance = Preferences.getNetworkTable().getInstance();
    verifyNotRegisteredToAnotherClass(ntInstance, preferenceName, recordClass);

    try {
      return createFromPreferences(preferenceName, recordClass, configWithDefaults);
    } catch (ReflectiveOperationException e) {
      if (throwExceptions) {
        throw new RuntimeException(e); // For self-tests.
      }
      DriverStation.reportWarning(
          String.format("Could not copy preferences into %s: %s", recordClass.getSimpleName(), e),
          e.getStackTrace());
      return configWithDefaults;
    }
  }

  private static void validatePreferenceName(String name) {
    if (name.isEmpty()) {
      throw new IllegalArgumentException("name cannot be empty");
    }
    if (name.indexOf(PATH_SEPARATOR) >= 0) {
      throw new IllegalArgumentException(String.format("name cannot contain '%c'", PATH_SEPARATOR));
    }
  }

  /**
   * Throws an exception if the given record class has been registered under a different name.
   *
   * @param ntInstance The network table instance that the preference is published to.
   * @param name Preference subtable that will be used to get the values.
   * @param recordClass Type of the record instance to populate from preferences.
   * @throws IllegalStateException If the subtable of the given name was registered to a different
   *     class.
   */
  private static void verifyNotRegisteredToAnotherClass(
      NetworkTableInstance ntInstance, String name, Class<? extends Record> recordClass) {
    String recordName = recordClass.getCanonicalName();
    if (recordName == null) {
      recordName = recordClass.getName();
    }

    NetworkTable registeredClassesTable = ntInstance.getTable(REGISTERED_CLASSES_NETWORK_TABLE_KEY);
    NetworkTableEntry entry = registeredClassesTable.getEntry(name);
    if (!entry.exists()) {
      entry.setString(recordName);
      entry.clearPersistent();
    } else {
      String registeredTo = entry.getString("");
      if (!recordName.equals(registeredTo)) {
        throw new IllegalStateException(
            String.format(
                "Preference with name '%s' already registered to %s", name, registeredTo));
      }
    }
  }

  /**
   * Creates an instance of the given type using the provided default values.
   *
   * @param prefix String to prepend to record field names to get the Preference key.
   * @param clazz Record class type.
   * @param configWithDefaults Default values to use if there are no values stored in NetworkTables.
   * @return An instance of the record class, populated with data from the Preferences table.
   * @throws ReflectiveOperationException If the fields of the class cannot be read via reflection.
   */
  private static <T> T createFromPreferences(
      String prefix, Class<? extends T> clazz, T configWithDefaults)
      throws ReflectiveOperationException {
    var components = clazz.getRecordComponents();
    Object[] params = new Object[components.length];
    Class<?>[] types = new Class[components.length];
    int i = 0;
    for (RecordComponent component : components) {
      String name = component.getName();
      String key = prefix + PATH_SEPARATOR + name;
      Class<?> type = component.getType();
      types[i] = type;

      boolean needComponentValue;
      PreferenceFetcher fetcher = null;
      boolean isRecordField = Record.class.isAssignableFrom(type);
      if (isRecordField) {
        needComponentValue = true;
      } else {
        fetcher = TYPE_TO_FETCHER.get(type);
        if (fetcher == null) {
          // Cannot get value from Preferences; copy over the value from the input record.
          needComponentValue = true;
        } else {
          needComponentValue = !Preferences.containsKey(key);
        }
      }

      Object componentValue = null;
      if (needComponentValue && configWithDefaults != null) {
        Field defaultValueField = clazz.getDeclaredField(name);
        defaultValueField.setAccessible(true);
        componentValue = defaultValueField.get(configWithDefaults);
      }

      if (isRecordField) {
        params[i] = createFromPreferences(key, type, componentValue);
      } else if (fetcher == null) {
        warn("Cannot store '%s' in Preferences; type %s is unsupported", name, type);
        params[i] = componentValue;
      } else {
        params[i] =
            fetcher.getValue(
                component, key, componentValue, /* initializePreference= */ needComponentValue);
      }
      i++;
    }
    Constructor<? extends T> constructor = clazz.getDeclaredConstructor(types);
    constructor.setAccessible(true);
    return constructor.newInstance(params);
  }

  /**
   * Type-safe functional interface for creating an instance of a type using data in Preferences.
   */
  @FunctionalInterface
  private interface GenericPreferenceFetcher<T> {
    /**
     * Gets a value from Preferences for the given component.
     *
     * @param component Provides dynamic access to the component of the record class.
     * @param key The Preference key that should be used when initializing the Preference.
     * @param defaultValue The default value that should be used when initializing the Preference.
     * @param initializePreference Whether the preference should be initialized.
     * @return The value; will match the type in "component";
     */
    T getValue(RecordComponent component, String key, T defaultValue, boolean initializePreference);
  }

  /**
   * Functional interface for creating an instance of a type using data in Preferences.
   *
   * <p>Note: this interface exists to avoid ugly casts in the code that uses the reflection APIs.
   */
  @FunctionalInterface
  private interface PreferenceFetcher {
    /**
     * Gets a value from Preferences for the given component.
     *
     * @param component Provides dynamic access to the component of the record class.
     * @param key The Preference key that should be used when initializing the Preference.
     * @param defaultValue The default value that should be used when initializing the Preference.
     * @param initializePreference Whether the preference should be initialized.
     * @return The value; will match the type in "component";
     */
    Object getValue(
        RecordComponent component, String key, Object defaultValue, boolean initializePreference);
  }

  private static final Map<Type, PreferenceFetcher> TYPE_TO_FETCHER = new HashMap<>();

  /**
   * Registers a preference fetcher with a type.
   *
   * @param type The type to register.
   * @param simpleFetcher The fetcher that should be used to create values of the given type.
   */
  @SuppressWarnings("unchecked")
  private static <T> void register(Class<T> type, GenericPreferenceFetcher<T> simpleFetcher) {
    PreferenceFetcher fetcher =
        (component, key, defaultValue, initializePreference) ->
            simpleFetcher.getValue(component, key, (T) defaultValue, initializePreference);
    TYPE_TO_FETCHER.put(type, fetcher);
  }

  static {
    register(Boolean.TYPE, PersistedConfiguration::booleanFetcher);
    register(BooleanSupplier.class, PersistedConfiguration::booleanSupplierFetcher);
    register(Integer.TYPE, PersistedConfiguration::intFetcher);
    register(IntSupplier.class, PersistedConfiguration::intSupplierFetcher);
    register(Long.TYPE, PersistedConfiguration::longFetcher);
    register(LongSupplier.class, PersistedConfiguration::longSupplierFetcher);
    register(Double.TYPE, PersistedConfiguration::doubleFetcher);
    register(DoubleSupplier.class, PersistedConfiguration::doubleSupplierFetcher);
    register(String.class, PersistedConfiguration::stringFetcher);
    register(Supplier.class, PersistedConfiguration::supplierFetcher);
  }

  /** Gets a boolean value from Preferences for the given component. */
  private static boolean booleanFetcher(
      RecordComponent component, String key, Boolean defaultValue, boolean initialize) {
    if (initialize) {
      if (defaultValue == null) {
        defaultValue = Boolean.FALSE;
      }
      Preferences.initBoolean(key, defaultValue);
      return defaultValue;
    }
    return Preferences.getBoolean(key, false);
  }

  /** Gets a BooleanSupplier value from Preferences for the given component. */
  private static BooleanSupplier booleanSupplierFetcher(
      RecordComponent component,
      String key,
      BooleanSupplier defaultValueSupplier,
      boolean initialize) {
    if (initialize) {
      boolean defaultValue = false;
      if (defaultValueSupplier != null) {
        defaultValue = defaultValueSupplier.getAsBoolean();
      }
      Preferences.initBoolean(key, defaultValue);
    }
    return () -> Preferences.getBoolean(key, false);
  }

  /** Gets an int value from Preferences for the given component. */
  private static int intFetcher(
      RecordComponent component, String key, Integer defaultValue, boolean initialize) {
    if (initialize) {
      if (defaultValue == null) {
        defaultValue = 0;
      }
      initIntegerPreference(key, defaultValue);
      return defaultValue;
    }
    return getIntegerPreference(key);
  }

  /** Gets a IntSupplier value from Preferences for the given component. */
  private static IntSupplier intSupplierFetcher(
      RecordComponent component, String key, IntSupplier defaultValueSupplier, boolean initialize) {
    if (initialize) {
      int defaultValue = defaultValueSupplier != null ? defaultValueSupplier.getAsInt() : 0;
      initIntegerPreference(key, defaultValue);
    }
    return () -> getIntegerPreference(key);
  }

  /** Gets a long value from Preferences for the given component. */
  private static long longFetcher(
      RecordComponent component, String key, Long defaultValue, boolean initialize) {
    if (initialize) {
      if (defaultValue == null) {
        defaultValue = 0L;
      }
      Preferences.initLong(key, defaultValue);
      return defaultValue;
    }
    return Preferences.getLong(key, 0);
  }

  /** Gets a LongSupplier value from Preferences for the given component. */
  private static LongSupplier longSupplierFetcher(
      RecordComponent component,
      String key,
      LongSupplier defaultValueSupplier,
      boolean initialize) {
    if (initialize) {
      long defaultValue = defaultValueSupplier != null ? defaultValueSupplier.getAsLong() : 0;
      Preferences.initLong(key, defaultValue);
    }
    return () -> Preferences.getLong(key, 0);
  }

  /** Gets a double value from Preferences for the given component. */
  private static double doubleFetcher(
      RecordComponent component, String key, Double defaultValue, boolean initialize) {
    if (initialize) {
      if (defaultValue == null) {
        defaultValue = 0.0;
      }
      Preferences.initDouble(key, defaultValue);
      return defaultValue;
    }
    return Preferences.getDouble(key, 0);
  }

  /** Gets a DoubleSupplier value from Preferences for the given component. */
  private static DoubleSupplier doubleSupplierFetcher(
      RecordComponent component,
      String key,
      DoubleSupplier defaultValueSupplier,
      boolean initialize) {
    if (initialize) {
      double defaultValue = defaultValueSupplier != null ? defaultValueSupplier.getAsDouble() : 0;
      Preferences.initDouble(key, defaultValue);
    }
    return () -> Preferences.getDouble(key, 0);
  }

  /** Gets a String value from Preferences for the given component. */
  private static String stringFetcher(
      RecordComponent component, String key, String defaultValue, boolean initialize) {
    if (initialize) {
      if (defaultValue == null) {
        defaultValue = "";
      }
      Preferences.initString(key, defaultValue);
      return defaultValue;
    }
    return Preferences.getString(key, "");
  }

  /**
   * Gets a Supplier&lt;String&gt; value from Preferences for the given component. Supports String,
   * long, int, boolean and float values.
   */
  private static Supplier<String> supplierFetcher(
      RecordComponent component,
      String key,
      Supplier<String> defaultValueSupplier,
      boolean initialize) {
    Type supplierType =
        ((ParameterizedType) component.getGenericType()).getActualTypeArguments()[0];
    if (!String.class.equals(supplierType)) {
      String formatString =
          initialize
              ? "Cannot store '%s' in Preferences; type %s is unsupported"
              : "Cannot read '%s' from Preferences; type %s is unsupported";
      warn(formatString, component.getName(), component.getGenericType());
      return defaultValueSupplier;
    }

    if (initialize) {
      String defaultValue = defaultValueSupplier != null ? defaultValueSupplier.get() : "";
      if (defaultValue == null) {
        defaultValue = "";
        warn("Cannot get initial value for '%s'; Supplier returned null", component.getName());
      }
      Preferences.initString(key, defaultValue);
    }
    return () -> Preferences.getString(key, "");
  }

  /** Deletes Preferences that were created by older versions of this class. */
  private static void deleteLegacyKeys() {
    if (!deletedLegacyKeys) {
      // Preferences installs a listener that makes all new topics persistent. The ".registeredTo"
      // topics used to be placed under /Preferences, so could have been persisted. They are now
      // written under a different top-level table. Delete the topics created by the previous code.
      for (var key : Preferences.getKeys()) {
        if (key.endsWith(".registeredTo")) {
          Preferences.remove(key);
        }
      }
      deletedLegacyKeys = true;
    }
  }

  /**
   * Puts the given int into the preferences table if it doesn't already exist.
   *
   * <p>Unlike with {@link Preferences#initInt(String, int)}, the value is stored as an integer, not
   * a double.
   *
   * @param key The key
   * @param value The value
   */
  private static void initIntegerPreference(String key, int value) {
    NetworkTable table = Preferences.getNetworkTable();
    NetworkTableEntry entry = table.getEntry(key);
    if (NetworkTableType.kDouble.equals(entry.getType())) {
      // If we get here that should mean there is a value for this key, so the default value should
      // be ignored. We update the default value just in case, if nothing else than to be
      // consistent with the Preferences "init" methods.
      entry.setDefaultDouble(value);
    } else {
      entry.setDefaultInteger(value);
    }
    entry.setPersistent();
  }

  /**
   * Returns the value at the given key, as an int. If this table does not have a value for that
   * position, then a value of zero will be returned.
   *
   * @param key the key
   * @return either the value in the table, or zero
   */
  private static int getIntegerPreference(String key) {
    NetworkTable table = Preferences.getNetworkTable();
    NetworkTableEntry entry = table.getEntry(key);
    return (int) entry.getInteger(0);
  }

  /**
   * Emits a warning, usually via {@link DataLogManager}.
   *
   * @param format A format string as described in {@link java.util.Formatter Formatter}.
   * @param args Arguments referenced by the format specifiers in the format string.
   */
  private static void warn(String format, Object... args) {
    String message = String.format("WARNING: " + format, args);
    errorReporter.accept(message);
  }

  private PersistedConfiguration() {
    throw new AssertionError("Not instantiable");
  }
}
