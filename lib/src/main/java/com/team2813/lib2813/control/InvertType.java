package com.team2813.lib2813.control;

import static java.util.stream.Collectors.*;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.ctre.phoenix6.signals.InvertedValue;

public enum InvertType {
	CLOCKWISE(InvertedValue.Clockwise_Positive, false),
	COUNTER_CLOCKWISE(InvertedValue.CounterClockwise_Positive, true),
	FOLLOW_MASTER,
	OPPOSE_MASTER;

	private final Optional<InvertedValue> phoenixInvert;
	private final Optional<Boolean> sparkMaxInvert;

	public static final Set<InvertType> rotationValues = Collections.unmodifiableSet(
			EnumSet.of(CLOCKWISE, COUNTER_CLOCKWISE));

	InvertType() {
		phoenixInvert = Optional.empty();
		sparkMaxInvert = Optional.empty();
	}

	InvertType(InvertedValue phoenixInvert, boolean sparkMaxInvert) {
		this.phoenixInvert = Optional.of(phoenixInvert);
		this.sparkMaxInvert = Optional.of(sparkMaxInvert);
	}

	public Optional<InvertedValue> phoenixInvert() {
		return phoenixInvert;
	}

	private InvertedValue forcePhoenixInvert() {
		return phoenixInvert.orElseThrow();
	}

	public Optional<Boolean> sparkMaxInvert() {
		return sparkMaxInvert;
	}

	private boolean forceSparkMaxInvert() {
		return sparkMaxInvert.orElseThrow();
	}

	/**
	 * Contains the maps for {@link InvertType#fromPhoenixInvert(InvertedValue)} and
	 * {@link InvertType#fromSparkMaxInvert(boolean)}. In a static class so that
	 * they will only be
	 * initialized if they are needed.
	 */
	private static final class Maps {
		private static final Map<InvertedValue, InvertType> phoenixMap = Stream.of(InvertType.values())
				.filter((j) -> j.phoenixInvert.isPresent())
				.collect(toUnmodifiableMap(InvertType::forcePhoenixInvert, (j) -> j, (a, b) -> null));
		private static final Map<Boolean, InvertType> sparkMaxMap = Stream.of(InvertType.values())
				.filter((j) -> j.sparkMaxInvert.isPresent())
				.collect(toUnmodifiableMap(InvertType::forceSparkMaxInvert, (j) -> j, (a, b) -> null));
	}

	/**
	 * Gets an {@link InvertType} from a phoenix {@link InvertedValue}.
	 * 
	 * @param v the {@link InvertedValue} to search for
	 * @return {@link Optional#empty()} if no {@link InvertType} is found,
	 *         otherwise,
	 *         an optional describing the {@link InvertType}
	 */
	public static Optional<InvertType> fromPhoenixInvert(InvertedValue v) {
		return Optional.of(Maps.phoenixMap.get(v));
	}

	/**
	 * Gets an {@link InvertType} from a spark max invert
	 * 
	 * @param v the {@link InvertedValue} to search for
	 * @return {@link Optional#empty()} if no {@link InvertType} is found,
	 *         otherwise,
	 *         an optional describing the {@link InvertType}
	 */
	public static Optional<InvertType> fromSparkMaxInvert(boolean v) {
		return Optional.of(Maps.sparkMaxMap.get(v));
	}
}
