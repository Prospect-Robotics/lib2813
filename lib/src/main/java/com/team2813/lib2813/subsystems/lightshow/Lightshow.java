package com.team2813.lib2813.subsystems.lightshow;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj2.command.Subsystem;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public abstract class Lightshow extends SubsystemBase {
	/**
	 * A static state that signifies that the lights should be off, that is always applied
	 */
	protected static final State off = new State() {
		public Color color() {
			return new Color(0, 0, 0);
		};
		public boolean apply() {
			return true;
		};
	};
	protected Set<State> states = new HashSet<>();
	protected Optional<? extends State> defaultState;

	abstract protected void useColor(Color c);

	/**
	 * Creates a new Lightshow subsystem using an enum. Uses the given {@code enumClass} to get a
	 * list of {@link State}s to use.
	 * 
	 * @param <T> The type of an enum that implements state
	 * @param enumClass
	 * @param colorConsumer
	 */
	protected <T extends Enum<T> & State> Lightshow(Class<T> enumClass) {
		addStates(enumClass);
	}

	/**
	 * Creates a new Lightshow subsystem with a set of states.
	 * @param states
	 * @param colorConsumer
	 */
	protected Lightshow(Set<? extends State> states) {
		addStates(states);
	}

	public final <T extends Enum<T> & State> void addStates(Class<? extends T> enumClass) {
		T[] constants = enumClass.getEnumConstants();
		states.addAll(Arrays.asList(constants));
	}

	public final void addStates(Set<? extends State> states) {
		this.states.addAll(states);
	}

	/**
	 * Updates the current States.
	 * Makes the current {@link #states} updated,
	 * and gets the Color to be displayed.
	 * 
	 * @implSpec Do anything with {@link #states} to determine which {@link Color}
	 *           to display
	 * 
	 * @return an optional representing the color to be displayed
	 * @see #periodic()
	 */
	protected abstract Optional<Color> update();

	/**
	 * {@inheritDoc}
	 * <p>This implementation of {@link Subsystem#periodic()}
	 * updates the colors by passing the call of {@link #update()}
	 * to the given {@link Consumer}.
	 * If the call to {@link #update()} returns an empty optional,
	 * than the color of the default state is used, or the color is not changed.
	 * 
	 * @implSpec
	 *           The {@link #colorConsumer} is passed the result of
	 *           {@link #update()}, or the {@link #defaultState}'s color. If there
	 *           is no color from {@link #update()} and no default state, there is
	 *           no requirement for what to pass to the {@link #colorConsumer}, or
	 *           if the {@link #colorConsumer} is called at all
	 */
	@Override
	public void periodic() {
		Optional<Color> color = update().or(() -> defaultState.map(State::color));
		if (color.isPresent()) {
			useColor((color.get()));
		}
	}
	
	/**
	 * Sets the {@link State} to be used if none is given.
	 * @param defaultState the {@link State} to be used as a default. 
	 */
	public void setDefaultState(State defaultState) {
		this.defaultState = Optional.of(defaultState);
	}
}
