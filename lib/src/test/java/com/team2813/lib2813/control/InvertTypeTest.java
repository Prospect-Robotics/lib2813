/*
Copyright 2024-2025 Prospect Robotics SWENext Club

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
package com.team2813.lib2813.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.ctre.phoenix6.signals.InvertedValue;
import org.junit.Test;

public class InvertTypeTest {
  @Test
  public void phoenixInvertsExist() {
    for (InvertType v : InvertType.rotationValues) {
      assertTrue(
          String.format("No phoenix invert exists for InvertType %s.", v),
          v.phoenixInvert().isPresent());
    }
  }

  @Test
  public void sparkMaxInvertsExist() {
    for (InvertType v : InvertType.rotationValues) {
      assertTrue(
          String.format("No spark max invert exists for InvertType %s.", v),
          v.phoenixInvert().isPresent());
    }
  }

  @Test
  public void fromPhoenixInvertTest() {
    for (InvertType v : InvertType.rotationValues) {
      InvertedValue val = v.phoenixInvert().orElseThrow();
      assertEquals(v, InvertType.fromPhoenixInvert(val).orElse(null));
    }
  }

  @Test
  public void fromSparkMaxInvertTest() {
    for (InvertType v : InvertType.rotationValues) {
      boolean val = v.sparkMaxInvert().orElseThrow();
      assertEquals(v, InvertType.fromSparkMaxInvert(val).orElse(null));
    }
  }
}
