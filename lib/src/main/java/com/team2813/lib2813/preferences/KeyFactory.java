package com.team2813.lib2813.preferences;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Creates and caches keys for {@link Preference} instances. */
public class KeyFactory {
  protected final ConcurrentMap<Preference, String> instanceToKey = new ConcurrentHashMap<>();
  private final String removePrefix;
  private final int removePrefixLen;

  public static final KeyFactory DEFAULT_INSTANCE = new KeyFactory();

  public KeyFactory() {
    this("com.team2813.");
  }

  public KeyFactory(String removePrefix) {
    this.removePrefix = removePrefix;
    this.removePrefixLen = removePrefix.length();
  }

  public KeyFactory(Package containingPackage) {
    this(containingPackage.getName() + ".");
  }

  /**
   * Creates a key for the given preference class and name.
   *
   * @param preference The preference instance. Usually an enum class.
   * @param name The preference name. Usually the enum instance name.
   */
  public String createKey(Preference preference, String name) {
    return instanceToKey.computeIfAbsent(
        preference,
        p -> {
          String prefix = p.getClass().getCanonicalName();
          if (prefix.startsWith(this.removePrefix)) {
            prefix = prefix.substring(this.removePrefixLen);
            if (prefix.isEmpty()) {
              return name;
            }
          }
          return prefix + "." + name;
        });
  }
}
