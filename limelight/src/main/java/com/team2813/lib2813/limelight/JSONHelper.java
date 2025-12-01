/*
Copyright 2024-2025 Prospect Robotics SWENext Club

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
package com.team2813.lib2813.limelight;

import java.util.Optional;
import java.util.function.Function;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class JSONHelper {
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

  static JSONObject getRoot(JSONObject json) {
    if (json.has("Results")) {
      // This JSON was provided by an older version of the limelight code,
      // which had a "Results" root node.
      return json.getJSONObject("Results");
    } else {
      return json;
    }
  }

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
