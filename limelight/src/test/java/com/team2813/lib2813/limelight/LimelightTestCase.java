package com.team2813.lib2813.limelight;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static com.team2813.lib2813.testing.truth.Pose2dSubject.assertThat;
import static com.team2813.lib2813.testing.truth.Pose3dSubject.assertThat;
import static com.team2813.lib2813.testing.truth.Translation3dSubject.assertThat;
import static org.junit.Assert.assertEquals;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation3d;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;
import org.json.JSONObject;
import org.junit.Test;

/**
 * Abstract base class for Limelight implementation testing.
 * 
 * <p>This class provides a comprehensive test suite for validating Limelight implementations
 * against various JSON response scenarios. It tests all aspects of the Limelight interface
 * including pose estimation, target detection, AprilTag visibility, and latency measurements.
 * 
 * <p>Subclasses must implement two abstract methods to adapt the tests to their specific
 * Limelight implementation:
 * <ul>
 *   <li>{@link #createLimelight()} - creates the Limelight instance to test</li>
 *   <li>{@link #setJson(JSONObject)} - configures the test environment with JSON data</li>
 * </ul>
 * 
 * <p>Test coverage includes:
 * <ul>
 *   <li>Empty/missing data scenarios</li>
 *   <li>Invalid data handling</li>
 *   <li>Robot pose estimation in multiple coordinate systems (field, blue alliance, red alliance)</li>
 *   <li>AprilTag detection and position reporting</li>
 *   <li>Latency measurements (capture and targeting)</li>
 *   <li>Field map integration</li>
 * </ul>
 */
abstract class LimelightTestCase {

  /**
   * Tests that an uninitialized Limelight returns empty values.
   * Verifies that capture latency is not present when no data has been received.
   */
  @Test
  public final void emptyValues() {
    Limelight limelight = createLimelight();
    assertWithMessage("JSON should be empty").that(limelight.getCaptureLatency()).isEmpty();
  }

  /**
   * Tests handling of invalid/malformed JSON data.
   * Verifies that the Limelight correctly identifies invalid data and returns empty optionals.
   * 
   * @throws Exception if test resources cannot be loaded
   */
  @Test
  public final void invalidDataTest() throws Exception {
    JSONObject obj = readJSON("InvalidDataTest.json");
    setJson(obj);

    Limelight limelight = createLimelight();

    LocationalData locationalData = limelight.getLocationalData();
    assertThat(locationalData.isValid()).isFalse();
    assertThat(locationalData.getBotpose()).isEmpty();
    OptionalDouble actualCaptureLatency = locationalData.getCaptureLatency();
    assertThat(actualCaptureLatency).isEmpty();
    OptionalDouble actualTargetingLatency = locationalData.getTargetingLatency();
    assertThat(actualTargetingLatency).isEmpty();
  }

  /**
   * Tests scenario where no target is detected but latency data is present.
   * Validates that valid latency measurements can be obtained even without a target.
   * 
   * @throws Exception if test resources cannot be loaded
   */
  @Test
  public final void absentTest1() throws Exception {
    JSONObject obj = readJSON("AbsentTest1.json");
    setJson(obj);

    Limelight limelight = createLimelight();

    LocationalData locationalData = limelight.getLocationalData();
    assertThat(locationalData.isValid()).isTrue();
    assertThat(locationalData.getBotpose()).isEmpty();
    OptionalDouble actualCaptureLatency = locationalData.getCaptureLatency();
    double expectedCaptureLatencyMs = 37.40;
    assertAlmostEqual(expectedCaptureLatencyMs, actualCaptureLatency, 0.005);
    OptionalDouble actualTargetingLatency = locationalData.getTargetingLatency();
    double expectedTargetingLatencyMs = 38.95;
    assertAlmostEqual(expectedTargetingLatencyMs, actualTargetingLatency, 0.005);
    assertThat(locationalData.getBotPoseEstimateBlue()).isEmpty();
    assertThat(locationalData.getBotPoseEstimateRed()).isEmpty();
  }

