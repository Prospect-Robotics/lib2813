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
package com.team2813.lib2813.vendor.rev;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.team2813.lib2813.control.InvertType;
import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests for {@link InvertTypes}. */
class InvertTypesTest {

  @ParameterizedTest
  @FieldSource("com.team2813.lib2813.control.InvertType#rotationValues")
  public void toSparkMaxInvert(InvertType invertType) {
    Optional<Boolean> value = InvertTypes.toSparkMaxInvert(invertType);

    assertWithMessage("No InvertedValue exists for InvertType.%s", invertType)
        .that(value)
        .isPresent();
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void toInvertType(boolean sparkMaxInvert) {
    InvertType invertType = InvertTypes.toInvertType(sparkMaxInvert);

    assertThat(invertType).isNotNull();
    assertThat(InvertTypes.toSparkMaxInvert(invertType)).hasValue(sparkMaxInvert);
  }
}
