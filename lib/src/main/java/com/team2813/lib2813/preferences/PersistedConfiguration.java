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
 * Utility for constructing Java {@link Record} instances whose values are
 * persisted in WPILib's {@link Preferences} system.
 *
 * <p>This allows robot configuration values to be declared in record types
 * (which are immutable and self-documenting), but automatically backed by
 * NetworkTables Preferences so they can be updated live from Shuffleboard or
 * SmartDashboard. These persisted values will survive robot reboots and be
 * written to flash.
 *
 * <h2>Supported Component Types</h2>
 * Each record component (field) must be one of the following:
 * <ul>
 *   <li>{@code boolean}, {@link BooleanSupplier}, or {@link Supplier}&lt;{@link Boolean}&gt;</li>
 *   <li>{@code int}, {@link IntSupplier}, or {@link Supplier}&lt;{@link Integer}&gt;</li>
 *   <li>{@code long}, {@link LongSupplier}, or {@link Supplier}&lt;{@link Long}&gt;</li>
 *   <li>{@code double}, {@link DoubleSupplier}, or {@link Supplier}&lt;{@link Double}&gt;</li>
 *   <li>{@code String} or {@link Supplier}&lt;{@link String}&gt;</li>
 *   <li>Another {@code Record}, recursively following these rules</li>
 * </ul>
 *
 * <h2>Defaults</h2>
 * <ul>
 *   <li>If a record instance is provided, its values will be used as defaults for missing preferences.</li>
 *   <li>If no defaults are provided, Java primitive defaults are used (e.g. {@code false}, {@code 0}, {@code 0.0}).</li>
 *   <li>If a supplier is used (e.g. {@code DoubleSupplier}), the supplier will be invoked once to provide the default.</li>
 * </ul>
 *
 * <h2>Registry Enforcement</h2>
 * Each preference "namespace" (the {@code preferenceName} parameter) is bound to a single record class.
 * If code later attempts to bind the same namespace to a different record type, an exception is thrown.
 * This prevents accidental reuse of preference keys for incompatible types.
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * public final class Drive {
 *
 *   // Define a configuration record for drive parameters.
 *   public record DriveConfiguration(
 *       boolean addVisionMeasurements,
 *       long robotWeight,
 *       DoubleSupplier maxAngularVelocity) {
 *
 *     // Load persisted configuration, with default values provided here.
 *     public static DriveConfiguration fromPreferences() {
 *       DriveConfiguration defaults =
 *           new DriveConfiguration(true, 2813, () -> 3.14);
 *       return PersistedConfiguration.fromPreferences("Drive", defaults);
 *     }
 *   }
 * }
 * }</pre>
 *
 * <p>Preference keys created will be:
 * <ul>
 *   <li>{@code "Drive/addVisionMeasurements"} (default: {@code true})</li>
 *   <li>{@code "Drive/robotWeight"} (default: {@code 2813})</li>
 *   <li>{@code "Drive/maxAngularVelocity"} (default: {@code 3.14})</li>
 * </ul>
 *
 * <h2>Notes and Gotchas</h2>
 * <ul>
 *   <li>Records are immutable; values are read from Preferences each time {@code fromPreferences} is called.
 *       If live updates are needed, use Supplier-based components instead of raw primitives.</li>
 *   <li>Unsupported component types (e.g., {@code List}, {@code Map}, or custom classes) will log a warning,
 *       and the default value (if provided) will be used instead.</li>
 *   <li>Suppliers must not return {@code null}. If they do, a warning is logged and the default supplier is used.</li>
 *   <li>{@link Preferences} keys cannot contain the path separator character (usually {@code '/'});
 *       if they do, an {@link IllegalArgumentException} is thrown.</li>
 *   <li>This class is {@code final} and cannot be instantiated—only static factory methods are provided.</li>
 * </ul>
 *
 * @author Team 2813
 * @since 2.0.0
 */
public final class PersistedConfiguration {
  /** Key used to track which record classes are bound to preference namespaces. */
  static final String REGISTERED_CLASSES_NETWORK_TABLE_KEY = "PersistedConfiguration/registry";

  /** Whether legacy keys have already been cleaned once per program run. */
  private static boolean deletedLegacyKeys = false;

  // Package-private fields for self-tests.
  static boolean throwExceptions = false;
  static Consumer<String> errorReporter = DataLogManager::log;

