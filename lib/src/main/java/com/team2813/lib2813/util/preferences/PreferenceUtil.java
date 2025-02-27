package com.team2813.lib2813.util.preferences;

final class PreferenceUtil {

  public static String fullKey(PreferenceKey key) {
    String prefix = key.getClass().getCanonicalName();
    if (prefix.startsWith("com.team2813.")) {
      prefix = prefix.substring(13);
    }
    return prefix + "." + key.name();
  }

  private PreferenceUtil() {
    throw new AssertionError("Not instantiable");
  }
}
