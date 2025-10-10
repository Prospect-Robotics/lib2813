package com.team2813.lib2813.preferences;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Preferences;
import org.junit.rules.ExternalResource;

/**
 * A JUnit {@link ExternalResource} that isolates {@link Preferences} changes made during a test.
 *
 * <p>This rule ensures that modifications to WPILib preferences within a test do not persist or
 * affect other tests. It accomplishes this by creating a temporary {@link NetworkTableInstance}
 * and associating it with {@link Preferences} for the duration of the test.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * @Rule
 * public IsolatedPreferences prefs = new IsolatedPreferences();
 *
 * @Test
 * public void testPreferenceChange() {
 *     prefs.getPreferencesTable().getEntry("example").setDouble(42.0);
 *     // test logic
 * }
 * }</pre>
 */
public final class IsolatedPreferences extends ExternalResource {

  /** The temporary {@link NetworkTableInstance} used to isolate preference changes. */
  private NetworkTableInstance tempInstance;

  /**
   * Gets the {@link NetworkTable} used by {@link Preferences} in this test.
   *
   * @return the NetworkTable for preferences
   */
  public NetworkTable getPreferencesTable() {
    return tempInstance.getTable("Preferences");
  }

  /** Sets up a temporary NetworkTable instance before each test. */
  @Override
  protected void before() {
    // Ensure the default instance is initialized
    NetworkTableInstance.getDefault();

    // Create a temporary local NetworkTable instance for isolation
    tempInstance = NetworkTableInstance.create();
    tempInstance.startLocal();

    // Redirect Preferences to use the temporary instance
    Preferences.setNetworkTableInstance(tempInstance);
  }

  /** Cleans up the temporary instance after each test. */
  @Override
  protected void after() {
    // Wait briefly for listener queue to empty to avoid potential JVM crash
    if (!tempInstance.waitForListenerQueue(.1)) {
      System.err.println(
          "Timed out waiting for the NetworkTableInstance listener queue to empty (waited 100ms);"
              + " JVM may crash");
    }

    // Restore Preferences to the default NetworkTable instance
    Preferences.setNetworkTableInstance(NetworkTableInstance.getDefault());
    tempInstance.close();
  }
}
