package com.team2813.lib2813.feature;

import edu.wpi.first.wpilibj2.command.*;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Features {
    private Features() {
        throw new AssertionError("not instantiable");
    }

    /**
     * Decorates the command to only run if all the provided features were enabled.
     *
     * @param commandSupplier Called to create the command.
     * @param first A feature identifier (if {@code null}, the command will not be run).
     * @param rest Zero or more additional feature identifiers (if {@code null} or contains {@code null} values, the
     *             command will not be run).
     *
     * @return the decorated command
     * @throws NullPointerException if {@code command} is {@code null}
     */
    @SafeVarargs
    public static <T extends Enum<T> & FeatureIdentifier> FeatureControlledCommand ifAllEnabled(
            Supplier<Command> commandSupplier, T first, T... rest) {
        return FeatureControlledCommand.ifAllEnabled(commandSupplier, nonNull(first, rest));
    }

    /**
     * Returns {@code true} iff all the given features are currently enabled.
     *
     * @param featureIdentifiers Feature identifiers (if {@code null} or contains {@code null} values, the
     *              returned value will be {@code false}).
     */
    public static <T extends Enum<T> & FeatureIdentifier> boolean allEnabled(Collection<T> featureIdentifiers) {
        if (featureIdentifiers == null || featureIdentifiers.stream().anyMatch(Objects::isNull)) {
            return false;
        }
        FeatureRegistry registry = FeatureRegistry.getInstance();
        return featureIdentifiers.stream().map(registry::getFeature).allMatch(FeatureRegistry.Feature::enabled);
    }

    /**
     * Returns {@code true} iff all the given features are currently enabled.
     *
     * @param first A feature identifier (if {@code null}, this method will return {@code false}).
     * @param rest Zero or more additional feature identifiers (if {@code null} or contains {@code null} values,
     *             this method will return {@code false}).
     */
    @SafeVarargs
    public static <T extends Enum<T> & FeatureIdentifier> boolean allEnabled(T first, T... rest) {
        if (first == null || rest == null || Stream.of(rest).anyMatch(Objects::isNull)) {
            return false;
        }

        FeatureRegistry registry = FeatureRegistry.getInstance();
        if (!registry.getFeature(first).enabled()) {
            return false;
        }
        return Stream.of(rest).map(registry::getFeature).allMatch(FeatureRegistry.Feature::enabled);
    }

    /**
     * Creates a {@link BooleanSupplier} that returns {@code true} iff all the given features are enabled.
     *
     * @param featureIdentifiers Feature identifiers (if {@code null} or contains {@code null} values, the
     *              returned supplier will always return {@code false}).
     */
    public static <T extends Enum<T> & FeatureIdentifier> BooleanSupplier asSupplier(Collection<T> featureIdentifiers) {
        if (featureIdentifiers == null || featureIdentifiers.stream().anyMatch(Objects::isNull)) {
            return () -> false;
        }

        FeatureRegistry registry = FeatureRegistry.getInstance();
        List<FeatureRegistry.Feature> features = featureIdentifiers.stream().map(registry::getFeature).toList();
        return () -> features.stream().allMatch(FeatureRegistry.Feature::enabled);
    }

    /**
     * Creates a {@link BooleanSupplier} that returns {@code true} iff all the given features are enabled.
     *
     * @param first A feature identifier (if {@code null}, the returned supplier will always return {@code false}).
     * @param rest  Zero or more additional feature identifiers (if {@code null} or contains {@code null} values, the
     *              returned supplier will always return {@code false}).
     */
    @SafeVarargs
    public static <T extends Enum<T> & FeatureIdentifier> BooleanSupplier asSupplier(T first, T... rest) {
        if (first == null || rest == null) {
            return () -> false;
        }

        List<T> featureIdentifiers = new ArrayList<>(rest.length + 1);
        featureIdentifiers.add(first);
        featureIdentifiers.addAll(Arrays.asList(rest));
        return asSupplier(featureIdentifiers);
    }

    /**
     * Gets the state of all features
     *
     * <p>This returned value encodes all features that have been updated from their
     * default values. This can be used to determine if feature values have changed
     * over time (for example, to reset the system if the feature values have changed
     * since the last time the robot was enabled).
     */
    public static Object getState() {
        return FeatureRegistry.getInstance().getState();
    }

    @SafeVarargs
    private static <T extends Enum<T> & FeatureIdentifier> Collection<FeatureIdentifier> nonNull(T first, T... rest) {
        if (first == null || rest == null) {
            return Collections.singleton(NullFeature.INSTANCE);
        }

        List<FeatureIdentifier> featureIdentifiers = new ArrayList<>(rest.length + 1);
        featureIdentifiers.add(first);
        Stream.of(rest).map(NullFeature::nullToNullFeature).forEach(featureIdentifiers::add);
        return featureIdentifiers;
    }

    private static class NullFeature implements FeatureIdentifier {
        static final NullFeature INSTANCE = new NullFeature();

        static FeatureIdentifier nullToNullFeature(FeatureIdentifier featureIdentifier) {
            if (featureIdentifier == null) {
                return INSTANCE;
            }
            return featureIdentifier;
        }

        @Override
        public String name() {
            return "NULL";
        }

        @Override
        public FeatureBehavior behavior() {
            return FeatureBehavior.ALWAYS_DISABLED;
        }
    }
}
