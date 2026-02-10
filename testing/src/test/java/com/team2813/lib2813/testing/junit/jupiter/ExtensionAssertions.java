/*
Copyright 2025-2026 Prospect Robotics SWENext Club

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
package com.team2813.lib2813.testing.junit.jupiter;

import java.util.List;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.Event;
import org.junit.platform.testkit.engine.Events;
import org.opentest4j.MultipleFailuresError;

/**
 * A collection of utility methods that support asserting conditions in tests of JUnit Extensions.
 */
final class ExtensionAssertions {

  /**
   * Asserts that the supplied {@code events} do not contain any failures.
   *
   * @param events Events fired during execution of a test plan on the JUnit Platform.
   */
  public static void assertHasNoFailures(Events events, String category) {
    if (!events.failed().list().isEmpty()) {
      List<AssertionError> failures =
          events.failed().stream().map(Event::toString).map(AssertionError::new).toList();
      throw new MultipleFailuresError(
          String.format("Expected no failed events with category '%s'", category), failures);
    }

    events.assertStatistics(
        stats -> {
          stats.skipped(0);
          stats.failed(0);
        });
  }

  /**
   * Asserts that the supplied {@code results} do not contain any failures.
   *
   * @param results Results of executing a test plan on the JUnit Platform.
   */
  public static void assertHasNoFailures(EngineExecutionResults results) {
    assertHasNoFailures(results.containerEvents(), "container");
    assertHasNoFailures(results.testEvents(), "test");
  }

  private ExtensionAssertions() {
    throw new AssertionError("Not instantiable");
  }
}
