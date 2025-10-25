package com.team2813.lib2813.limelight;

import java.util.Optional;
import java.util.function.Function;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility class for safely extracting values from JSONObjects returned by the Limelight.
 *
 * <p>All methods handle missing keys or invalid types gracefully by returning {@link Optional}.
 * This avoids throwing unchecked {@link JSONException}s during normal processing.
 */
class JSONHelper {

  /**
   * Converts an integer value from a JSON object to a boolean.
   *
   * <p>The key must be present and contain 0 (false) or 1 (true). Returns {@link Optional#empty()}
   * if the key is missing or the value is not an integer.
   *
   * @param obj the JSON object to read from
   * @param key the key corresponding to the boolean-as-int
   * @return {@link Optional} containing true if value is 1, false if 0, or empty if missing/invalid
   */
  static Optional<Boolean> getBooleanFromInt(JSONObject obj, String key) {
    if (!obj.has(key)) {
      return Optional.empty();
    }
    try {
      return Optional.of(obj.getInt(key) == 1);
    } catch (JSONException e) {
      return Optional.empty();
    }
  }

  /**
   * Returns a function that retrieves a nested JSONObject by key.
   *
   * <p>The returned function returns {@link Optional#empty()} if the key is missing or not a JSON
   * object.
   *
   * @param key the key to extract
   * @return a function that extracts an Optional&lt;JSONObject&gt; from a parent JSONObject
   */
  static Function<JSONObject, Optional<JSONObject>> getJSONObject(String key) {
    return (j) -> {
      if (!j.has(key)) {
        return Optional.empty();
      }
      try {
        return Optional.of(j.getJSONObject(key));
      } catch (JSONException e) {
        return Optional.empty();
      }
    };
  }

  /**
   * Returns the "root" JSONObject for the Limelight results.
   *
   * <p>Older versions of the Limelight returned a root node called "Results". This method
   * normalizes that so downstream code can always work with a consistent root object.
   *
   * @param json the raw JSON response from the Limelight
   * @return the root JSONObject to extract fields from
   */
  static JSONObject getRoot(JSONObject json) {
    if (json.has("Results")) {
      return json.getJSONObject("Results");
    } else {
      return json;
    }
  }

  /**
   * Retrieves a long value from a JSONObject by key.
   *
   * @param obj the JSON object to read from
   * @param key the key corresponding to the long value
   * @return Optional containing the long value, or empty if missing or invalid
   */
  static Optional<Long> getLong(JSONObject obj, String key) {
    if (!obj.has(key)) {
      return Optional.empty();
    }
    try {
      return Optional.of(obj.getLong(key));
    } catch (JSONException e) {
      return Optional.empty();
    }
  }

  /**
   * Retrieves a double value from a JSONObject by key.
   *
   * @param obj the JSON object to read from
   * @param key the key corresponding to the double value
   * @return Optional containing the double value, or empty if missing or invalid
   */
  static Optional<Double> getDouble(JSONObject obj, String key) {
    if (!obj.has(key)) {
      return Optional.empty();
    }
    try {
      return Optional.of(obj.getDouble(key));
    } catch (JSONException e) {
      return Optional.empty();
    }
  }

  /**
   * Retrieves a JSONArray from a JSONObject by key.
   *
   * @param obj the JSON object to read from
   * @param key the key corresponding to the JSON array
   * @return Optional containing the JSONArray, or empty if missing or invalid
   */
  static Optional<JSONArray> getArr(JSONObject obj, String key) {
    if (!obj.has(key)) {
      return Optional.empty();
    }
    try {
      return Optional.of(obj.getJSONArray(key));
    } catch (JSONException e) {
      return Optional.empty();
    }
  }
}
