package com.team2813.lib2813.limelight;

import edu.wpi.first.math.geometry.Pose3d;
import java.util.*;

/**
 * Stub implementation of LocationalData where all optional values return empty.
 * 
 * <p>This class provides a null-object pattern implementation for LocationalData,
 * useful as a fallback when real Limelight data is unavailable. It can represent
 * either valid or invalid data states, but always returns empty optionals for all
 * pose and timing information.
 * 
 * <p>Two singleton instances are provided:
 * <ul>
 *   <li>{@link #VALID} - represents valid but empty data</li>
 *   <li>{@link #INVALID} - represents invalid/disconnected state</li>
 * </ul>
 */
final class StubLocationalData implements LocationalData {
  /** Singleton instance representing valid but empty locational data. */
  static final StubLocationalData VALID = new StubLocationalData(true);
  
  /** Singleton instance representing invalid locational data. */
  static final StubLocationalData INVALID = new StubLocationalData(false);

  /** Whether this stub data should be considered valid. */
  private final boolean valid;

  /**
   * Creates a new StubLocationalData instance.
   * 
   * @param valid whether this stub data should report as valid
   */
  private StubLocationalData(boolean valid) {
    this.valid = valid;
  }

  /**
   * {@inheritDoc}
   * 
   * @return the validity state set during construction
   */
  @Override
  public boolean isValid() {
    return valid;
  }

  /**
   * {@inheritDoc}
   * 
   * @return always false, as stub data never has targets
   */
  @Override
  public boolean hasTarget() {
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @return always empty
   */
  @Override
  public Optional<Pose3d> getBotpose() {
    return Optional.empty();
  }

  /**
   * {@inheritDoc}
   * 
   * @return always empty
   */
  @Override
  public Optional<BotPoseEstimate> getBotPoseEstimate() {
    return Optional.empty();
  }

  /**
   * {@inheritDoc}
   * 
   * @return always empty
   */
  @Override
  public Optional<Pose3d> getBotposeBlue() {
    return Optional.empty();
  }

  /**
   * {@inheritDoc}
   * 
   * @return always empty
   */
  @Override
  public Optional<BotPoseEstimate> getBotPoseEstimateBlue() {
    return Optional.empty();
  }

  /**
   * {@inheritDoc}
   * 
   * @return always empty
   */
  @Override
  public Optional<Pose3d> getBotposeRed() {
    return Optional.empty();
  }

  /**
   * {@inheritDoc}
   * 
   * @return always empty
   */
  @Override
  public Optional<BotPoseEstimate> getBotPoseEstimateRed() {
    return Optional.empty();
  }

  /**
   * {@inheritDoc}
   * 
   * @return always empty
   */
  @Override
  public OptionalDouble getCaptureLatency() {
    return OptionalDouble.empty();
  }

  /**
   * {@inheritDoc}
   * 
   * @return always empty
   */
  @Override
  public OptionalDouble getTargetingLatency() {
    return OptionalDouble.empty();
  }

  /**
   * {@inheritDoc}
   * 
   * @return always empty
   */
  @Override
  public OptionalDouble getTimestamp() {
    return OptionalDouble.empty();
  }

  /**
   * {@inheritDoc}
   * 
   * @return always empty
   */
  @Override
  public OptionalDouble lastMSDelay() {
    return OptionalDouble.empty();
  }

  /**
   * {@inheritDoc}
   * 
   * @return always an empty, immutable set
   */
  @Override
  public Set<Integer> getVisibleTags() {
    return Set.of();
  }

  /**
   * {@inheritDoc}
   * 
   * @return always an empty, immutable map
   */
  @Override
  public Map<Integer, Pose3d> getVisibleAprilTagPoses() {
    return Collections.emptyMap();
  }
}