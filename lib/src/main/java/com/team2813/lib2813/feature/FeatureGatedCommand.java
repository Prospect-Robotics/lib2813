package com.team2813.lib2813.feature;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;

import java.util.ArrayList;
import java.util.function.Supplier;

public class FeatureGatedCommand {
    private final ArrayList<Entry> entries;
    private final Supplier<Command> fallback;

    private FeatureGatedCommand(ArrayList<Entry> entries, Supplier<Command> defaultCommand) {
        this.entries = entries;
        this.fallback = defaultCommand;
    }

    public Command get() {
        for (var entry : entries) {
            if (entry.feature.enabled()) {
                return entry.commandSupplier.get();
            }
        }
        return fallback.get();
    }

    public Command getDeferred() {
        return new InstantCommand(() -> this.get().schedule());
    }

    /**
     * returns a command that will avoid the added 20ms latency you'd normally get from deferring,
     * but which MUST only be scheduled via the .schedule() method
     */
    public Command getDeferredFast() {
        return new FastDeferredCommandBodge(this::get);
    }

    private record Entry(FeatureIdentifier feature, Supplier<Command> commandSupplier) {
    }

    public static class Builder {
        private final ArrayList<Entry> entries = new ArrayList<>();

        public Builder or(FeatureIdentifier feature, Supplier<Command> commandSupplier) {
            this.entries.add(new Entry(feature, commandSupplier));
            return this;
        }

        public FeatureGatedCommand or(Supplier<Command> fallback) {
            return new FeatureGatedCommand(this.entries, fallback);
        }

        public FeatureGatedCommand orNone() {
            return or(Commands::none);
        }
    }

    private static class FastDeferredCommandBodge extends Command {
        private final Supplier<Command> nextCommandSupplier;

        private FastDeferredCommandBodge(Supplier<Command> nextCommandSupplier) {
            this.nextCommandSupplier = nextCommandSupplier;
        }

        @Override
        public void schedule() {
            nextCommandSupplier.get().schedule();
        }

        @Override
        public void initialize() {
            throw new RuntimeException("this should never happen since this'll never actually be scheduled");
        }

        @Override
        public void execute() {
            throw new RuntimeException("this should never happen since this'll never actually be scheduled");
        }

        @Override
        public void end(boolean interrupted) {
            throw new RuntimeException("this should never happen since this'll never actually be scheduled");
        }
    }
}
