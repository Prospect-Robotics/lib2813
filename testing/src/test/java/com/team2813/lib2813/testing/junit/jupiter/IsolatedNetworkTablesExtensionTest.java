/*
Copyright 2025 Prospect Robotics SWENext Club

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

import static com.google.common.truth.Truth.assertThat;
import static com.team2813.lib2813.testing.junit.jupiter.ExtensionAssertions.assertHasNoFailures;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import edu.wpi.first.networktables.NetworkTableInstance;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

/** Tests for {@link IsolatedNetworkTablesExtension}. */
class IsolatedNetworkTablesExtensionTest {

  @ExtendWith(IsolatedNetworkTablesExtension.class)
  @Tag("ignore-outside-testkit")
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  public static class SampleTest {

    @Test
    public void verifyProvidesNetworkTableParameter(NetworkTableInstance ntInstance) {
      assertThat(ntInstance).isNotNull();
      assertThat(ntInstance.getHandle())
          .isNotEqualTo(NetworkTableInstance.getDefault().getHandle());
    }
  } // end SampleTest

  @Test
  void verifyExtension() {
    // Act - Run tests in SampleTest
    EngineExecutionResults results =
        EngineTestKit.engine("junit-jupiter").selectors(selectClass(SampleTest.class)).execute();

    // Assert - All tests in SampleTest pass
    assertHasNoFailures(results);
  }
}
