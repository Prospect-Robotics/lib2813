/*
Copyright 2025 Prospect Robotics SWENext Club

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
package com.team2813.lib2813.limelight;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalLong;

final class Optionals {
  // make class non-instantiable
  private Optionals() {
    throw new AssertionError("cannot create Optionals instance");
  }

  static OptionalLong unboxLong(Optional<Long> val) {
    return val.map(OptionalLong::of).orElseGet(OptionalLong::empty);
  }

  static OptionalDouble unboxDouble(Optional<Double> val) {
    return val.map(OptionalDouble::of).orElseGet(OptionalDouble::empty);
  }
}
