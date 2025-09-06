package com.team2813.lib2813.preferences;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Preferences;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.rules.ExternalResource;

/**
 * A JUnit rule that ensures that changes to preferences done by a test are not leaked out to other
 * tests.
 */
public final class IsolatedPreferences extends ExternalResource {
  private static final ScheduledExecutorService CLOSE_EXECUTOR =
      Executors.newSingleThreadScheduledExecutor();
  private NetworkTableInstance tempInstance;

  /** Gets the {@link NetworkTable} that contains the preference values. */
  public NetworkTable getPreferencesTable() {
    return tempInstance.getTable("Preferences");
  }

  @Override
  protected void before() {
    NetworkTableInstance.getDefault();
    tempInstance = NetworkTableInstance.create();
    Preferences.setNetworkTableInstance(tempInstance);
  }

  @Override
  protected void after() {
    Preferences.setNetworkTableInstance(NetworkTableInstance.getDefault());
    CLOSE_EXECUTOR.schedule(() -> tempInstance.close(), 10, TimeUnit.MILLISECONDS);
  }
}
