package com.team2813.lib2813.preferences;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Preferences;
import org.junit.rules.ExternalResource;

/**
 * A JUnit rule that ensures that changes to preferences done by a test are not leaked out to other
 * tests.
 */
public final class IsolatedPreferences extends ExternalResource {
  private NetworkTableInstance tempInstance;

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
