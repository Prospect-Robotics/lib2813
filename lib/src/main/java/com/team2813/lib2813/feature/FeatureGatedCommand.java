package com.team2813.lib2813.feature;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class FeatureGatedCommand extends FeatureGated<Command> {
    private FeatureGatedCommand(List<Entry<Command>> entries, Supplier<Command> defaultCommand) {
        super(entries, defaultCommand);
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

    public static class Builder extends FeatureGated.BaseBuilder<Command, FeatureGatedCommand, Builder> {
        public FeatureGatedCommand orNone() {
            return or(Commands::none);
        }

        @Override
        protected FeatureGatedCommand build(List<Entry<Command>> entries, Supplier<Command> fallback) {
            return new FeatureGatedCommand(entries, fallback);
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
