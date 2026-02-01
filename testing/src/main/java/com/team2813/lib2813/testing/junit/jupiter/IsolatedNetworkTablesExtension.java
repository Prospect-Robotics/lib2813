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
package com.team2813.lib2813.testing.junit.jupiter;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Preferences;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * JUnit Jupiter extension for providing an isolated NetworkTableInstance to tests.
 *
 * <p>Example use:
 *
 * <pre>{@code
 * @ExtendWith(IsolatedNetworkTablesExtension.class)
 * public final class IntakeTest {
 *
 *   @Test
 *   public void intakeCoral(NetworkTableInstance ntInstance)  {
 *     // Do something with ntInstance
 *   }
 * }
 * }</pre>
 *
 * @since 2.0.0
 */
public final class IsolatedNetworkTablesExtension
    implements Extension, BeforeEachCallback, AfterEachCallback, ParameterResolver {
  private static final Namespace NAMESPACE = Namespace.create(IsolatedNetworkTablesExtension.class);
  private static final StoreKey<Data> DATA_KEY = StoreKey.of(Data.class);

  @Override
  public void beforeEach(ExtensionContext context) {
    Store store = context.getStore(NAMESPACE);
    NetworkTableInstance ntInstance =
        DATA_KEY.getOrComputeIfAbsent(store, Data::create).testInstance;
    Preferences.setNetworkTableInstance(ntInstance);
  }

  @Override
  public void afterEach(ExtensionContext context) {
    // If this extension created a temporary NetworkTableInstance, close it.
    Store store = context.getStore(NAMESPACE);
    Data data = DATA_KEY.remove(store);
    if (data != null) {
      Preferences.setNetworkTableInstance(data.prevInstance);

      // Clear out the listener queue before destroying our temporary NetworkTableInstance.
      //
      // This works around a race condition in WPILib where a listener registered by Preferences can
      // be called after the NetworkTableInstance was closed (see
      // https://github.com/wpilibsuite/allwpilib/issues/8215).
      if (!data.testInstance.waitForListenerQueue(.2)) {
        System.err.println(
            "Timed out waiting for the NetworkTableInstance listener queue to empty (waited 200ms);"
                + " JVM may crash");
      }
      data.testInstance.close();
    }
  }

  @Override
  public boolean supportsParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    if (parameterContext.getTarget().isEmpty()) {
      return false; // The extension only supports test method parameter injection.
    }
    return NetworkTableInstance.class.equals(parameterContext.getParameter().getType());
  }

  @Override
  public NetworkTableInstance resolveParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    Store store = extensionContext.getStore(NAMESPACE);
    return DATA_KEY.get(store).testInstance;
  }

  private record Data(NetworkTableInstance testInstance, NetworkTableInstance prevInstance) {
    static Data create() {
      NetworkTableInstance testInstance = NetworkTableInstance.create();
      testInstance.startLocal();
      NetworkTableInstance prevInstance = Preferences.getNetworkTable().getInstance();
      return new Data(testInstance, prevInstance);
    }
  }
}
