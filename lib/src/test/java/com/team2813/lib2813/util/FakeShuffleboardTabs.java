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
package com.team2813.lib2813.util;

import static edu.wpi.first.util.ErrorMessages.requireNonNullParam;

import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import java.util.concurrent.atomic.AtomicInteger;

public final class FakeShuffleboardTabs implements ShuffleboardTabs {
  private static final AtomicInteger nextValue = new AtomicInteger(1);
  private final String prefix;

  public FakeShuffleboardTabs() {
    prefix = "f" + nextValue.getAndIncrement();
  }

  @Override
  public ShuffleboardTab getTab(String title) {
    requireNonNullParam(title, "title", "getTab");
    return Shuffleboard.getTab(prefix + title);
  }

  @Override
  public void selectTab(String title) {
    requireNonNullParam(title, "title", "getTab");
    Shuffleboard.selectTab(prefix + title);
  }
}
