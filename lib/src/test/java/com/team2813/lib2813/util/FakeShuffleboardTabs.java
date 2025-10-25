package com.team2813.lib2813.util;

import static edu.wpi.first.util.ErrorMessages.requireNonNullParam;

import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A fake implementation of {@link ShuffleboardTabs} for testing purposes.
 *
 * <p>Each instance of this class generates a unique prefix so that tab names do not collide with
 * real Shuffleboard tabs during testing. This allows multiple tests to run in parallel without
 * interfering with each other.
 */
public final class FakeShuffleboardTabs implements ShuffleboardTabs {

  /** Generates unique IDs for each instance. */
  private static final AtomicInteger nextValue = new AtomicInteger(1);

  /** Prefix added to every tab name to ensure uniqueness. */
  private final String prefix;

  /** Constructs a new {@link FakeShuffleboardTabs} instance with a unique prefix. */
  public FakeShuffleboardTabs() {
    prefix = "f" + nextValue.getAndIncrement();
  }

  /**
   * Returns a Shuffleboard tab with the given title, prefixed to avoid collisions.
   *
   * @param title The title of the tab (must be non-null)
   * @return The Shuffleboard tab corresponding to this prefixed title
   * @throws NullPointerException if title is null
   */
  @Override
  public ShuffleboardTab getTab(String title) {
    requireNonNullParam(title, "title", "getTab");
    return Shuffleboard.getTab(prefix + title);
  }

  /**
   * Selects the Shuffleboard tab with the given title, prefixed to avoid collisions.
   *
   * @param title The title of the tab (must be non-null)
   * @throws NullPointerException if title is null
   */
  @Override
  public void selectTab(String title) {
    requireNonNullParam(title, "title", "getTab");
    Shuffleboard.selectTab(prefix + title);
  }
}