  /**
   * Tests another scenario where no target is detected but latency data differs.
   * Verifies consistent behavior with different latency values.
   * 
   * @throws Exception if test resources cannot be loaded
   */
  @Test
  public final void absentTest2() throws Exception {
    JSONObject obj = readJSON("AbsentTest2.json");
    setJson(obj);

    Limelight limelight = createLimelight();

    LocationalData locationalData = limelight.getLocationalData();
    assertThat(locationalData.isValid()).isTrue();
    assertThat(locationalData.getBotpose()).isEmpty();
    OptionalDouble actualCaptureLatency = locationalData.getCaptureLatency();
    double expectedCaptureLatencyMs = 37.40;
    assertAlmostEqual(expectedCaptureLatencyMs, actualCaptureLatency, 0.005);
    OptionalDouble actualTargetingLatency = locationalData.getTargetingLatency();
    double expectedTargetingLatencyMs = 54.64;
    assertAlmostEqual(expectedTargetingLatencyMs, actualTargetingLatency, 0.005);
    assertThat(locationalData.getBotPoseEstimateBlue()).isEmpty();
    assertThat(locationalData.getBotPoseEstimateRed()).isEmpty();
  }

  /**
   * Tests scenario with a detected target and full pose estimation data.
   * Validates robot pose in field coordinates, blue alliance coordinates, and red alliance coordinates.
   * Also verifies that pose estimates include valid timestamps.
   * 
   * @throws Exception if test resources cannot be loaded
   */
  @Test
  public final void presentTest1() throws Exception {
    JSONObject obj = readJSON("PresentTest1.json");
    setJson(obj);

    Limelight limelight = createLimelight();

    assertHasTarget(limelight);
    LocationalData locationalData = limelight.getLocationalData();
    assertThat(locationalData.isValid()).isTrue();

    OptionalDouble actualCaptureLatency = locationalData.getCaptureLatency();
    double expectedCaptureLatencyMs = 37.40;
    assertAlmostEqual(expectedCaptureLatencyMs, actualCaptureLatency, 0.005);
    OptionalDouble actualTargetingLatency = locationalData.getTargetingLatency();
    double expectedTargetingLatencyMs = 66.61;
    assertAlmostEqual(expectedTargetingLatencyMs, actualTargetingLatency, 0.005);

    assertThat(locationalData.getBotpose()).isPresent();
    Pose3d actualPose = locationalData.getBotpose().get();
    Rotation3d rotation =
        new Rotation3d(Math.toRadians(6.82), Math.toRadians(-25.66), Math.toRadians(-173.14));
    Pose3d expectedPose = new Pose3d(7.35, 0.708, 0.91, rotation);
    assertThat(actualPose).isWithin(0.005).of(expectedPose);

    assertThat(locationalData.getBotPoseEstimate()).isPresent();
    var poseEstimate = locationalData.getBotPoseEstimate().get();
    assertThat(poseEstimate.timestampSeconds()).isGreaterThan(0.0);
    assertThat(poseEstimate.pose()).isWithin(0.005).of(expectedPose.toPose2d());

    assertThat(locationalData.getBotPoseEstimateBlue()).isPresent();
    var blueEstimate = locationalData.getBotPoseEstimateBlue().get();
    assertThat(blueEstimate.timestampSeconds()).isGreaterThan(0.0);
    var expectedPoseEstimate = new Pose2d(15.62, 4.52, rotation.toRotation2d());
    assertThat(blueEstimate.pose()).isWithin(0.005).of(expectedPoseEstimate);

    assertThat(locationalData.getBotPoseEstimateRed()).isPresent();
    var redEstimate = locationalData.getBotPoseEstimateRed().get();
    assertThat(redEstimate.timestampSeconds()).isWithin(0.005).of(blueEstimate.timestampSeconds());
    expectedPoseEstimate = new Pose2d(0.92, 3.10, new Rotation2d(Math.toRadians(6.86)));
    assertThat(redEstimate.pose()).isWithin(0.005).of(expectedPoseEstimate);
  }

