package com.team2813.lib2813.feature;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class FeatureGated<T> {
    private final List<Entry<T>> entries;
    private final Supplier<T> fallback;

    protected FeatureGated(List<Entry<T>> entries, Supplier<T> defaultSupplier) {
        this.entries = entries;
        this.fallback = defaultSupplier;
    }

    public T get() {
        for (var entry : entries) {
            if (entry.feature.enabled()) {
                return entry.supplier.get();
            }
        }
        return fallback.get();
    }

    protected record Entry<T>(FeatureIdentifier feature, Supplier<T> supplier) {
    }

    protected abstract static class BaseBuilder<T, BuildResult extends FeatureGated<T>, Self extends BaseBuilder<T, BuildResult, Self>> {
        private final List<Entry<T>> entries = new ArrayList<>();

        public Self or(FeatureIdentifier feature, Supplier<T> supplier) {
            this.entries.add(new Entry<T>(feature, supplier));
            return (Self) this;
        }

        protected abstract BuildResult build(List<Entry<T>> entries, Supplier<T> fallback);

        public BuildResult or(Supplier<T> fallback) {
            return build(this.entries, fallback);
        }
    }

    public static class Builder<T> extends BaseBuilder<T, FeatureGated<T>, Builder<T>> {
        @Override
        protected FeatureGated<T> build(List<Entry<T>> entries, Supplier<T> fallback) {
            return new FeatureGated<T>(entries, fallback);
        }
    }
}
