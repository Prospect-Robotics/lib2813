package com.team2813.lib2813.util;

import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;

/**
 * Provides an abstraction over Shuffleboard tabs.
 *
 * <p>This interface acts as a wrapper around {@link Shuffleboard#getTab(String)} and {@link
 * Shuffleboard#selectTab(String)}, providing a seam for testing. By using this interface, code
 * can interact with Shuffleboard tabs without directly depending on the static Shuffleboard API,
 * which is useful for unit tests and mocking.
 */
public interface ShuffleboardTabs {

  /**
   * Returns the {@link ShuffleboardTab} with the given title.
   *
   * @param title the title of the tab
   * @return the Shuffleboard tab with the specified title
   */
  ShuffleboardTab getTab(String title);

  /**
   * Selects the tab with the given title as the currently active tab in Shuffleboard.
   *
   * @param title the title of the tab to select
   */
  void selectTab(String title);
}
