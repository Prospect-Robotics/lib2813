/*
Copyright 2023-2026 Prospect Robotics SWENext Club

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
package com.team2813.lib2813.vendor.ctre.motor;

import static org.junit.Assert.assertEquals;

import com.team2813.lib2813.control.InvertType;
import org.junit.Test;

public class TalonFXEqualsTest {
  @Test
  public void IdentityTest() {
    TalonFXWrapper motor = new TalonFXWrapper(0, InvertType.CLOCKWISE);
    assertEquals(motor, motor);
  }
}
