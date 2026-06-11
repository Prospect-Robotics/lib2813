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
package com.team2813.lib2813.vision;

import edu.wpi.first.apriltag.AprilTag;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.geometry.Pose3d;
import java.util.List;
import java.util.Optional;

/** Contains AprilTags for the Reefscape Welded field. */
enum ReefscapeAprilTag {
  RED_REEF_CENTER(7),
  BLUE_REEF_CENTER(18),
  RED_PROCESSOR(3),
  BLUE_PROCESSOR(16);

  /** Creates a (mutable) AprilTag for this enum value. */
  AprilTag toAprilTag() {
    Optional<Pose3d> tagPose = memoizedFieldLayout().getTagPose(tagId);
    return new AprilTag(tagId, tagPose.orElseThrow());
  }

  public int id() {
    return tagId;
  }

  /** Creates a field layout for Reefscape Welded with the origin set to zero. */
  static AprilTagFieldLayout createFieldLayout() {
    return AprilTagFieldLayout.loadField(AprilTagFields.k2025ReefscapeWelded);
  }

  /** Creates a field layout with the dimensions of Reefscape Welded and the given tag. */
  static AprilTagFieldLayout createFieldLayout(ReefscapeAprilTag tag) {
    var fieldLayout = memoizedFieldLayout();
    return new AprilTagFieldLayout(
        List.of(tag.toAprilTag()), fieldLayout.getFieldLength(), fieldLayout.getFieldWidth());
  }

  private static AprilTagFieldLayout possiblyNullFieldLayout;

  private static AprilTagFieldLayout memoizedFieldLayout() {
    if (possiblyNullFieldLayout == null) {
      possiblyNullFieldLayout = createFieldLayout();
    }
    return possiblyNullFieldLayout;
  }

  ReefscapeAprilTag(int tagId) {
    this.tagId = tagId;
  }

  private final int tagId;
}
