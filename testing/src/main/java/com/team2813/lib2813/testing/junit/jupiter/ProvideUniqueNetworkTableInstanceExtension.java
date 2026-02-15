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
import edu.wpi.first.networktables.NetworkTableListener;
import edu.wpi.first.wpilibj.Preferences;
import java.lang.reflect.Field;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;

/** JUnit Jupiter extension for providing an isolated NetworkTableInstance to tests. */
final class ProvideUniqueNetworkTableInstanceExtension
    implements Extension,
        BeforeAllCallback,
        BeforeEachCallback,
        AfterEachCallback,
        ParameterResolver {
  private static final Namespace NAMESPACE =
      Namespace.create(ProvideUniqueNetworkTableInstanceExtension.class);
  private static final StoreKey<Data> DATA_KEY = StoreKey.of(Data.class);
  private static final StoreKey<ProvideUniqueNetworkTableInstance> ANNOTATION_KEY =
      StoreKey.of(ProvideUniqueNetworkTableInstance.class);

  @Override
  public void beforeAll(ExtensionContext context) {
    Store store = context.getStore(NAMESPACE);
    ANNOTATION_KEY.put(store, getAnnotation(context));
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    Store store = context.getStore(NAMESPACE);
    NetworkTableInstance ntInstance =
        DATA_KEY.getOrComputeIfAbsent(store, Data::create).testInstance;

    ProvideUniqueNetworkTableInstance annotation = ANNOTATION_KEY.get(store);
    if (annotation.replacePreferencesNetworkTable()) {
      Preferences.setNetworkTableInstance(ntInstance);
    }
    ntInstance.startLocal();
    if (annotation.replacePreferencesNetworkTable()) {
      ntInstance.waitForListenerQueue(1);
      removePreferencesListener();
    }
  }

  @Override
  public void afterEach(ExtensionContext context) {
    // If this extension created a temporary NetworkTableInstance, close it.
    Store store = context.getStore(NAMESPACE);
    Data data = DATA_KEY.remove(store);
    if (data != null) {
      ProvideUniqueNetworkTableInstance annotation = ANNOTATION_KEY.get(store);

      // Clear out the listener queue before destroying our temporary NetworkTableInstance.
      //
      // This works around a race condition in WPILib where a listener registered by Preferences can
      // be called after the NetworkTableInstance was closed (see
      // https://github.com/wpilibsuite/allwpilib/issues/8215).
      double timeout = annotation.waitForListenerQueueSeconds();
      boolean closeInstance = data.testInstance.waitForListenerQueue(timeout);

      Preferences.setNetworkTableInstance(data.prevInstance);
      if (closeInstance) {
        data.testInstance.close();
      } else {
        System.err.printf(
            "Timed out waiting for the NetworkTableInstance listener queue to empty (waited"
                + " %dms); will not close temporary NetworkTableInstance%n",
            Math.round(timeout * 1000));
      }
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
      NetworkTableInstance prevInstance = Preferences.getNetworkTable().getInstance();
      return new Data(testInstance, prevInstance);
    }
  }

  private static ProvideUniqueNetworkTableInstance getAnnotation(ExtensionContext context) {
    return AnnotationSupport.findAnnotation(
            context.getRequiredTestClass(),
            ProvideUniqueNetworkTableInstance.class,
            context.getEnclosingTestClasses())
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Could not find an enclosed class annotated with @IsolatedNetworkTables"));
  }

  /**
   * Removes the listener installed by {@link
   * Preferences#setNetworkTableInstance(NetworkTableInstance)}.
   *
   * <p>The listener is a constant source of SIGSEGVs in our GitHub test actions.
   */
  private static void removePreferencesListener() {
    synchronized (Preferences.class) {
      try {
        Field listenerField = Preferences.class.getDeclaredField("m_listener");
        listenerField.setAccessible(true);
        NetworkTableListener listener = (NetworkTableListener) listenerField.get(null);
        listenerField.set(null, null);
        listener.close();
      } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
      }
    }
  }
}
