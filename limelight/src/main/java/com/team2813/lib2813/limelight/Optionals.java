package com.team2813.lib2813.limelight;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalLong;

/**
 * Utility class for working with {@link Optional} values.
 * <p>
 * Provides methods to convert boxed {@link Optional} types to their corresponding primitive
 * {@code Optional} types. This class is non-instantiable.
 * </p>
 */
final class Optionals {

    /**
     * Private constructor to prevent instantiation.
     * Invoking this constructor will always throw an {@link AssertionError}.
     */
    private Optionals() {
        throw new AssertionError("cannot create Optionals instance");
    }

    /**
     * Converts an {@link Optional} of {@link Long} to an {@link OptionalLong}.
     *
     * @param val the {@code Optional<Long>} to convert
     * @return an {@code OptionalLong} containing the value if present, or an empty {@code OptionalLong} if not
     */
    static OptionalLong unboxLong(Optional<Long> val) {
        return val.map(OptionalLong::of).orElseGet(OptionalLong::empty);
    }

    /**
     * Converts an {@link Optional} of {@link Double} to an {@link OptionalDouble}.
     *
     * @param val the {@code Optional<Double>} to convert
     * @return an {@code OptionalDouble} containing the value if present, or an empty {@code OptionalDouble} if not
     */
    static OptionalDouble unboxDouble(Optional<Double> val) {
        return val.map(OptionalDouble::of).orElseGet(OptionalDouble::empty);
    }
}
