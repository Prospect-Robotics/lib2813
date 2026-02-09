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
import edu.wpi.first.networktables.NetworkTableListener;
import edu.wpi.first.wpilibj.Preferences;
import java.lang.reflect.Field;
import org.junit.rules.ExternalResource;

/**
 * A JUnit rule that ensures that changes to preferences done by a test are not leaked out to other
 * tests.
 */
final class IsolatedPreferences extends ExternalResource {
  private NetworkTableInstance tempInstance;
  private NetworkTableInstance prevInstance;

  /** Gets the {@link NetworkTable} that contains the preference values. */
  public NetworkTable getPreferencesTable() {
    return tempInstance.getTable("Preferences");
  }

  @Override
  protected void before() {
    prevInstance = Preferences.getNetworkTable().getInstance();
    tempInstance = NetworkTableInstance.create();
    Preferences.setNetworkTableInstance(tempInstance);
    removePreferencesListener();
  }

  @Override
  protected void after() {
    Preferences.setNetworkTableInstance(prevInstance);

    // Clear out the listener queue before destroying our temporary NetworkTableInstance.
    //
    // This works around a race condition in WPILib where a listener registered by Preferences can
    // be called after the NetworkTableInstance was closed (see
    // https://github.com/wpilibsuite/allwpilib/issues/8215).
    if (!tempInstance.waitForListenerQueue(4)) {
      System.err.println(
          "Timed out waiting for the NetworkTableInstance listener queue to empty (waited 400ms);"
              + " will not close temporary NetworkTableInstance");
    } else {
      tempInstance.close();
    }
  }

  /**
   * Removes the listener installed by {@link
   * Preferences#setNetworkTableInstance(NetworkTableInstance)}.
   *
   * <p>The listener is a constant source of SIGSEGVs in our GitHub test actions.
   */
  private static void removePreferencesListener() {
    try {
      Field listnerField = Preferences.class.getDeclaredField("m_listener");
      listnerField.setAccessible(true);
      NetworkTableListener listener = (NetworkTableListener) listnerField.get(null);
      listnerField.set(null, null);
      listener.close();
    } catch (NoSuchFieldException | IllegalAccessException e) {
    }
  }
}
