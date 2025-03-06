package com.team2813.lib2813.preferences;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Preferences;
import org.junit.rules.ExternalResource;

public final class IsolatedPreferences extends ExternalResource {
  private NetworkTableInstance instance;

  public NetworkTableInstance getNetworkTableInstance() {
    return instance;
  }

  @Override
  protected void before() {
    instance = NetworkTableInstance.create();
    Preferences.setNetworkTableInstance(instance);
  }

  @Override
  protected void after() {
    try {
      Preferences.setNetworkTableInstance(NetworkTableInstance.getDefault());
    } finally {
      instance.close();
    }
  }
}
