package com.team2813.lib2813.util;

import java.time.ZonedDateTime;

/**
 * Holder for data collected at build time about the robot code.
 *
 * @author Team 2813
 */
public interface BuildConstants {

  /** The current git branch when the code was built. */
  String gitBranch();

  /** The time the most recent commit at HEAD was submitted. */
  ZonedDateTime gitSubmitTime();

  /** The time the code was built. */
  ZonedDateTime buildTime();
}
