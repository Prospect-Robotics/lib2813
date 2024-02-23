package com.team2813.lib2813.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

import com.team2813.lib2813.feature.FeatureIdentifier.FeatureBehavior;

import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.shuffleboard.SimpleWidget;

/** Container for features that can be enabled at runtime. */
final class FeatureRegistry {
    private final Map<FeatureIdentifier, Feature> registeredFeatures = new ConcurrentHashMap<>();

    /** Package-scope constructor (for testing) */
    FeatureRegistry() {}

    public static FeatureRegistry getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        static final FeatureRegistry instance = new FeatureRegistry();
    }

    /**
     * Creates a {@link BooleanSupplier} that returns {@code true} iff all the given features are enabled.
     *
     * @param first A feature identifier (if {@code null}, the returned supplier will always return {@code false}).
     * @param rest  Zero or more additional feature identifiers (if {@code null} or contains {@code null} values, the
     *              returned supplier will always return {@code false}).
     */
    @SafeVarargs
    final <T extends Enum<T> & FeatureIdentifier> BooleanSupplier asSupplier(T first, T... rest) {
        if (first == null || rest == null) {
            return () -> false;
        }

        List<T> featureIdentifiers = new ArrayList<>(rest.length + 1);
        featureIdentifiers.add(first);
        featureIdentifiers.addAll(Arrays.asList(rest));
        return asSupplier(featureIdentifiers);
    }

    <T extends Enum<T> & FeatureIdentifier> BooleanSupplier asSupplier(Collection<T> featureIdentifiers) {
        if (featureIdentifiers == null || featureIdentifiers.stream().anyMatch(Objects::isNull)) {
            return () -> false;
        }

        List<Feature> features = featureIdentifiers.stream().map(this::getFeature).toList();

        return () -> features.stream().allMatch(Feature::enabled);
    }

    @SafeVarargs
    final <T extends Enum<T> & FeatureIdentifier> boolean allEnabled(T first, T... rest) {
        if (first == null || rest == null || Stream.of(rest).anyMatch(Objects::isNull)) {
            return false;
        }

        if (!getFeature(first).enabled()) {
            return false;
        }

        return Stream.of(rest).map(this::getFeature).allMatch(Feature::enabled);
    }

    boolean enabled(FeatureIdentifier id) {
        return getFeature(id).enabled();
    }

    private Feature getFeature(FeatureIdentifier id) {
        return registeredFeatures.computeIfAbsent(id, Feature::new);
    }

    private static final class Feature {
        private final SimpleWidget widget;
        private static final ShuffleboardTab shuffleboardTab = Shuffleboard.getTab("Features");

        Feature(FeatureIdentifier id) {
            String name = String.format("%s.%s", id.getClass().getName(), id.name());
            if (name.startsWith("com.team2813.")) {
                name = name.substring(13);
            }

            FeatureBehavior behavior = id.behavior();
            boolean enabled = (behavior == FeatureBehavior.INITIALLY_ENABLED);
            boolean alwaysDisabled = (
                    behavior == null || behavior == FeatureBehavior.ALWAYS_DISABLED);
            if (alwaysDisabled) {
                shuffleboardTab.addBoolean(name, () -> false).withWidget(BuiltInWidgets.kBooleanBox);
                widget = null;
            } else {
                widget = shuffleboardTab.add(name, enabled).withWidget(BuiltInWidgets.kToggleSwitch);
            }
        }

        boolean enabled() {
            return widget != null && widget.getEntry().getBoolean(false);
        }
    }
}
