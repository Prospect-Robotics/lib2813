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