  /**
   * Tests another scenario with a detected target and different pose values.
   * Ensures consistent pose estimation behavior across different robot positions.
   * 
   * @throws Exception if test resources cannot be loaded
   */
  @Test
  public final void presentTest2() throws Exception {
    JSONObject obj = readJSON("PresentTest2.json");
    setJson(obj);

    Limelight limelight = createLimelight();

    assertHasTarget(limelight);
    LocationalData locationalData = limelight.getLocationalData();
    assertThat(locationalData.isValid()).isTrue();

    OptionalDouble actualCaptureLatency = locationalData.getCaptureLatency();
    double expectedCaptureLatencyMs = 37.40;
    assertAlmostEqual(expectedCaptureLatencyMs, actualCaptureLatency, 0.005);
    OptionalDouble actualTargetingLatency = locationalData.getTargetingLatency();
    double expectedTargetingLatencyMs = 59.20;
    assertAlmostEqual(expectedTargetingLatencyMs, actualTargetingLatency, 0.005);

    assertThat(locationalData.getBotpose()).isPresent();
    Pose3d actualPose = locationalData.getBotpose().get();
    Rotation3d rotation =
        new Rotation3d(Math.toRadians(-5.18), Math.toRadians(-24.32), Math.toRadians(-164.64));
    Pose3d expectedPose = new Pose3d(7.469, 0.81, 1.01, rotation);
    assertThat(actualPose).isWithin(0.005).of(expectedPose);

    assertThat(locationalData.getBotPoseEstimateBlue()).isPresent();
    var blueEstimate = locationalData.getBotPoseEstimateBlue().get();
    assertThat(blueEstimate.timestampSeconds()).isGreaterThan(0.0);
    var expectedPoseEstimate = new Pose2d(15.74, 4.81, rotation.toRotation2d());
    assertThat(blueEstimate.pose()).isWithin(0.005).of(expectedPoseEstimate);

    assertThat(locationalData.getBotPoseEstimateRed()).isPresent();
    var redEstimate = locationalData.getBotPoseEstimateRed().get();
    expectedPoseEstimate = new Pose2d(0.80, 3.20, new Rotation2d(Math.toRadians(15.36)));
    assertThat(redEstimate.pose()).isWithin(0.005).of(expectedPoseEstimate);
    assertThat(blueEstimate.timestampSeconds()).isWithin(0.005).of(redEstimate.timestampSeconds());
  }

  /**
   * Tests retrieval of robot pose in blue alliance coordinates.
   * Validates the coordinate transformation from field coordinates to blue alliance origin.
   * 
   * @throws Exception if test resources cannot be loaded
   */
  @Test
  public final void getBotposeBlue() throws Exception {
    JSONObject obj = readJSON("BotposeBlueRedTest.json");
    setJson(obj);
    Limelight limelight = createLimelight();
    assertHasTarget(limelight);

    LocationalData locationalData = limelight.getLocationalData();
    Optional<Pose3d> botposeBlue = locationalData.getBotposeBlue();
    assertThat(botposeBlue).isPresent();
    Pose3d actualPose = botposeBlue.get();
    Rotation3d expectedRotation = new Rotation3d(0, 0, Math.toRadians(-123.49));
    Pose3d expectedPose = new Pose3d(4.72, 5.20, 0, expectedRotation);
    assertThat(actualPose).isWithin(0.005).of(expectedPose);
  }

  /**
   * Tests retrieval of robot pose in red alliance coordinates.
   * Validates the coordinate transformation from field coordinates to red alliance origin.
   * 
   * @throws Exception if test resources cannot be loaded
   */
  @Test
  public final void getBotposeRed() throws Exception {
    JSONObject obj = readJSON("BotposeBlueRedTest.json");
    setJson(obj);
    Limelight limelight = createLimelight();
    assertHasTarget(limelight);

    LocationalData locationalData = limelight.getLocationalData();
    Optional<Pose3d> botposeBlue = locationalData.getBotposeRed();
    assertThat(botposeBlue).isPresent();
    Pose3d actualPose = botposeBlue.get();

    Rotation3d expectedRotation = new Rotation3d(0, 0, Math.toRadians(56.51));
    Pose3d expectedPose = new Pose3d(11.83, 3.01, 0, expectedRotation);
    assertThat(actualPose).isWithin(0.005).of(expectedPose);
  }

  /**
   * Tests detection and reporting of visible AprilTag IDs.
   * Verifies that the Limelight correctly identifies which tags are in view.
   * 
   * @throws Exception if test resources cannot be loaded
   */
  @Test
  public final void getVisibleTags() throws Exception {
    JSONObject obj = readJSON("BotposeBlueRedTest.json");
    setJson(obj);
    Limelight limelight = createLimelight();
    assertHasTarget(limelight);
    assertThat(limelight.getLocationalData().getVisibleTags()).containsExactly(20);
  }

