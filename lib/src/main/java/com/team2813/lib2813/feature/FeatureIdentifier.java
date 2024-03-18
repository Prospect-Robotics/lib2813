package com.team2813.lib2813.feature;

import edu.wpi.first.wpilibj2.command.Command;

import java.util.function.Supplier;

/** Mix-in interface for features. This should be implemented by an enum. */
public interface FeatureIdentifier {

    enum FeatureBehavior {
        /** The feature is disabled but can be enabled via Shuffleboard. */
        INITIALLY_DISABLED,

        /** The feature is disabled but can be enabled via Shuffleboard. */
        INITIALLY_ENABLED,

        /** The feature is disabled and cannot be enabled via Shuffleboard. */
        ALWAYS_DISABLED,
    }

    String name();

    FeatureBehavior behavior();

    /** Returns {@code true} iff this feature is enabled. */
    default boolean enabled() {
        return FeatureRegistry.getInstance().enabled(this);
    }

    default FeatureGatedCommand.Builder gatedCommand(Supplier<Command> commandSupplier) {
        return new FeatureGatedCommand.Builder().or(this, commandSupplier);
    }
}