  /**
   * Constructs a record instance, populating its components from Preferences, using the provided instance to get default values.
   *
   * <p>The provided instance supplies the default values. For each component:
   * <ul>
   *   <li>If a corresponding preference already exists, that preference value is used.</li>
   *   <li>If no preference exists, the component value from {@code configWithDefaults} is stored
   *       into Preferences and used as the returned value.</li>
   * </ul>
   *
   * @param preferenceName Subtable name under Preferences (must not contain '/')
   * @param configWithDefaults Instance containing default values for all record components
   * @return A new record instance populated with Preferences values
   * @throws IllegalArgumentException if {@code preferenceName} is empty or contains '/'
   * @throws IllegalStateException if {@code preferenceName} was already registered to a different record class
   */
  public static <T extends Record> T fromPreferences(String preferenceName, T configWithDefaults) {
    @SuppressWarnings("unchecked")
    Class<T> recordClass = (Class<T>) configWithDefaults.getClass();
    return fromPreferences(preferenceName, recordClass, configWithDefaults);
  }

  /**
   * Construct a record instance with values loaded from Preferences,
   * using Java defaults if no explicit defaults are provided.
   *
   * <p>For example, an {@code int} component defaults to {@code 0},
   * and a {@code double} component defaults to {@code 0.0}.
   *
   * @param preferenceName Subtable name under Preferences
   * @param recordClass Record type to instantiate
   * @return A new record instance populated with Preferences values
   * @throws IllegalArgumentException if {@code preferenceName} is invalid
   * @throws IllegalStateException if namespace was registered to another class
   */
  public static <T extends Record> T fromPreferences(String preferenceName, Class<T> recordClass) {
    return fromPreferences(preferenceName, recordClass, null);
  }

  /**
   * Internal shared implementation for record construction.
   *
   * @param preferenceName Preference subtable name
   * @param recordClass Record type
   * @param configWithDefaults Optional default instance (may be null)
   */
  private static <T extends Record> T fromPreferences(
      String preferenceName, Class<T> recordClass, T configWithDefaults) {
    deleteLegacyKeys();
    NetworkTableInstance ntInstance = NetworkTableInstance.getDefault();
    validatePreferenceName(preferenceName);
    verifyNotRegisteredToAnotherClass(ntInstance, preferenceName, recordClass);

    try {
      return createFromPreferences(preferenceName, recordClass, configWithDefaults);
    } catch (ReflectiveOperationException e) {
      if (throwExceptions) {
        throw new RuntimeException(e); // For unit tests
      }
      DriverStation.reportWarning(
          String.format("Could not copy preferences into %s: %s",
              recordClass.getSimpleName(), e),
          e.getStackTrace());
      return configWithDefaults;
    }
  }

  /**
   * Validates that a preference name is legal (non-empty and does not contain path separators).
   */
  private static void validatePreferenceName(String name) {
    if (name.isEmpty()) {
      throw new IllegalArgumentException("name cannot be empty");
    }
    if (name.indexOf(PATH_SEPARATOR) >= 0) {
      throw new IllegalArgumentException(
          String.format("name cannot contain '%c'", PATH_SEPARATOR));
    }
  }

