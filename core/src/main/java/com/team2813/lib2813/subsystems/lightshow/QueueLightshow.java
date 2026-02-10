/*
Copyright 2024-2026 Prospect Robotics SWENext Club

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.team2813.lib2813.subsystems.lightshow;

import edu.wpi.first.wpilibj.util.Color;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.Set;

/**
 * A lightshow that changes color when a State transitions from inactive to active.
 *
 * <p>All states that return {@code true} on a call to {@link State#isActive()} are pushed to a
 * deque. States are only removed from the deque if they are at the front and {@link
 * State#isActive()} returns {@code false}. When a state is removed, the next one will be activated
 * if {@link State#isActive()} returns {@code true}, until either a state returns {@code true} upon
 * a call to {@link State#isActive()}, in which the color will be used, or there are no states where
 * {@link State#isActive()} return {@code true}, then the default color is used.
 *
 * <p>For example usage, see FRC Team 2813's <a
 * href="https://github.com/Prospect-Robotics/Robot2024/blob/master/Robot2024/src/main/java/com/team2813/subsystems/LEDs.java"
 * >2024 robot code</a>.
 */
public abstract class QueueLightshow extends Lightshow {
  private final Deque<State> activatedStates = new ArrayDeque<>();

  public <T extends Enum<T> & State> QueueLightshow(Class<T> enumClass) {
    super(enumClass);
    defaultState = Optional.of(Lightshow.off);
  }

  public QueueLightshow(Set<State> states) {
    super(states);
    defaultState = Optional.of(Lightshow.off);
  }

  @Override
  protected Optional<Color> update() {
    // NOTE: Even though this class is called QueueLightshow, we are treating activatedStates as a
    // stack, not a queue.

    // If new States become active, push them, so we change the color.
    for (State s : states) {
      if (!activatedStates.contains(s) && s.isActive()) {
        activatedStates.addFirst(s); // Push
      }
    }

    // Pick the color to display. We start with the State that was most recently pushed, so the
    // color returned changes when a either a State transitions to active or when the State
    // associated with the previous color transitioned to inactive.
    do {
      State s = activatedStates.peekFirst();
      if (s == null) {
        return Optional.empty(); // No states active. The lightshow is over. 😿
      }
      if (s.isActive()) {
        return Optional.of(s.color());
      }
      activatedStates.removeFirst(); // Pop
    } while (true);
  }
}
