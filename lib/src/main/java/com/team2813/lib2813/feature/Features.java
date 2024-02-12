package com.team2813.lib2813.feature;

import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj2.command.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class Features {
    private Features() {
        throw new AssertionError("not instantiable");
    }

    /**
     * Decorates the command to only run if all the provided features were enabled when the command is initialized.
     *
     * @param command The command to decorate
     * @param first A feature identifier (if {@code null}, the command will not be run).
     * @param rest Zero or more additional feature identifiers (if {@code null} or contains {@code null} values, the
     *             command will not be run).
     *
     * @return the decorated command
     * @throws NullPointerException if {@code command} is {@code null}
     */
    @SafeVarargs
    public static <T extends Enum<T> & FeatureIdentifier> ConditionalCommand whenAllEnabled(
            Command command, T first, T... rest) {
        requireNonNull(command, "command cannot be null");
        if (first == null || rest == null) {
            return new ConditionalCommand(Commands.none(), Commands.none(), () -> false);
        }
        List<T> features = new ArrayList<>(rest.length + 1);
        features.add(first);
        features.addAll(Arrays.asList(rest));
        return new FeatureControlledCommand<>(command, features);
    }

    private static class FeatureControlledCommand<T extends Enum<T> & FeatureIdentifier> extends ConditionalCommand {
        final List<FeatureIdentifier> features;

        FeatureControlledCommand(Command command, List<T> features) {
            super(command, Commands.none(), FeatureRegistry.getInstance().asSupplier(features));
            this.features = new ArrayList<>(features);
        }

        @Override
        public void initSendable(SendableBuilder builder) {
            super.initSendable(builder);
            builder.addStringArrayProperty(
                    "features",
                    () -> features.stream().map(FeatureIdentifier::name).toArray(String[]::new),
                    null);
        }
    }

    /**
     * Returns {@code True} iff all the given features are enabled.
     *
     * <p>To see determine if a single feature is enabled, use {@link FeatureIdentifier#enabled()}
     *
     * @param first A feature identifier (if {@code null}, the command will not be run).
     * @param rest Zero or more additional feature identifiers (if {@code null} or contains {@code null} values, the
     *             command will not be run).
     */
    @SafeVarargs
    public static <T extends Enum<T> & FeatureIdentifier> boolean allEnabled(T first, T... rest) {
        return FeatureRegistry.getInstance().allEnabled(first, rest);
    }
}
