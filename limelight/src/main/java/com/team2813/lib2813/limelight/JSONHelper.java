package com.team2813.lib2813.limelight;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Function;

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

	static OptionalLong unboxLong(Optional<Long> val) {
		if (val.isPresent()) {
			return OptionalLong.of(val.get());
		} else {
			return OptionalLong.empty();
		}
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
}
