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
package com.team2813.lib2813.testing.truth;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;

interface Component {

  Type getType();

  enum Type {
    TRANSLATION,
    ROTATION
  }

  abstract class ComponentArgumentsProvider<T extends Component> implements ArgumentsProvider {
    private final List<T> values;

    protected ComponentArgumentsProvider(Type componentType, Stream<T> allValues) {
      values = allValues.filter(c -> c.getType() == componentType).toList();
    }

    @Override
    public final Stream<? extends Arguments> provideArguments(
        ParameterDeclarations parameters, ExtensionContext context) {
      return values.stream().map(Arguments::of);
    }
  }
}
