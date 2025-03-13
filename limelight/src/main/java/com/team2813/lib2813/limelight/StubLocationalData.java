package com.team2813.lib2813.limelight;

import edu.wpi.first.math.geometry.Pose3d;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;

/** Implementation of LocationalData where all optional values return empty values. */
final class StubLocationalData implements LocationalData {
  static final StubLocationalData INSTANCE = new StubLocationalData();

  @Override
  public boolean hasTarget() {
    return false;
  }

  @Override
  public Optional<Pose3d> getBotpose() {
    return Optional.empty();
  }

  @Override
  public Optional<Pose3d> getBotposeBlue() {
    return Optional.empty();
  }

  @Override
  public Optional<Pose3d> getBotposeRed() {
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
  public double getTimestamp() {
    return 0;
  }

  @Override
  public Set<Integer> getVisibleTags() {
    return Set.of();
  }
}
