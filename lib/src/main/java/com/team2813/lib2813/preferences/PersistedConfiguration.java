package com.team2813.lib2813.preferences;

import static edu.wpi.first.networktables.NetworkTable.PATH_SEPARATOR;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
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
 * <p>The Preference values can be updated in the SmartDashboard and/or Shuffleboard UI; updated
 * values will be stored in the flash storage for the robot.
 *
 * <p>Example use:
 *
 * {@snippet :
 * public final class Drive {
 *
 *   public record DriveConfiguration(
 *       boolean addVisionMeasurements, long robotWeight,
 *       DoubleSupplier powerMultiplier) {
 *
 *     public static DriveConfiguration fromPreferences() {
 *       return PersistedConfiguration.fromPreferences("Drive", DriveConfiguration.class);
 *     }
 *   }
 * }
 * }
 *
 * <p>In the above example, {@code fromPreferences()} would return a record instance with the values
 * populated the "Preferences" NetworkTables table. The keys would be:
 *
 * <ul>
 *   <li>{@code "Drive/addVisionMeasurements"}
 *   <li>{@code "Drive/robotWeight"}
 *   <li>{@code "Drive/powerMultiplier"}
 * </ul>
 *
 * <p>If no value is stored in Preferences for a key, the default value returned (and initialized in
 * Preferences) would be the default value for the type of the record component. In the above
 * example, if none of the above preference keys existed, preferences will be created with the
 * following values:
 *
 * <ul>
 *   <li>{@code "Drive/addVisionMeasurements"}: {@code false}
 *   <li>{@code "Drive/robotWeight"}: 0
 *   <li>{@code "Drive/powerMultiplier"}: 0.0
 * </ul>
 *
 * <p>The caller could specify different default values by passing an instance of the record class:
 *
 * {@snippet :
 * public final class Drive {
 *
 *   public record DriveConfiguration(
 *       boolean addVisionMeasurements, long robotWeight,
 *       DoubleSupplier powerMultiplier) {
 *
 *     public static DriveConfiguration fromPreferences() {
 *       DriveConfiguration defaultConfig = new DriveConfiguration(
 *           true, 1337, () -> 3.14);
 *       return PersistedConfiguration.fromPreferences("Drive", defaultConfig);
 *     }
 *   }
 * }
 * }
 *
 * <p>In the above example, {@code fromPreferences()} would return a record instance with the values
 * populated the "Preferences" NetworkTables table. The keys and default values would be:
 *
 * <ul>
 *   <li>{@code "Drive/addVisionMeasurements"} (default value: {@code true})
 *   <li>{@code "Drive/robotWeight"} (default value: {@code 1337})
 *   <li>{@code "Drive/powerMultiplier"} (default value: {@code 3.14})
 * </ul>
 *
 * <p>For record classes with many component values of the same type, it is strongly recommended
 * that a builder is provided to construct the record, to avoid callers passing the parameters in
 * the wrong order. To make generation of a builder easier, consider using <a
 * href="https://github.com/google/auto/blob/main/value/userguide/autobuilder.md">{@code @AutoBuilder}</a>
 * from Google Auto or <a href="https://projectlombok.org/features/Builder">{@code @Builder}</a>
 * from Project Lombok. Note that {@code PersistedConfiguration} will use the default record
 * constructor to create record instances, so any parameter validation should be done in a custom
 * constructor; see <a href="https://www.baeldung.com/java-records-custom-constructor">Custom
 * Constructor in Java Records</a> for details.
 *
 * @since 1.3.0
 */
public final class PersistedConfiguration {
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
   * @param preferenceName Preference subtable to use to get the values.
   * @param configWithDefaults Record instance with all values set to their preferred default
   *     values.
   * @throws IllegalArgumentException If {@code preferenceName} is empty or contains a {@code '/'}.
   * @throws IllegalStateException If {@code preferenceName} was used for a different record class.
   */
  public static <T extends Record> T fromPreferences(String preferenceName, T configWithDefaults) {
    return fromPreferences(preferenceName, configWithDefaults, PATH_SEPARATOR);
  }

  /**
   * Creates a record class instance of the provided type, with fields populated from Preferences.
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
   * <p>The default values for the preferences will be Java defaults (for example, zero for
   * integers).
   *
   * @param preferenceName Preference subtable to use to get the values.
   * @param recordClass Type of the record instance to populate from preferences.
   * @throws IllegalArgumentException If {@code preferenceName} is empty or contains a {@code '/'}.
   * @throws IllegalStateException If {@code preferenceName} was used for a different record class.
   */
  public static <T extends Record> T fromPreferences(String preferenceName, Class<T> recordClass) {
    return fromPreferences(preferenceName, recordClass, null, PATH_SEPARATOR);
  }

  static <T extends Record> T fromPreferences(
      String preferenceName, T configWithDefaults, char pathSeparator) {
    @SuppressWarnings("unchecked")
    Class<T> recordClass = (Class<T>) configWithDefaults.getClass();
    return fromPreferences(preferenceName, recordClass, configWithDefaults, pathSeparator);
  }