  /**
   * Ensures the given preference namespace is not already registered to another record class.
   *
   * <p>Each namespace is stored in a special NetworkTable registry entry. If the namespace
   * is new, it is bound to the current record type. If it already exists and points to
   * a different record type, this method throws an exception.
   */
  private static void verifyNotRegisteredToAnotherClass(
      NetworkTableInstance ntInstance, String name, Class<? extends Record> recordClass) {
    String recordName = recordClass.getCanonicalName();
    if (recordName == null) {
      recordName = recordClass.getName(); // fallback if canonical name unavailable
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
                "Preference with name '%s' already registered to %s",
                name, registeredTo));
      }
    }
  }

  /**
   * Core factory logic: instantiate a record using reflection,
   * reading each component value from Preferences (or defaults).
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
      String key = prefix + PATH_SEPARATOR + name; // Preference key = namespace/componentName
      Class<?> type = component.getType();
      types[i] = type;

      boolean needComponentValue;
      PreferenceFactory factory = null;
      boolean isRecordField = Record.class.isAssignableFrom(type);

      if (isRecordField) {
        // Nested record: recurse into sub-record
        needComponentValue = true;
      } else {
        factory = TYPE_TO_FACTORY.get(type);
        if (factory == null) {
          // Unsupported type: fall back to copying default value
          needComponentValue = true;
        } else {
          // If no key exists, we need the component value for initialization
          needComponentValue = !Preferences.containsKey(key);
        }
      }

      Object componentValue = null;
      if (needComponentValue && configWithDefaults != null) {
        // Use reflection to get the field value from the default record instance
        Field defaultValueField = clazz.getDeclaredField(name);
        defaultValueField.setAccessible(true);
        componentValue = defaultValueField.get(configWithDefaults);
      }

      if (isRecordField) {
        params[i] = createFromPreferences(key, type, componentValue);
      } else if (factory == null) {
        warn("Cannot store '%s' in Preferences; type %s is unsupported", name, type);
        params[i] = componentValue;
      } else {
        // Use registered factory to create the value (from Preferences or defaults)
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

  /**
   * Functional interface mapping a record component into a preference-backed value.
   */
  @FunctionalInterface
  private interface PreferenceFactory {
    Object create(
        RecordComponent component, String key, Object defaultValue, boolean initializePreference);
  }

  /**
   * Generic variant of {@link PreferenceFactory} with typed defaults.
   */
  @FunctionalInterface
  private interface GenericPreferenceFactory<T> {
    T create(RecordComponent component, String key, T defaultValue, boolean initializePreference);
  }

  /** Registry of supported record component types to their corresponding factories. */
  private static final Map<Type, PreferenceFactory> TYPE_TO_FACTORY = new HashMap<>();

  @SuppressWarnings("unchecked")
  private static <T> void register(Class<T> type, GenericPreferenceFactory<T> simpleFactory) {
    PreferenceFactory factory =
        (component, key, defaultValue, initializePreference) ->
            simpleFactory.create(component, key, (T) defaultValue, initializePreference);
    TYPE_TO_FACTORY.put(type, factory);
  }

  // Static initialization: register supported primitive and supplier types
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

  /** Maps boxed types to the primitive/supported type used for preferences. */
  private static final Map<Type, Type> SUPPLIER_TYPE_TO_REGISTERED_TYPE =
      Map.of(
          Boolean.class, Boolean.TYPE,
          Integer.class, Integer.TYPE,
          Long.class, Long.TYPE,
          Double.class, Double.TYPE,
          String.class, String.class);

  // ===============================
  // FACTORY IMPLEMENTATIONS
  // ===============================

  private static boolean booleanFactory(
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

  private static BooleanSupplier booleanSupplierFactory(
      RecordComponent component,
      String key,
      BooleanSupplier defaultValueSupplier,
      boolean initialize) {
    if (initialize) {
      boolean defaultValue = defaultValueSupplier != null
          ? defaultValueSupplier.getAsBoolean()
          : false;
      Preferences.initBoolean(key, defaultValue);
    }
    return () -> Preferences.getBoolean(key, false);
  }

  private static int intFactory(
      RecordComponent component, String key, Integer defaultValue, boolean initialize) {
    if (initialize) {
      if (defaultValue == null) {
        defaultValue = 0;
      }
      Preferences.initInt(key, defaultValue);
      return defaultValue;
    }
    return Preferences.getInt(key, 0);
  }

  private static IntSupplier intSupplierFactory(
      RecordComponent component, String key, IntSupplier defaultValueSupplier, boolean initialize) {
    if (initialize) {
      int defaultValue = defaultValueSupplier != null ? defaultValueSupplier.getAsInt() : 0;
      Preferences.initInt(key, defaultValue);
    }
    return () -> Preferences.getInt(key, 0);
  }

  private static long longFactory(
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

  private static double doubleFactory(
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

  private static String stringFactory(
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

  private static Supplier<?> supplierFactory(
      RecordComponent component, String key, Supplier<?> defaultValueSupplier, boolean initialize) {
    Type supplierType =
        ((ParameterizedType) component.getGenericType()).getActualTypeArguments()[0];
    Type registeredType = SUPPLIER_TYPE_TO_REGISTERED_TYPE.get(supplierType);
    if (registeredType == null) {
      warn("Cannot store '%s' in Preferences; type %s is unsupported",
          component.getName(), component.getGenericType());
      return defaultValueSupplier;
    }
    PreferenceFactory factory = TYPE_TO_FACTORY.get(registeredType);

    if (initialize) {
      Object defaultValue = null;
      if (defaultValueSupplier != null) {
        defaultValue = defaultValueSupplier.get();
        if (defaultValue == null) {
          warn("Cannot store '%s' in Preferences; default value is null",
              component.getName());
          return defaultValueSupplier;
        }
      }
      factory.create(component, key, defaultValue, true);
    }

    return () -> factory.create(component, key, null, false);
  }

  /**
   * Removes legacy ".registeredTo" keys from Preferences (used by older versions of this class).
   * Ensures backward compatibility without polluting the Preferences namespace.
   */
  private static void deleteLegacyKeys() {
    if (!deletedLegacyKeys) {
      for (var key : Preferences.getKeys()) {
        if (key.endsWith(".registeredTo")) {
          Preferences.remove(key);
        }
      }
      deletedLegacyKeys = true;
    }
  }

  /**
   * Utility method for reporting warnings. Logs to the configured {@link #errorReporter}.
   */
  private static void warn(String format, Object... args) {
    String message = String.format("WARNING: " + format, args);
    errorReporter.accept(message);
  }

  /** Private constructor to prevent instantiation. */
  private PersistedConfiguration() {
    throw new AssertionError("Not instantiable");
  }
}
