package com.team2813.lib2813.preferences;

import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Preferences;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;

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
 */
public class PreferencesInjector {
  /**
   * Injector instance that removes "com.team2813." from class names when creating prefence key
   * names.
   */
  public static final PreferencesInjector DEFAULT_INSTANCE =
      new PreferencesInjector("com.team2813.");

  private final String removePrefix;
  private final int removePrefixLen;

  // The below package-scope fields are for the self-tests.
  boolean throwExceptions = false;
  Consumer<String> errorReporter = DataLogManager::log;

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
    @SuppressWarnings("unchecked")
    Class<? extends T> clazz = (Class<? extends T>) configWithDefaults.getClass();

    try {
      return injectPreferences(clazz, configWithDefaults);
    } catch (ReflectiveOperationException e) {
      if (throwExceptions) {
        throw new RuntimeException(e); // For self-tests.
      }
      DriverStation.reportWarning(
          String.format("Could not inject preferences into %s: %s", clazz.getSimpleName(), e),
          e.getStackTrace());
      return configWithDefaults;
    }
  }

  /**
   * Creates a preference key for the given record component.
   *
   * <p>This method is protected and non-final so subclasses can change the default behavior.
   */
  protected String createKey(RecordComponent component) {
    Class<?> recordClass = component.getDeclaringRecord();
    String recordName = recordClass.getCanonicalName();
    if (recordName == null) {
      recordName = recordClass.getName();
    }
    String componentName = component.getName();
    if (recordName.startsWith(this.removePrefix)) {
      recordName = recordName.substring(this.removePrefixLen);
      if (recordName.isEmpty()) {
        return componentName;
      }
    }
    return recordName + "." + componentName;
  }

  private <T extends java.lang.Record> T injectPreferences(
      Class<? extends T> clazz, T configWithDefaults) throws ReflectiveOperationException {
    var components = clazz.getRecordComponents();
    Object[] params = new Object[components.length];
    Class<?>[] types = new Class[components.length];
    int i = 0;
    for (RecordComponent component : components) {
      String name = component.getName();
      Class<?> type = component.getType();
      types[i] = type;

      Object defaultValue = null;
      String key = null;
      boolean getDefaultValue;

      PreferenceFactory factory = TYPE_TO_FACTORY.get(type);
      if (factory == null) {
        warn("Cannot store '%s' in Preferences; type %s is unsupported", name, type);
        getDefaultValue = true;
      } else {
        key = createKey(component);
        getDefaultValue = !Preferences.containsKey(key);
      }
      if (getDefaultValue) {
        Field defaultValueField = clazz.getDeclaredField(name);
        defaultValueField.setAccessible(true);
        defaultValue = defaultValueField.get(configWithDefaults);
      }

      if (factory == null) {
        params[i] = defaultValue;
      } else if (getDefaultValue && defaultValue == null) {
        warn("Cannot store '%s' in Preferences; default value is null", name);
        params[i] = null;
      } else {
        // Fetch the value from Preferences
        params[i] = factory.create(this, component, key, defaultValue);
      }
      i++;
    }
    return clazz.getDeclaredConstructor(types).newInstance(params);
  }

  @FunctionalInterface
  private interface PreferenceFactory {
    Object create(
        PreferencesInjector injector, RecordComponent component, String key, Object defaultValue);
  }

  @FunctionalInterface
  private interface GenericPreferenceFactory<T> {
    T create(PreferencesInjector injector, RecordComponent component, String key, T defaultValue);
  }

  private static final Map<Type, PreferenceFactory> TYPE_TO_FACTORY = new HashMap<>();

  @SuppressWarnings("unchecked")
  private static <T> void register(Class<T> type, GenericPreferenceFactory<T> simpleFactory) {
    PreferenceFactory factory =
        (injector, component, key, defaultValue) ->
            simpleFactory.create(injector, component, key, (T) defaultValue);
    TYPE_TO_FACTORY.put(type, factory);
  }

  static {
    register(Boolean.TYPE, PreferencesInjector::booleanFactory);
    register(BooleanSupplier.class, PreferencesInjector::booleanSupplierFactory);
    register(Integer.TYPE, PreferencesInjector::intFactory);
    register(IntSupplier.class, PreferencesInjector::intSupplierFactory);
    register(Long.TYPE, PreferencesInjector::longFactory);
    register(LongSupplier.class, PreferencesInjector::longSupplierFactory);
    register(Double.TYPE, PreferencesInjector::doubleFactory);
    register(DoubleSupplier.class, PreferencesInjector::doubleSupplierFactory);
    register(String.class, PreferencesInjector::stringFactory);
    register(Supplier.class, PreferencesInjector::supplierFactory);
  }

  /** Maps the generic types supported by Preferences to their primitive types. */
  private static final Map<Type, Type> WRAPPER_TO_PRIMITIVE =
      Map.of(
          Boolean.class, Boolean.TYPE,
          Integer.class, Integer.TYPE,
          Long.class, Long.TYPE,
          Double.class, Double.TYPE,
          String.class, String.class);

  /** Gets a boolean value from Preferences for the given component. */
  private static boolean booleanFactory(
      PreferencesInjector injector, RecordComponent component, String key, Boolean defaultValue) {
    if (defaultValue != null) {
      Preferences.initBoolean(key, defaultValue);
    }
    return Preferences.getBoolean(key, false);
  }

  /** Gets a BooleanSupplier value from Preferences for the given component. */
  private static BooleanSupplier booleanSupplierFactory(
      PreferencesInjector injector,
      RecordComponent component,
      String key,
      BooleanSupplier defaultValueSupplier) {
    if (defaultValueSupplier != null) {
      boolean defaultValue = defaultValueSupplier.getAsBoolean();
      Preferences.initBoolean(key, defaultValue);
    }
    return () -> Preferences.getBoolean(key, false);
  }

  /** Gets an int value from Preferences for the given component. */
  private static int intFactory(
      PreferencesInjector injector, RecordComponent component, String key, Integer defaultValue) {
    if (defaultValue != null) {
      Preferences.initInt(key, defaultValue);
    }
    return Preferences.getInt(key, 0);
  }

  /** Gets a IntSupplier value from Preferences for the given component. */
  private static IntSupplier intSupplierFactory(
      PreferencesInjector injector,
      RecordComponent component,
      String key,
      IntSupplier defaultValueSupplier) {
    if (defaultValueSupplier != null) {
      int defaultValue = defaultValueSupplier.getAsInt();
      Preferences.initInt(key, defaultValue);
    }
    return () -> Preferences.getInt(key, 0);
  }

  /** Gets a long value from Preferences for the given component. */
  private static long longFactory(
      PreferencesInjector injector, RecordComponent component, String key, Long defaultValue) {
    if (defaultValue != null) {
      Preferences.initLong(key, defaultValue);
    }
    return Preferences.getLong(key, 0);
  }

  /** Gets a LongSupplier value from Preferences for the given component. */
  private static LongSupplier longSupplierFactory(
      PreferencesInjector injector,
      RecordComponent component,
      String key,
      LongSupplier defaultValueSupplier) {
    if (defaultValueSupplier != null) {
      long defaultValue = defaultValueSupplier.getAsLong();
      Preferences.initLong(key, defaultValue);
    }
    return () -> Preferences.getLong(key, 0);
  }

  /** Gets a double value from Preferences for the given component. */
  private static double doubleFactory(
      PreferencesInjector injector, RecordComponent component, String key, Double defaultValue) {
    if (defaultValue != null) {
      Preferences.initDouble(key, defaultValue);
    }
    return Preferences.getDouble(key, 0);
  }

  /** Gets a DoubleSupplier value from Preferences for the given component. */
  private static DoubleSupplier doubleSupplierFactory(
      PreferencesInjector injector,
      RecordComponent component,
      String key,
      DoubleSupplier defaultValueSupplier) {
    if (defaultValueSupplier != null) {
      double defaultValue = defaultValueSupplier.getAsDouble();
      Preferences.initDouble(key, defaultValue);
    }
    return () -> Preferences.getDouble(key, 0);
  }

  /** Gets a String value from Preferences for the given component. */
  private static String stringFactory(
      PreferencesInjector injector, RecordComponent component, String key, String defaultValue) {
    if (defaultValue != null) {
      Preferences.initString(key, defaultValue);
    }
    return Preferences.getString(key, "");
  }

  /**
   * Gets a Supplier value from Preferences for the given component. Supports String, long, int,
   * boolean and float values.
   */
  private static Supplier<?> supplierFactory(
      PreferencesInjector injector,
      RecordComponent component,
      String key,
      Supplier<?> defaultValueSupplier) {
    Type supplierType =
        ((ParameterizedType) component.getGenericType()).getActualTypeArguments()[0];
    Type supplierPrimativeType = WRAPPER_TO_PRIMITIVE.get(supplierType);
    if (supplierPrimativeType == null) {
      injector.warn(
          "Cannot store '%s' in Preferences; type %s is unsupported",
          component.getName(), component.getGenericType());
      return defaultValueSupplier;
    }
    PreferenceFactory factory = TYPE_TO_FACTORY.get(supplierPrimativeType);

    if (defaultValueSupplier != null) {
      Object defaultValue = defaultValueSupplier.get();
      if (defaultValue == null) {
        injector.warn(
            "Cannot store '%s' in Preferences; default value is null", component.getName());
        return defaultValueSupplier;
      }
      factory.create(
          injector, component, key, defaultValue); // Call Preferences.init{String,Double,etc}()
    }
    return () -> factory.create(injector, component, key, null);
  }

  private void warn(String format, Object... args) {
    String message = String.format("WARNING: " + format, args);
    errorReporter.accept(message);
  }
}
