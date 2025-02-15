package com.team2813.lib2813.feature;

import static java.util.Objects.requireNonNull;

import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.WrapperCommand;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** A command that is commandSelected via one or more features. */
public final class FeatureControlledCommand extends WrapperCommand {
    private final boolean commandSelected;
    private final String expression;

    /** If the features are all disabled, use the provided command. */
    public Command otherwise(Supplier<Command> commandSupplier) {
        if (commandSelected) {
            return this;
        }
        return new FeatureControlledCommand(commandSupplier.get(), String.format("not (%s)", expression));
    }

    /** If the features are disabled, and the following feature is commandSelected, use the provided command. */
    public <T extends Enum<T> & FeatureIdentifier>  FeatureControlledCommand elseIfEnabled(T feature, Supplier<Command> commandSupplier) {
        if (commandSelected) {
            return this;
        }
        return ifAllEnabled(commandSupplier, Collections.singleton(feature),
                s -> String.format("not %s and (%s)", expression, s));
    }

    String getExpression() {
        return expression;
    }

    static FeatureControlledCommand ifAllEnabled(
            Supplier<Command> commandSupplier, Collection<FeatureIdentifier> features) {
        return ifAllEnabled(commandSupplier, features, s -> s);
    }

    private static FeatureControlledCommand ifAllEnabled(
            Supplier<Command> commandSupplier, Collection<FeatureIdentifier> features, Function<String, String> expressionBuilder) {
        requireNonNull(commandSupplier, "commandSupplier cannot be null");
        requireNonNull(features, "features cannot be null");

        String expression = expressionBuilder.apply(
                features.stream().map(FeatureIdentifier::name).collect(Collectors.joining(", ")));

        if (features.stream().noneMatch(Objects::isNull) &&
                features.stream().allMatch(FeatureIdentifier::enabled)) {
            return new FeatureControlledCommand(commandSupplier.get(), expression);
        }
        return new FeatureControlledCommand(null, "not " + expression);
    }

    private FeatureControlledCommand(Command command, String expression) {
        super(command == null ? Commands.none() : command);
        this.commandSelected = (command != null);
        this.expression = expression;
    }

    @Override
    public void initSendable(SendableBuilder builder) {
        super.initSendable(builder);
        builder.publishConstString("expression", expression);
    }
}
