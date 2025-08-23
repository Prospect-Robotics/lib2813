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
