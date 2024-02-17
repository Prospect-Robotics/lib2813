package com.team2813.lib2813.feature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.team2813.lib2813.feature.FeatureIdentifier.FeatureBehavior;

import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.shuffleboard.SimpleWidget;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

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
     * @param rest Zero or more additional feature identifiers (if {@code null} or contains {@code null} values, the
     *             returned supplier will always return {@code false}).
     */
    @SafeVarargs
    final <T extends Enum<T> & FeatureIdentifier> BooleanSupplier asSupplier(T first, T... rest) {
        if (first == null || rest == null || Stream.of(rest).anyMatch(Objects::isNull)) {
            return () -> false;
        }

        List<Feature> features = new ArrayList<>(rest.length + 1);
        features.add(getFeature(first));
        Stream.of(rest).map(this::getFeature).forEach(features::add);

        return () -> features.stream().allMatch(Feature::enabled);
    }

    <T extends Enum<T> & FeatureIdentifier> BooleanSupplier asSupplier(Collection<T> featureIdentifiers) {
        if (featureIdentifiers == null || featureIdentifiers.stream().anyMatch(Objects::isNull)) {
            return () -> false;
        }

        List<Feature> features = featureIdentifiers.stream().map(this::getFeature).collect(Collectors.toList());

        return () -> features.stream().allMatch(Feature::enabled);
    }

    @SafeVarargs
    final <T extends Enum<T> & FeatureIdentifier> boolean allEnabled(T first, T... rest) {
        if (first == null || rest == null || Stream.of(rest).anyMatch(Objects::isNull)) {
            return false;
        }

        if (!getFeature(first).enabled) {
            return false;
        }

        return Stream.of(rest).map(this::getFeature).allMatch(Feature::enabled);
    }

    boolean enabled(FeatureIdentifier id) {
        return getFeature(id).enabled;
    }

    private Feature getFeature(FeatureIdentifier id) {
        int registeredFeatureCount = registeredFeatures.size();
        Feature feature = registeredFeatures.computeIfAbsent(id, Feature::new);
        if (registeredFeatures.size() > registeredFeatureCount) {
            if (RobotBase.isSimulation()) {
                updateSmartDashboard();
            }
        }
        return feature;
    }

    private synchronized void updateSmartDashboard() {
        System.out.println("Updating Features in SmartDashboard");
        // The below should work, but I do not see it in SmartDashboard.
        SmartDashboard.putData("Features", new SmartDashboardSendable());
    }

    private class SmartDashboardSendable implements Sendable {
        @Override
        public void initSendable(SendableBuilder builder) {
            builder.setSmartDashboardType("Feature List");
            Map<FeatureIdentifier, Feature> features = FeatureRegistry.this.registeredFeatures;
            System.out.printf("Adding %d features to Shuffleboard%n", features.size());
            builder.addStringArrayProperty(
                    "options", () -> features.keySet().stream().map(FeatureIdentifier::name).toArray(String[]::new),
                    null);
        }
    }

    private static final class Feature implements Sendable {
        private final String name;
        private volatile boolean enabled;
        private final boolean alwaysDisabled;
		private final SimpleWidget widget;
		private static final ShuffleboardTab tab = Shuffleboard.getTab("Features");

        Feature(FeatureIdentifier id) {
            String name = String.format("%s.%s", id.getClass().getName(), id.name());
            if (name.startsWith("com.team2813.")) {
                name = name.substring(13);
            }
            this.name = name;

            FeatureBehavior behavior = id.behavior();
            this.enabled = (behavior == FeatureBehavior.INITIALLY_ENABLED);
            this.alwaysDisabled = (
                    behavior == null || behavior == FeatureBehavior.ALWAYS_DISABLED);
			if (alwaysDisabled) {
				tab.addBoolean(name, () -> false).withWidget(BuiltInWidgets.kBooleanBox);
				widget = null;
			} else {
				widget = tab.add(name, enabled);
				widget.withWidget(BuiltInWidgets.kToggleSwitch);
			}
			
            // tab.add(name, this);

            System.out.printf("Adding feature %s to Shuffleboard%n", name);
            Shuffleboard.getTab("Features").add(name, this);
        }

        boolean enabled() {
            return !alwaysDisabled && widget.getEntry().getBoolean(false);
        }

        void enable(boolean enabled) {
            if (DriverStation.isEnabled()) {
                return; // Do not allow updating values while the Robot is enabled.
            }
            if (alwaysDisabled && enabled) {
                // We shouldn't be able to get here since initSendable() called publishConstBoolean()
                DriverStation.reportWarning(
                        String.format("Attempt to enable feature %s which is configured as ALWAYS_DISABLED", name), false);
                return;
            }
            this.enabled = enabled;
        }

        @Override
        public void initSendable(SendableBuilder builder) {
            builder.setSmartDashboardType("Feature");
            builder.publishConstString(".name", name);
            if (alwaysDisabled) {
                builder.publishConstBoolean("enabled", false);
            } else {
                builder.addBooleanProperty("enabled", this::enabled, this::enable);
            }
        }
    }
}
