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

import edu.wpi.first.math.geometry.Pose3d;
import java.util.*;

/** Implementation of LocationalData where all optional values return empty values. */
final class StubLocationalData implements LocationalData {
  static final StubLocationalData VALID = new StubLocationalData(true);
  static final StubLocationalData INVALID = new StubLocationalData(false);

  private final boolean valid;

  private StubLocationalData(boolean valid) {
    this.valid = valid;
  }

  @Override
  public boolean isValid() {
    return valid;
  }

  @Override
  public boolean hasTarget() {
    return false;
  }

  @Override
  public Optional<Pose3d> getBotpose() {
    return Optional.empty();
  }

  @Override
  public Optional<BotPoseEstimate> getBotPoseEstimate() {
    return Optional.empty();
  }

  @Override
  public Optional<Pose3d> getBotposeBlue() {
    return Optional.empty();
  }

  @Override
  public Optional<BotPoseEstimate> getBotPoseEstimateBlue() {
    return Optional.empty();
  }

  @Override
  public Optional<Pose3d> getBotposeRed() {
    return Optional.empty();
  }

  @Override
  public Optional<BotPoseEstimate> getBotPoseEstimateRed() {
    return Optional.empty();
  }

  @Override
  public OptionalDouble getCaptureLatency() {
    return OptionalDouble.empty();
  }

  @Override
  public OptionalDouble getTargetingLatency() {
    return OptionalDouble.empty();
  }

  @Override
  public OptionalDouble getTimestamp() {
    return OptionalDouble.empty();
  }

  @Override
  public OptionalDouble lastMSDelay() {
    return OptionalDouble.empty();
  }

  @Override
  public Set<Integer> getVisibleTags() {
    return Set.of();
  }

  @Override
  public Map<Integer, Pose3d> getVisibleAprilTagPoses() {
    return Collections.emptyMap();
  }
}
