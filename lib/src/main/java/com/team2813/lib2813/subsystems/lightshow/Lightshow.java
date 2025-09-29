package com.team2813.lib2813.subsystems.lightshow;

import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Abstract base class for robot lightshow subsystems.
 *
 * <p>A Lightshow subsystem manages a set of {@link State}s that determine the colors to be displayed
 * on LEDs or other lighting hardware. Implementations define how to actually apply the color via
 * {@link #useColor(Color)} and how to compute the active color in {@link #update()}.
 *
 * <p>States can be represented as enums implementing {@link State}, or as arbitrary {@link State}
 * instances.
 * 
 * <p>There is a built-in static {@link #off} state that always represents the lights being off.
 *
 * @author Team 2813
 */
public abstract class Lightshow extends SubsystemBase {

  /** A static state representing that the lights should be off (always applied). */
  protected static final State off = new State() {
    @Override
    public Color color() {
      return new Color(0, 0, 0);
    }

    @Override
    public boolean apply() {
      return true;
    }
  };

  /** The set of active states that the lightshow considers when updating the color. */
  protected final Set<State> states = new HashSet<>();

  /** The default state to use if no other state is active. */
  protected Optional<? extends State> defaultState = Optional.empty();

  /**
   * Constructs a Lightshow using an enum class representing the states.
   *
   * @param <T> the type of enum implementing {@link State}
   * @param enumClass the enum class containing the states
   */
  protected <T extends Enum<T> & State> Lightshow(Class<T> enumClass) {
    addStates(enumClass);
  }

  /**
   * Constructs a Lightshow with a set of states.
   *
   * @param states the initial states for this Lightshow
   */
  protected Lightshow(Set<? extends State> states) {
    addStates(states);
  }

  /**
   * Implementation-defined method to actually use a {@link Color}.
   *
   * @param c the color to display
   */
  protected abstract void useColor(Color c);

  /**
   * Adds states from an enum class implementing {@link State}.
   *
   * @param <T> the enum type implementing {@link State}
   * @param enumClass the enum class containing the states
   */
  public final <T extends Enum<T> & State> void addStates(Class<? extends T> enumClass) {
    T[] constants = enumClass.getEnumConstants();
    states.addAll(Arrays.asList(constants));
  }

  /**
   * Adds arbitrary {@link State} instances to the Lightshow.
   *
   * @param states the states to add
   */
  public final void addStates(Set<? extends State> states) {
    this.states.addAll(states);
  }

  /**
   * Updates the current active {@link State}s and determines the color to display.
   *
   * @return an optional containing the color to display, or empty if no state is active
   */
  protected abstract Optional<Color> update();

  /**
   * Called periodically by the scheduler to update the lights.
   *
   * <p>This implementation calls {@link #update()}, and applies the resulting color using
   * {@link #useColor(Color)}. If {@link #update()} returns empty, the {@link #defaultState}'s
   * color is used (if present). If neither provides a color, {@link #useColor(Color)} may not be
   * called.
   */
  @Override
  public void periodic() {
    Optional<Color> color = update().or(() -> defaultState.map(State::color));
    color.ifPresent(this::useColor);
  }

  /**
   * Sets the default {@link State} to use if no other state is active.
   *
   * @param defaultState the default state
   */
  public void setDefaultState(State defaultState) {
    this.defaultState = Optional.of(defaultState);
  }
}
