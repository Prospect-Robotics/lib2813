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
package com.team2813.lib2813.preferences;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Preferences;
import org.junit.rules.ExternalResource;

/**
 * A JUnit rule that ensures that changes to preferences done by a test are not leaked out to other
 * tests.
 */
final class IsolatedPreferences extends ExternalResource {
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
    Preferences.setNetworkTableInstance(NetworkTableInstance.getDefault());
    if (!tempInstance.waitForListenerQueue(.2)) {
      System.err.println(
          "Timed out waiting for the NetworkTableInstance listener queue to empty (waited 200ms);"
              + " JVM may crash");
    }
    tempInstance.close();
  }
}
