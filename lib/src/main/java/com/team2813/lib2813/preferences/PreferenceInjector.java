package com.team2813.lib2813.preferences;

import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Preferences;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.*;

public class PreferenceInjector {
  public static final PreferenceInjector DEFAULT_INSTANCE = new PreferenceInjector("com.team2813.");
  private final String removePrefix;
  private final int removePrefixLen;

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
   *   <li>{@code boolean} or {@code BooleanSupplier}
   *   <li>{@code int} or {@code IntSupplier}
   *   <li>{@code long} or {@code LongSupplier}
   *   <li>{@code double} or {@code DoubleSupplier}
   *   <li>{@code String} or {@code Supplier<String>}
   * </ul>
   *
   * <p>The values for the components for the passed-in instance will be used as the default value
   * for the preference. If a component is a supplier, the supplier will be called to get the
   * default instance.
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
          params[i] = factory.create(component, key, defaultValue);
        }
      }
      i++;
    }
    return clazz.getConstructor(types).newInstance(params);
  }

  @FunctionalInterface
  private interface PreferenceFactory {
    Object create(RecordComponent component, String key, Object defaultValue);
  }

  private static final Map<Type, PreferenceFactory> TYPE_TO_FACTORY = new HashMap<>();

  static {
    TYPE_TO_FACTORY.put(Boolean.TYPE, PreferenceInjector::booleanFactory);
    TYPE_TO_FACTORY.put(BooleanSupplier.class, PreferenceInjector::booleanSupplierFactory);
    TYPE_TO_FACTORY.put(Integer.TYPE, PreferenceInjector::intFactory);
    TYPE_TO_FACTORY.put(IntSupplier.class, PreferenceInjector::intSupplierFactory);
    TYPE_TO_FACTORY.put(Long.TYPE, PreferenceInjector::longFactory);
    TYPE_TO_FACTORY.put(LongSupplier.class, PreferenceInjector::longSupplierFactory);
    TYPE_TO_FACTORY.put(Double.TYPE, PreferenceInjector::doubleFactory);
    TYPE_TO_FACTORY.put(DoubleSupplier.class, PreferenceInjector::doubleSupplierFactory);
    TYPE_TO_FACTORY.put(String.class, PreferenceInjector::stringFactory);
    TYPE_TO_FACTORY.put(Supplier.class, PreferenceInjector::supplierFactory);
  }

  private static boolean booleanFactory(RecordComponent component, String key, Object value) {
    Boolean defaultValue = (Boolean) value;

    if (defaultValue != null) {
      Preferences.initBoolean(key, defaultValue);
    }
    return Preferences.getBoolean(key, false);
  }

  private static BooleanSupplier booleanSupplierFactory(
      RecordComponent component, String key, Object value) {
    BooleanSupplier supplier = (BooleanSupplier) value;

    if (supplier != null) {
      boolean defaultValue = supplier.getAsBoolean();
      Preferences.initBoolean(key, defaultValue);
    }
    return () -> Preferences.getBoolean(key, false);
  }

  private static long intFactory(RecordComponent component, String key, Object value) {
    Integer defaultValue = (Integer) value;

    if (defaultValue != null) {
      Preferences.initInt(key, defaultValue);
    }
    return Preferences.getInt(key, 0);
  }

  private static IntSupplier intSupplierFactory(
      RecordComponent component, String key, Object value) {
    IntSupplier supplier = (IntSupplier) value;

    if (supplier != null) {
      int defaultValue = supplier.getAsInt();
      Preferences.initInt(key, defaultValue);
    }
    return () -> Preferences.getInt(key, 0);
  }

  private static long longFactory(RecordComponent component, String key, Object value) {
    Long defaultValue = (Long) value;

    if (defaultValue != null) {
      Preferences.initLong(key, defaultValue);
    }
    return Preferences.getLong(key, 0);
  }

  private static LongSupplier longSupplierFactory(
      RecordComponent component, String key, Object value) {
    LongSupplier supplier = (LongSupplier) value;

    if (supplier != null) {
      long defaultValue = supplier.getAsLong();
      Preferences.initLong(key, defaultValue);
    }
    return () -> Preferences.getLong(key, 0);
  }

  private static double doubleFactory(RecordComponent component, String key, Object value) {
    Double defaultValue = (Double) value;

    if (defaultValue != null) {
      Preferences.initDouble(key, defaultValue);
    }
    return Preferences.getDouble(key, 0);
  }

  private static DoubleSupplier doubleSupplierFactory(
      RecordComponent component, String key, Object value) {
    DoubleSupplier supplier = (DoubleSupplier) value;

    if (supplier != null) {
      double defaultValue = supplier.getAsDouble();
      Preferences.initDouble(key, defaultValue);
    }
    return () -> Preferences.getDouble(key, 0);
  }

  private static String stringFactory(RecordComponent component, String key, Object value) {
    String defaultValue = (String) value;

    if (defaultValue != null) {
      Preferences.initString(key, defaultValue);
    }
    return Preferences.getString(key, "");
  }

  private static Supplier<?> supplierFactory(RecordComponent component, String key, Object value) {
    Supplier<?> supplier = (Supplier<?>) value;

    Type supplierType =
        ((ParameterizedType) component.getGenericType()).getActualTypeArguments()[0];
    if (!supplierType.equals(String.class)) {
      warn(
          "Cannot store '%s' in Preferences; type %s is unsupported",
          component.getName(), component.getGenericType());
      return supplier;
    }
    if (supplier != null) {
      String defaultValue = (String) supplier.get();
      if (defaultValue == null) {
        warn("Cannot store '%s' in Preferences; default value is null", component.getName());
        return supplier;
      }
      Preferences.initString(key, defaultValue);
    }
    return () -> Preferences.getString(key, "");
  }

  private static void warn(String format, Object... args) {
    DataLogManager.log(String.format("WARNING: " + format, args));
  }
}
