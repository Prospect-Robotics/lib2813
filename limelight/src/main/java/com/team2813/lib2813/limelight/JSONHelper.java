package com.team2813.lib2813.limelight;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.function.Function;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class JSONHelper {
	static Function<JSONObject, Optional<Boolean>> getBooleanFromInt(String key) {
		return (j) -> {
			if (!j.has(key)) {
				return Optional.empty();
			}
			try {
				return Optional.of(j.getInt(key) == 1);
			} catch (JSONException e) {
				return Optional.empty();
			}
		};
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
	
	static Function<JSONObject, Optional<JSONArray>> getJSONArray(String key) {
		return (j) -> {
			if (!j.has(key)) {
				return Optional.empty();
			}
			try {
				return Optional.of(j.getJSONArray(key));
			} catch (JSONException e) {
				return Optional.empty();
			}
		};
	}

	static Function<JSONObject, Optional<JSONObject>> getRoot() {
		return (json) -> {
			if (json.has("Results")) {
				return Optional.of(json.getJSONObject("Results"));
			} else {
				return Optional.of(json);
			}
 		};
	}

	static Function<JSONObject, Optional<Long>> getLong(String key) {
		return (j) -> {
			if (!j.has(key)) {
				return Optional.empty();
			}
			try {
				return Optional.of(j.getLong(key));
			} catch (JSONException e) {
				return Optional.empty();
			}
		};
	}

	static Function<JSONObject, Optional<Double>> getDouble(String key) {
		return (j) -> {
			if (!j.has(key)) {
				return Optional.empty();
			}
			try {
				return Optional.of(j.getDouble(key));
			} catch (JSONException e) {
				return Optional.empty();
			}
		};
	}
	
	static Function<JSONObject, Optional<JSONArray>> getArr(String key) {
		return (obj) -> {
			if (!obj.has(key)) {
				return Optional.empty();
			}
			try {
				return Optional.of(obj.getJSONArray(key));
			} catch (JSONException e) {
				return Optional.empty();
			}
		};
	}
}
