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
