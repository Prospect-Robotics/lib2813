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
    tempInstance = NetworkTableInstance.create();
    Preferences.setNetworkTableInstance(tempInstance);
  }

  @Override
  protected void after() {
    try {
      Preferences.setNetworkTableInstance(NetworkTableInstance.getDefault());
    } finally {
      tempInstance.close();
    }
  }
}
