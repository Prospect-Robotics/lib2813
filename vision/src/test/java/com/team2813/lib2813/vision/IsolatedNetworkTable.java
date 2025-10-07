package com.team2813.lib2813.vision;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Preferences;
import org.junit.rules.ExternalResource;

/**
 * A JUnit rule that creates a temporary {@link NetworkTableInstance} for each test.
 *
 * <p>The rule also updates the {@code NetworkTableInstance} used by {@link Preferences} to use the
 * same temporary NetworkTableInstance.
 */
final class IsolatedNetworkTable extends ExternalResource {
  private NetworkTableInstance tempInstance;

  /** Gets the temporary {@link NetworkTableInstance}. */
  public NetworkTableInstance getNetworkTableInstance() {
    return tempInstance;
  }

  /** Gets the {@link NetworkTable} that contains the preference values. */
  public NetworkTable getPreferencesTable() {
    return tempInstance.getTable("Preferences");
  }

  @Override
  protected void before() {
    NetworkTableInstance.getDefault();
    tempInstance = NetworkTableInstance.create();
    tempInstance.startLocal();
    Preferences.setNetworkTableInstance(tempInstance);
  }

  @Override
  protected void after() {
    if (!tempInstance.waitForListenerQueue(.1)) {
      System.err.println(
          "Timed out waiting for the NetworkTableInstance listener queue to empty (waited 100ms);"
              + " JVM may crash");
    }
    Preferences.setNetworkTableInstance(NetworkTableInstance.getDefault());
    tempInstance.close();
  }
}
