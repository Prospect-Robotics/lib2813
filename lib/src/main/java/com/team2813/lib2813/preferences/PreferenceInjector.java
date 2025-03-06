package com.team2813.lib2813.preferences;

import edu.wpi.first.wpilibj.Preferences;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.*;

public class PreferenceInjector {
  public static final PreferenceInjector DEFAULT_INSTANCE = new PreferenceInjector("com.team2813.");
  private final String removePrefix;
  private final int removePrefixLen;
  private final Map<Type, PreferenceFactory> typeToFactory = new HashMap<>();

  public PreferenceInjector(String removePrefix) {
    this.removePrefix = removePrefix;
    this.removePrefixLen = removePrefix.length();

    typeToFactory.put(Boolean.TYPE, this::booleanFactory);
    typeToFactory.put(BooleanSupplier.class, this::booleanSupplierFactory);
    typeToFactory.put(Integer.TYPE, this::intFactory);
    typeToFactory.put(IntSupplier.class, this::intSupplierFactory);
    typeToFactory.put(Long.TYPE, this::longFactory);
    typeToFactory.put(LongSupplier.class, this::longSupplierFactory);
    typeToFactory.put(Double.TYPE, this::doubleFactory);
    typeToFactory.put(DoubleSupplier.class, this::doubleSupplierFactory);
    typeToFactory.put(String.class, this::stringFactory);
    typeToFactory.put(Supplier.class, this::stringSupplierFactory);
  }

  /**
   * Creates an instance of the given record class with all fields populated from
   * preferences.
   *
   * <p>The type of the record components can be any of the following:
   * <ul>
   *   <li>{@code boolean} or {@code BooleanSupplier}</li>
   *   <li>{@code int} or {@code IntSupplier}</li>
   *   <li>{@code long} or {@code LongSupplier}</li>
   *   <li>{@code double} or {@code DoubleSupplier}</li>
   *   <li>{@code String} or {@code Supplier<String>}</li>
   * </ul>
   *
   * <p>The values for the components for the passed-in instance will be used
   * as the default value for the preference. If a component is a supplier, the
   * supplier will be called to get the default instance.
   *
   * @param configWithDefaults Record instance with all values set to their preferred default values.
   */
  public final <T extends java.lang.Record> T injectPreferences(T configWithDefaults) {
    try {
      @SuppressWarnings("unchecked")
      Class<? extends T> clazz = (Class<? extends T>) configWithDefaults.getClass();

      var components = clazz.getRecordComponents();
      if (components == null) {
        throw new IllegalArgumentException("Must pass in a record class");
      }

      Object[] params = new Object[components.length];
      Class<?>[] types = new Class[components.length];
      int i = 0;
      for (RecordComponent component : clazz.getRecordComponents()) {
        String name = component.getName();
        Class<?> type = component.getType();
        types[i] = type;

        PreferenceFactory factory = typeToFactory.get(type);
        if (factory != null) {
          String key = createKey(component);
          Object defaultValue = null;
          if (!Preferences.containsKey(key)) {
            Field defaultValueField = clazz.getDeclaredField(name);
            defaultValueField.setAccessible(true);
            defaultValue = defaultValueField.get(configWithDefaults);
            if (defaultValue == null) {
              throw new IllegalArgumentException(String.format("Default value for '%s' cannot be null", name));
            }
          }
          params[i] = factory.create(component, key, defaultValue);
        } else {
          throw new IllegalArgumentException(String.format("Unsupported type for '%s': %s", name, type));
        }
        i++;
      }
      return clazz.getConstructor(types).newInstance(params);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
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

  @FunctionalInterface
  private interface PreferenceFactory {
    Object create(RecordComponent component, String key, Object defaultValue);
  }

  private boolean booleanFactory(RecordComponent component, String key, Object value) {
    if (value != null) {
      boolean defaultValue = (Boolean) value;
      Preferences.initBoolean(key, defaultValue);
    }
    return Preferences.getBoolean(key, false);
  }

  private BooleanSupplier booleanSupplierFactory(RecordComponent component, String key, Object value) {
    if (value != null) {
      boolean defaultValue = ((BooleanSupplier) value).getAsBoolean();
      Preferences.initBoolean(key, defaultValue);
    }
    return () -> Preferences.getBoolean(key, false);
  }

  private long intFactory(RecordComponent component, String key, Object value) {
    if (value != null) {
      int defaultValue = (Integer) value;
      Preferences.initInt(key, defaultValue);
    }
    return Preferences.getInt(key, 0);
  }

  private IntSupplier intSupplierFactory(RecordComponent component, String key, Object value) {
    if (value != null) {
      int defaultValue = ((IntSupplier) value).getAsInt();
      Preferences.initInt(key, defaultValue);
    }
    return () -> Preferences.getInt(key, 0);
  }

  private long longFactory(RecordComponent component, String key, Object value) {
    if (value != null) {
      long defaultValue = (Long) value;
      Preferences.initLong(key, defaultValue);
    }
    return Preferences.getLong(key, 0);
  }

  private LongSupplier longSupplierFactory(RecordComponent component, String key, Object value) {
    if (value != null) {
      long defaultValue = ((LongSupplier) value).getAsLong();
      Preferences.initLong(key, defaultValue);
    }
    return () -> Preferences.getLong(key, 0);
  }

  private double doubleFactory(RecordComponent component, String key, Object value) {
    if (value != null) {
      double defaultValue = (Double) value;
      Preferences.initDouble(key, defaultValue);
    }
    return Preferences.getDouble(key, 0);
  }

  private DoubleSupplier doubleSupplierFactory(RecordComponent component, String key, Object value) {
    if (value != null) {
      double defaultValue = ((DoubleSupplier) value).getAsDouble();
      Preferences.initDouble(key, defaultValue);
    }
    return () -> Preferences.getDouble(key, 0);
  }

  private String stringFactory(RecordComponent component, String key, Object value) {
    if (value != null) {
      String defaultValue = (String) value;
      Preferences.initString(key, defaultValue);
    }
    return Preferences.getString(key,"");
  }

  private Supplier<String> stringSupplierFactory(RecordComponent component, String key, Object value) {
    Type supplierType = ((ParameterizedType) component.getGenericType()).getActualTypeArguments()[0];
    if (!supplierType.equals(String.class)) {
      throw new IllegalArgumentException(String.format("Unsupported type for '%s': %s", component.getName(), component.getGenericType()));
    }
    if (value != null) {
      @SuppressWarnings("unchecked")
      String defaultValue = ((Supplier<String>) value).get();
      if (defaultValue == null) {
        throw new IllegalArgumentException(String.format("Default value for '%s' cannot be null", component.getName()));
      }
      Preferences.initString(key, defaultValue);
    }
    return () -> Preferences.getString(key,"");
  }
}
