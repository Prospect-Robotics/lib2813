package com.team2813.lib2813.limelight;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.units.measure.Time;

import java.util.Optional;
import java.util.OptionalDouble;

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
  public Optional<Time> getTotalLatency() {
    return Optional.empty();
  }
}
