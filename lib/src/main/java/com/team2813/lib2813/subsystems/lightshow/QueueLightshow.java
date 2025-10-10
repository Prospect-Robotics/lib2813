package com.team2813.lib2813.subsystems.lightshow;

import edu.wpi.first.wpilibj.util.Color;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.Set;

/**
 * A Lightshow implementation that manages a queue of active states.
 *
 * <p>Behavior:
 *
 * <ul>
 *   <li>All {@link State}s that return {@code true} for {@link State#apply()} are added to a queue.
 *   <li>States are removed from the front of the queue if {@link State#apply()} returns {@code
 *       false}.
 *   <li>The last active state in the queue is used to determine the color to display.
 *   <li>If no states are active, the {@link Lightshow#off} state or {@link #defaultState} is used.
 * </ul>
 *
 * @author Team 2813
 */
public abstract class QueueLightshow extends Lightshow {

  private final Deque<State> activatedStates = new ArrayDeque<>();

  /**
   * Creates a QueueLightshow from an enum class of {@link State}s.
   *
   * @param <T> the enum type implementing {@link State}
   * @param enumClass the enum class containing the states
   */
  public <T extends Enum<T> & State> QueueLightshow(Class<T> enumClass) {
    super(enumClass);
    defaultState = Optional.of(Lightshow.off);
  }

  /**
   * Creates a QueueLightshow from a set of {@link State}s.
   *
   * @param states the set of states to use
   */
  public QueueLightshow(Set<State> states) {
    super(states);
    defaultState = Optional.of(Lightshow.off);
  }

  /**
   * Updates the queue of active states and returns the current color to display.
   *
   * <p>New states that return {@code true} for {@link State#apply()} are added to the front of the
   * queue. States at the front of the queue that return {@code false} are removed. The color of the
   * first active state is used.
   *
   * @return the color of the current active state, or empty if no states are active
   */
  @Override
  protected Optional<Color> update() {
    // Add new active states that are not already in the queue
    for (State s : states) {
      if (!activatedStates.contains(s) && s.apply()) {
        activatedStates.addFirst(s);
      }
    }

    // Remove inactive states from the front
    while (!activatedStates.isEmpty()) {
      State s = activatedStates.poll();
      if (s.apply()) {
        activatedStates.addFirst(s);
        return Optional.of(s.color());
      }
    }

    return Optional.empty();
  }
}
