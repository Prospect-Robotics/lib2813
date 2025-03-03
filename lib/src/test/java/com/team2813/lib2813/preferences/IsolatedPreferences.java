package com.team2813.lib2813.preferences;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Preferences;
import org.junit.rules.ExternalResource;

final class IsolatedPreferences extends ExternalResource {

  @Override
  protected void before() {
    Preferences.setNetworkTableInstance(NetworkTableInstance.create());
  }

  @Override
  protected void after() {
    Preferences.setNetworkTableInstance(NetworkTableInstance.getDefault());
  }
}
