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

import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.RuntimeType;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import edu.wpi.first.wpilibj.simulation.SimHooks;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;

/** JUnit Jupiter extension for testing code that depends on WPILib. */
final class InitWPILibExtension
    implements Extension,
        AfterAllCallback,
        AfterEachCallback,
        BeforeAllCallback,
        ParameterResolver {
  private static final Namespace NAMESPACE = Namespace.create(InitWPILibExtension.class);
  private static final StoreKey<InitWPILib> ANNOTATION_KEY = StoreKey.of(InitWPILib.class);

  @Override
  public void beforeAll(ExtensionContext context) {
    Store store = context.getStore(NAMESPACE);
    ANNOTATION_KEY.put(store, getAnnotation(context));
    // Ensure the Hardware Abstraction Layer is initialized before we try to use it. This logic is
    // based on a comment from Peter Johnson at
    // https://www.chiefdelphi.com/t/driverstation-getalliance-in-gradle-test/
    if (!HAL.initialize(500, 0)) {
      throw new IllegalStateException("Could not initialize Hardware Abstraction Layer");
    }
    DriverStationSim.setEnabled(true);
    DriverStationSim.notifyNewData();
    SimHooks.setHALRuntimeType(RuntimeType.kSimulation.value);

    CommandScheduler commandScheduler = CommandScheduler.getInstance();
    commandScheduler.enable();
    commandScheduler.cancelAll();
    commandScheduler.unregisterAllSubsystems();
  }

  @Override
  public void afterEach(ExtensionContext context) {
    CommandScheduler commandScheduler = CommandScheduler.getInstance();
    commandScheduler.cancelAll();
    commandScheduler.unregisterAllSubsystems();
  }

  @Override
  public void afterAll(ExtensionContext context) {
    CommandScheduler commandScheduler = CommandScheduler.getInstance();
    commandScheduler.cancelAll();
    commandScheduler.unregisterAllSubsystems();
    commandScheduler.disable();
    DriverStationSim.setEnabled(false);
    DriverStationSim.notifyNewData();
  }

  @Override
  public boolean supportsParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return parameterContext.getParameter().getType().isAssignableFrom(CommandsTester.class);
  }

  @Override
  public CommandTester resolveParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext) {
    InitWPILib annotation = ANNOTATION_KEY.get(extensionContext.getStore(NAMESPACE));
    return new CommandsTester(annotation);
  }

  private static InitWPILib getAnnotation(ExtensionContext context) {
    return AnnotationSupport.findAnnotation(
            context.getRequiredTestClass(), InitWPILib.class, context.getEnclosingTestClasses())
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Could not find enclosed class annotated with @InitWPILib"));
  }
}
