/*
Copyright 2024-2026 Prospect Robotics SWENext Club

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

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum InvertType {
  CLOCKWISE,
  COUNTER_CLOCKWISE,
  FOLLOW_MASTER,
  OPPOSE_MASTER;

  /**
   * A set of all {@link InvertType}s that have a phoenix and spark max invert. Anything that isn't
   * in this set is for motor following
   */
  public static final Set<InvertType> rotationValues =
      Collections.unmodifiableSet(EnumSet.of(CLOCKWISE, COUNTER_CLOCKWISE));
}