  private static <T extends Record> T fromPreferences(
      String preferenceName, Class<T> recordClass, T configWithDefaults, char pathSeparator) {
    NetworkTableInstance ntInstance = NetworkTableInstance.getDefault();
    validatePreferenceName(preferenceName);
    verifyNotRegisteredToAnotherClass(ntInstance, preferenceName, recordClass);

    try {
      return createFromPreferences(preferenceName, recordClass, configWithDefaults, pathSeparator);
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

  private static void verifyNotRegisteredToAnotherClass(
      NetworkTableInstance ntInstance, String name, Class<? extends Record> recordClass) {
    String recordName = recordClass.getCanonicalName();
    if (recordName == null) {
      recordName = recordClass.getName();
    }

    NetworkTable preferencesTable = ntInstance.getTable("Preferences");
    NetworkTableEntry entry =
        preferencesTable.getEntry(String.format("%s%c.registeredTo", name, PATH_SEPARATOR));
    if (!entry.exists()) {
      entry.setString(recordName);
    } else {
      String registeredTo = entry.getString("");
      if (!recordName.equals(registeredTo)) {
        throw new IllegalStateException(
            String.format(
                "Preference with name '%s' already registered to %s", name, registeredTo));
      }
    }
  }

  private static <T> T createFromPreferences(
      String prefix, Class<? extends T> clazz, T configWithDefaults, char pathSeparator)
      throws ReflectiveOperationException {
    var components = clazz.getRecordComponents();
    Object[] params = new Object[components.length];
    Class<?>[] types = new Class[components.length];
    int i = 0;
    for (RecordComponent component : components) {
      String name = component.getName();
      String key = prefix + pathSeparator + name;
      Class<?> type = component.getType();
      types[i] = type;

      boolean needComponentValue;
      PreferenceFactory factory = null;
      boolean isRecordField = Record.class.isAssignableFrom(type);
      if (isRecordField) {
        needComponentValue = true;
      } else {
        factory = TYPE_TO_FACTORY.get(type);
        if (factory == null) {
          // Cannot get value from Preferences; copy over the value from the input record.
          needComponentValue = true;
        } else {
          needComponentValue = !Preferences.containsKey(key);
        }
      }

      Object componentValue = null;
      if (needComponentValue) {
        if (configWithDefaults != null) {
          Field defaultValueField = clazz.getDeclaredField(name);
          defaultValueField.setAccessible(true);
          componentValue = defaultValueField.get(configWithDefaults);
        }
      }

      if (isRecordField) {
        params[i] = createFromPreferences(key, type, componentValue, pathSeparator);
      } else if (factory == null) {
        warn("Cannot store '%s' in Preferences; type %s is unsupported", name, type);
        params[i] = componentValue;
      } else {
        // Fetch the value from Preferences
        params[i] =
            factory.create(
                component, key, componentValue, /* initializePreference= */ needComponentValue);
      }
      i++;
    }
    Constructor<? extends T> constructor = clazz.getDeclaredConstructor(types);
    constructor.setAccessible(true);
    return constructor.newInstance(params);
  }

  @FunctionalInterface
  private interface PreferenceFactory {
    Object create(
        RecordComponent component, String key, Object defaultValue, boolean initializePreference);
  }

  @FunctionalInterface
  private interface GenericPreferenceFactory<T> {
    T create(RecordComponent component, String key, T defaultValue, boolean initializePreference);
  }

  private static final Map<Type, PreferenceFactory> TYPE_TO_FACTORY = new HashMap<>();

  @SuppressWarnings("unchecked")
  private static <T> void register(Class<T> type, GenericPreferenceFactory<T> simpleFactory) {
    PreferenceFactory factory =
        (component, key, defaultValue, initializePreference) ->
            simpleFactory.create(component, key, (T) defaultValue, initializePreference);
    TYPE_TO_FACTORY.put(type, factory);
  }

  static {
    register(Boolean.TYPE, PersistedConfiguration::booleanFactory);
    register(BooleanSupplier.class, PersistedConfiguration::booleanSupplierFactory);
    register(Integer.TYPE, PersistedConfiguration::intFactory);
    register(IntSupplier.class, PersistedConfiguration::intSupplierFactory);
    register(Long.TYPE, PersistedConfiguration::longFactory);
    register(LongSupplier.class, PersistedConfiguration::longSupplierFactory);
    register(Double.TYPE, PersistedConfiguration::doubleFactory);
    register(DoubleSupplier.class, PersistedConfiguration::doubleSupplierFactory);
    register(String.class, PersistedConfiguration::stringFactory);
    register(Supplier.class, PersistedConfiguration::supplierFactory);
  }

  /** Maps the generic types supported by Preferences to their primitive types. */
  private static final Map<Type, Type> SUPPLIER_TYPE_TO_REGISTERED_TYPE =
      Map.of(
          Boolean.class, Boolean.TYPE,
          Integer.class, Integer.TYPE,
          Long.class, Long.TYPE,
          Double.class, Double.TYPE,
          String.class, String.class);

  /** Gets a boolean value from Preferences for the given component. */
  private static boolean booleanFactory(
      RecordComponent component, String key, Boolean defaultValue, boolean initialize) {
    if (initialize) {
      if (defaultValue == null) {
        defaultValue = Boolean.FALSE;
      }
      Preferences.initBoolean(key, defaultValue);
    }
    return Preferences.getBoolean(key, false);
  }

  /** Gets a BooleanSupplier value from Preferences for the given component. */
  private static BooleanSupplier booleanSupplierFactory(
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
  private static int intFactory(
      RecordComponent component, String key, Integer defaultValue, boolean initialize) {
    if (initialize) {
      if (defaultValue == null) {
        defaultValue = 0;
      }
      Preferences.initInt(key, defaultValue);
    }
    return Preferences.getInt(key, 0);
  }

  /** Gets a IntSupplier value from Preferences for the given component. */
  private static IntSupplier intSupplierFactory(
      RecordComponent component, String key, IntSupplier defaultValueSupplier, boolean initialize) {
    if (initialize) {
      int defaultValue = defaultValueSupplier != null ? defaultValueSupplier.getAsInt() : 0;
      Preferences.initInt(key, defaultValue);
    }
    return () -> Preferences.getInt(key, 0);
  }

  /** Gets a long value from Preferences for the given component. */
  private static long longFactory(
      RecordComponent component, String key, Long defaultValue, boolean initialize) {
    if (initialize) {
      if (defaultValue == null) {
        defaultValue = 0L;
      }
      Preferences.initLong(key, defaultValue);
    }
    return Preferences.getLong(key, 0);
  }

  /** Gets a LongSupplier value from Preferences for the given component. */
  private static LongSupplier longSupplierFactory(
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
  private static double doubleFactory(
      RecordComponent component, String key, Double defaultValue, boolean initialize) {
    if (initialize) {
      if (defaultValue == null) {
        defaultValue = 0.0;
      }
      Preferences.initDouble(key, defaultValue);
    }
    return Preferences.getDouble(key, 0);
  }

  /** Gets a DoubleSupplier value from Preferences for the given component. */
  private static DoubleSupplier doubleSupplierFactory(
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
  private static String stringFactory(
      RecordComponent component, String key, String defaultValue, boolean initialize) {
    if (initialize) {
      if (defaultValue == null) {
        defaultValue = "";
      }
      Preferences.initString(key, defaultValue);
    }
    return Preferences.getString(key, "");
  }

  /**
   * Gets a Supplier value from Preferences for the given component. Supports String, long, int,
   * boolean and float values.
   */
  private static Supplier<?> supplierFactory(
      RecordComponent component, String key, Supplier<?> defaultValueSupplier, boolean initialize) {
    Type supplierType =
        ((ParameterizedType) component.getGenericType()).getActualTypeArguments()[0];
    Type registeredType = SUPPLIER_TYPE_TO_REGISTERED_TYPE.get(supplierType);
    if (registeredType == null) {
      warn(
          "Cannot store '%s' in Preferences; type %s is unsupported",
          component.getName(), component.getGenericType());
      return defaultValueSupplier;
    }
    PreferenceFactory factory = TYPE_TO_FACTORY.get(registeredType);

    if (initialize) {
      Object defaultValue = null;
      if (defaultValueSupplier != null) {
        defaultValue = defaultValueSupplier.get();
        if (defaultValue == null) {
          warn("Cannot store '%s' in Preferences; default value is null", component.getName());
          return defaultValueSupplier;
        }
      }
      factory.create(
          component, key, defaultValue, true); // Call Preferences.init{String,Double,etc}()
    }

    return () -> factory.create(component, key, null, false);
  }

  private static void warn(String format, Object... args) {
    String message = String.format("WARNING: " + format, args);
    errorReporter.accept(message);
  }

  private static <T> T createInstanceWithJavaDefaults(Class<T> clazz)
      throws ReflectiveOperationException {
    if (clazz.isPrimitive()) {
      return clazz.cast(Array.get(Array.newInstance(clazz, 1), 0));
    }
    if (Record.class.isAssignableFrom(clazz)) {
      var components = clazz.getRecordComponents();
      Object[] params = new Object[components.length];
      Class<?>[] types = new Class[components.length];
      int i = 0;
      for (RecordComponent component : components) {
        Class<?> type = component.getType();
        types[i] = type;
        params[i] = createInstanceWithJavaDefaults(type);
      }
      return clazz.getDeclaredConstructor(types).newInstance(params);
    }
    throw new IllegalArgumentException("Unsupported type: " + clazz);
  }

  private PersistedConfiguration() {
    throw new AssertionError("Not instantiable");
  }
}
