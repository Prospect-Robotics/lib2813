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
 * JUnit Jupiter annotation used to signal tests that depends on WPILib.
 *
 * <p>Also provides a {@link CommandTester} for tests.
 *
 * <p>Example use:
 *
 * <pre>{@code
 * @InitWPILib
 * public final class FlightSubsystemTest {
 *
 *   @Test
 *   public void initiallyNotInAir() {
 *     var flight = new FlightSubsystem();
 *
 *     assertThat(flight.inAir()).isFalse();
 *   }
 *
 *   @Test
 *   public void takesFlight(CommandTester commandTester) {
 *     var flight = new FlightSubsystem();
 *     Command takeOff = flight.createTakeOffCommandCommand();
 *
 *     commandTester.runUntilComplete(takeOff);
 *
 *     assertThat(flight.inAir()).isTrue();
 *   }
 * }
 * }</pre>
 *
 * @since 2.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(InitWPILibExtension.class)
public @interface InitWPILib {}
