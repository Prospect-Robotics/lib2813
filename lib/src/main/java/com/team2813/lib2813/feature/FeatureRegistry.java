package com.team2813.lib2813.feature;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.team2813.lib2813.feature.FeatureIdentifier.FeatureBehavior;

import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.shuffleboard.SimpleWidget;

/** Container for features that can be enabled at runtime. */
final class FeatureRegistry {
    private final Map<FeatureIdentifier, Feature> registeredFeatures = new ConcurrentHashMap<>();
    private final ShuffleboardTab shuffleboardTab;

    /** Package-scope constructor (for testing) */
    FeatureRegistry(String shuffleboardTabName) {
        this.shuffleboardTab = Shuffleboard.getTab(shuffleboardTabName);
    }

    public static FeatureRegistry getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        static final FeatureRegistry instance = new FeatureRegistry("Features");
    }

    boolean enabled(FeatureIdentifier id) {
        return getFeature(id).enabled();
    }

    Feature getFeature(FeatureIdentifier id) {
        return registeredFeatures.computeIfAbsent(id, Feature::new);
    }

    Object getState() {
        return registeredFeatures.entrySet().stream()
                .filter(entry -> entry.getValue().changed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    final class Feature {
        final SimpleWidget widget;
        final boolean initiallyEnabled;

        Feature(FeatureIdentifier id) {
            String name = String.format("%s.%s", id.getClass().getName(), id.name());
            if (name.startsWith("com.team2813.")) {
                name = name.substring(13);
            }

            FeatureBehavior behavior = id.behavior();
            initiallyEnabled = (behavior == FeatureBehavior.INITIALLY_ENABLED);
            boolean alwaysDisabled = (
                    behavior == null || behavior == FeatureBehavior.ALWAYS_DISABLED);
            if (alwaysDisabled) {
                shuffleboardTab.addBoolean(name, () -> false).withWidget(BuiltInWidgets.kBooleanBox);
                widget = null;
            } else {
                widget = shuffleboardTab.add(name, initiallyEnabled).withWidget(BuiltInWidgets.kToggleSwitch);
            }
        }

        boolean enabled() {
            return widget != null && widget.getEntry().getBoolean(false);
        }

        boolean changed() {
            return enabled() != initiallyEnabled;
        }
    }
}