  /**
   * Tests retrieval of visible AprilTag positions on the field.
   * Validates that tag positions are correctly loaded from the field map and
   * that pose estimates include the set of visible tags.
   * 
   * @throws Exception if test resources cannot be loaded
   */
  @Test
  public final void getVisibleAprilTagPoses() throws Exception {
    JSONObject obj = readJSON("BotposeBlueRedTest.json");
    setJson(obj);
    Limelight limelight = createLimelight();
    assertHasTarget(limelight);
    uploadFieldMap(limelight);

    LocationalData locationalData = limelight.getLocationalData();
    Map<Integer, Pose3d> tagMap = locationalData.getVisibleAprilTagPoses();
    assertThat(tagMap).containsKey(20);
    Set<Integer> tags = tagMap.keySet();
    Pose3d pose = tagMap.get(20);
    assertThat(pose.getTranslation()).isWithin(0.005).of(new Translation3d(-3.87, 0.72, 0.31));
    assertThat(tagMap).hasSize(1);

    assertThat(locationalData.getBotPoseEstimate()).isPresent();
    assertThat(locationalData.getBotPoseEstimate().get().visibleAprilTags()).isEqualTo(tags);

    assertThat(locationalData.getBotPoseEstimateBlue()).isPresent();
    assertThat(locationalData.getBotPoseEstimateBlue().get().visibleAprilTags()).isEqualTo(tags);

    assertThat(locationalData.getBotPoseEstimateRed()).isPresent();
    assertThat(locationalData.getBotPoseEstimateRed().get().visibleAprilTags()).isEqualTo(tags);
  }

  /**
   * Tests retrieval of AprilTag locations via the getLocatedAprilTags method.
   * Validates that tag positions can be queried by their IDs after field map upload.
   * 
   * @throws Exception if test resources cannot be loaded
   */
  @Test
  public final void visibleTagLocation() throws Exception {
    JSONObject obj = readJSON("BotposeBlueRedTest.json");
    setJson(obj);
    Limelight limelight = createLimelight();
    uploadFieldMap(limelight);

    Set<Integer> visibleTags = limelight.getLocationalData().getVisibleTags();
    List<Pose3d> aprilTags = limelight.getLocatedAprilTags(visibleTags);
    assertThat(aprilTags).hasSize(1);
    Pose3d pose = aprilTags.get(0);
    assertThat(pose.getTranslation()).isWithin(0.005).of(new Translation3d(-3.87, 0.72, 0.31));
  }

  /**
   * Creates a Limelight instance for testing.
   * Subclasses must implement this to provide their specific Limelight implementation.
   * 
   * @return a Limelight instance to test
   */
  protected abstract Limelight createLimelight();

  /**
   * Configures the test environment with JSON data.
   * Subclasses must implement this to inject test data into their Limelight implementation.
   * 
   * @param json the JSONObject containing Limelight response data
   */
  protected abstract void setJson(JSONObject json);

  /**
   * Uploads the FRC 2025 Round 2 field map to the Limelight.
   * This configures AprilTag positions for testing.
   * 
   * @param limelight the Limelight instance to configure
   * @throws IOException if the field map resource cannot be loaded
   */
  private void uploadFieldMap(Limelight limelight) throws IOException {
    boolean updateLimelight = false;
    try (var stream = getClass().getResourceAsStream("frc2025r2.fmap")) {
      limelight.setFieldMap(stream, updateLimelight);
    }
  }

  /**
   * Reads a JSON test file from the classpath resources.
   * 
   * @param fileName the name of the JSON file to read
   * @return a JSONObject parsed from the file contents
   * @throws IOException if the file cannot be found or read
   */
  private JSONObject readJSON(String fileName) throws IOException {
    try (InputStream is = getClass().getResourceAsStream(fileName)) {
      if (is == null) {
        throw new FileNotFoundException(fileName);
      }
      try (InputStreamReader isr = new InputStreamReader(is);
          BufferedReader reader = new BufferedReader(isr)) {
        return new JSONObject(reader.lines().collect(Collectors.joining(System.lineSeparator())));
      }
    }
  }

  /**
   * Asserts that the Limelight has a valid target detected.
   * Checks both the top-level hasTarget() method and the LocationalData hasTarget() method.
   * 
   * @param limelight the Limelight to check
   */
  private void assertHasTarget(Limelight limelight) {
    assertWithMessage("Should have target").that(limelight.hasTarget()).isTrue();
    assertWithMessage("Should have target")
        .that(limelight.getLocationalData().hasTarget())
        .isTrue();
  }

  /**
   * Asserts that an OptionalDouble value is approximately equal to an expected value.
   * If the optional is empty, the assertion fails.
   * 
   * @param expected the expected double value
   * @param actual the OptionalDouble to check
   * @param delta the maximum acceptable difference between expected and actual
   */
  protected static void assertAlmostEqual(double expected, OptionalDouble actual, double delta) {
    actual.ifPresentOrElse(
        d -> assertEquals(expected, d, delta),
        () -> assertEquals(OptionalDouble.of(expected), actual));
  }
}