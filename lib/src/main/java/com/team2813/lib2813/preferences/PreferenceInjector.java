package com.team2813.lib2813.preferences;

import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Preferences;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;

public class PreferenceInjector {
  public static final PreferenceInjector DEFAULT_INSTANCE = new PreferenceInjector("com.team2813.");
  private final String removePrefix;
  private final int removePrefixLen;

  // The below package-scope fields are for the self-tests.
  boolean throwExceptions = false;
  Consumer<String> errorReporter = DataLogManager::log;

  public PreferenceInjector(String removePrefix) {
    this.removePrefix = removePrefix;
    this.removePrefixLen = removePrefix.length();
  }

  /**
   * Creates an instance of the given record class with all fields populated from preferences.
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

  /** Creates a preference key for the given record component. */
  protected String createKey(RecordComponent component) {
    String prefix = component.getDeclaringRecord().getCanonicalName();
    String name = component.getName();
    if (prefix.startsWith(this.removePrefix)) {
      prefix = prefix.substring(this.removePrefixLen);
      if (prefix.isEmpty()) {
        return name;
      }
    }
    return prefix + "." + name;
  }

  private <T extends java.lang.Record> T injectPreferences(
      Class<? extends T> clazz, T configWithDefaults) throws ReflectiveOperationException {
    var components = clazz.getRecordComponents();
    Object[] params = new Object[components.length];
    Class<?>[] types = new Class[components.length];
    int i = 0;
    for (RecordComponent component : clazz.getRecordComponents()) {
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
      } else {
        if (getDefaultValue && defaultValue == null) {
          warn("Cannot store '%s' in Preferences; default value is null", name);
          params[i] = null;
        } else {
          params[i] = factory.create(this, component, key, defaultValue);
        }
      }
      i++;
    }
    return clazz.getDeclaredConstructor(types).newInstance(params);
  }

  @FunctionalInterface
  private interface PreferenceFactory {
    Object create(
        PreferenceInjector injector, RecordComponent component, String key, Object defaultValue);
  }

  @FunctionalInterface
  private interface GenericPreferenceFactory<T> {
    T create(PreferenceInjector injector, RecordComponent component, String key, T defaultValue);
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
    register(Boolean.TYPE, PreferenceInjector::booleanFactory);
    register(BooleanSupplier.class, PreferenceInjector::booleanSupplierFactory);
    register(Integer.TYPE, PreferenceInjector::intFactory);
    register(IntSupplier.class, PreferenceInjector::intSupplierFactory);
    register(Long.TYPE, PreferenceInjector::longFactory);
    register(LongSupplier.class, PreferenceInjector::longSupplierFactory);
    register(Double.TYPE, PreferenceInjector::doubleFactory);
    register(DoubleSupplier.class, PreferenceInjector::doubleSupplierFactory);
    register(String.class, PreferenceInjector::stringFactory);
    register(Supplier.class, PreferenceInjector::supplierFactory);
  }

  /** Maps the generic types supported by Preferences to their primitive types. */
  private static final Map<Type, Type> WRAPPER_TO_PRIMITIVE =
      Map.of(
          Boolean.class, Boolean.TYPE,
          Integer.class, Integer.TYPE,
          Long.class, Long.TYPE,
          Double.class, Double.TYPE,
          String.class, String.class);

  private static boolean booleanFactory(
      PreferenceInjector injector, RecordComponent component, String key, Boolean defaultValue) {
    if (defaultValue != null) {
      Preferences.initBoolean(key, defaultValue);
    }
    return Preferences.getBoolean(key, false);
  }

  private static BooleanSupplier booleanSupplierFactory(
      PreferenceInjector injector,
      RecordComponent component,
      String key,
      BooleanSupplier defaultValueSupplier) {
    if (defaultValueSupplier != null) {
      boolean defaultValue = defaultValueSupplier.getAsBoolean();
      Preferences.initBoolean(key, defaultValue);
    }
    return () -> Preferences.getBoolean(key, false);
  }

  private static int intFactory(
      PreferenceInjector injector, RecordComponent component, String key, Integer defaultValue) {
    if (defaultValue != null) {
      Preferences.initInt(key, defaultValue);
    }
    return Preferences.getInt(key, 0);
  }

  private static IntSupplier intSupplierFactory(
      PreferenceInjector injector,
      RecordComponent component,
      String key,
      IntSupplier defaultValueSupplier) {
    if (defaultValueSupplier != null) {
      int defaultValue = defaultValueSupplier.getAsInt();
      Preferences.initInt(key, defaultValue);
    }
    return () -> Preferences.getInt(key, 0);
  }

  private static long longFactory(
      PreferenceInjector injector, RecordComponent component, String key, Long defaultValue) {
    if (defaultValue != null) {
      Preferences.initLong(key, defaultValue);
    }
    return Preferences.getLong(key, 0);
  }

  private static LongSupplier longSupplierFactory(
      PreferenceInjector injector,
      RecordComponent component,
      String key,
      LongSupplier defaultValueSupplier) {
    if (defaultValueSupplier != null) {
      long defaultValue = defaultValueSupplier.getAsLong();
      Preferences.initLong(key, defaultValue);
    }
    return () -> Preferences.getLong(key, 0);
  }

  private static double doubleFactory(
      PreferenceInjector injector, RecordComponent component, String key, Double defaultValue) {
    if (defaultValue != null) {
      Preferences.initDouble(key, defaultValue);
    }
    return Preferences.getDouble(key, 0);
  }

  private static DoubleSupplier doubleSupplierFactory(
      PreferenceInjector injector,
      RecordComponent component,
      String key,
      DoubleSupplier defaultValueSupplier) {
    if (defaultValueSupplier != null) {
      double defaultValue = defaultValueSupplier.getAsDouble();
      Preferences.initDouble(key, defaultValue);
    }
    return () -> Preferences.getDouble(key, 0);
  }

  private static String stringFactory(
      PreferenceInjector injector, RecordComponent component, String key, String defaultValue) {
    if (defaultValue != null) {
      Preferences.initString(key, defaultValue);
    }
    return Preferences.getString(key, "");
  }

  private static Supplier<?> supplierFactory(
      PreferenceInjector injector,
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
