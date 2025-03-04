package com.team2813.lib2813.limelight;

import static com.team2813.lib2813.limelight.Optionals.unboxDouble;

import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalDouble;

import com.team2813.lib2813.limelight.LimelightHelpers.LimelightResults;
import edu.wpi.first.math.geometry.Pose3d;
import org.json.JSONObject;

class NetworkTablesLimelight implements Limelight {
  private static final double[] ZEROS = new double[6];
  private final String limelightName;

  NetworkTablesLimelight(String limelightName) {
    this.limelightName = limelightName;
  }
  
  @Override
  public OptionalDouble getTimestamp() {
    return unboxDouble(getResults().map(results -> results.timestamp_LIMELIGHT_publish));
  }

  @Override
  public boolean hasTarget() {
    return getResults().map(results -> results.targets_Fiducials.length > 0).orElse(false);
  }

  @Override
  public LocationalData getLocationalData() {
    Optional<LimelightHelpers.LimelightResults> results = getResults();
    if (results.isEmpty()) {
      return StubLocationalData.INSTANCE;
    }
    return new NTLocationalData(results.get());
  }

  @Override
  public Optional<JSONObject> getJsonDump() {
    return Optional.empty();
  }

  @Override
  public OptionalDouble getCaptureLatency() {
    return unboxDouble(getResults().map(results -> results.latency_capture));
  }

  private Optional<LimelightResults> getResults() {
    LimelightHelpers.LimelightResults results = LimelightHelpers.getLatestResults(limelightName);
    if (results.error == null) {
      return Optional.of(results);
    }
    return Optional.empty();
  }

  private static class StubLocationalData implements LocationalData {
    static final StubLocationalData INSTANCE = new StubLocationalData();

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
    public OptionalDouble lastMSDelay() {
      return OptionalDouble.empty();
    }
  }

  private static class NTLocationalData implements LocationalData {
    private final LimelightResults results;

    NTLocationalData(LimelightHelpers.LimelightResults results) {
      this.results = results;
    }

    @Override
    public Optional<Pose3d> getBotpose() {
      return toPose3D(results.botpose);
    }
    
    @Override
    public Optional<Pose3d> getBotposeBlue() {
      return toPose3D(results.botpose_wpiblue);
    }
    
    @Override
    public Optional<Pose3d> getBotposeRed() {
      return toPose3D(results.botpose_wpired);
    }

    @Override
    public OptionalDouble getCaptureLatency() {
      return OptionalDouble.of(results.latency_capture);
    }

    @Override
    public OptionalDouble getTargetingLatency() {
      return OptionalDouble.of(results.latency_pipeline);
    }

    private static Optional<Pose3d> toPose3D(double[] inData) {
      if (inData.length != 6 || Arrays.equals(ZEROS, inData)) {
        return Optional.empty();
      }
      return Optional.of(LimelightHelpers.toPose3D(inData));
    }
  }
}
