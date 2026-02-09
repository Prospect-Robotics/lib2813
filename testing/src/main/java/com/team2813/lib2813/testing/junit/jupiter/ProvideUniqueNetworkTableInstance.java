/*
Copyright 2026 Prospect Robotics SWENext Club

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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * JUnit Jupiter annotation used to signal tests that need an isolated NetworkTableInstance.
 *
 * <p>Example use:
 *
 * <pre>{@code
 * @ProvideUniqueNetworkTableInstance
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
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(ProvideUniqueNetworkTableInstanceExtension.class)
public @interface ProvideUniqueNetworkTableInstance {

  /**
   * How long to wait for the listener queue to empty before destroying the temporary network table
   * instance.
   */
  double waitForListenerQueueSeconds() default 0.6;

  /**
   * Whether to call {@link
   * edu.wpi.first.wpilibj.Preferences#setNetworkTableInstance(edu.wpi.first.networktables.NetworkTableInstance)}
   * with the temporary network table instance before starting each test.
   */
  boolean replacePreferencesNetworkTable() default false;
}
