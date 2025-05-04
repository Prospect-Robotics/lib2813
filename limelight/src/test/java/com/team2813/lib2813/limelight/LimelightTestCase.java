package com.team2813.lib2813.limelight;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static com.team2813.lib2813.limelight.truth.Pose2dSubject.assertThat;
import static com.team2813.lib2813.limelight.truth.Pose3dSubject.assertThat;
import static org.junit.Assert.assertEquals;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation3d;
import org.json.JSONObject;
import org.junit.Test;

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

abstract class LimelightTestCase {

	@Test
	public final void emptyValues() {
		Limelight limelight = createLimelight();
		assertWithMessage("JSON should be empty")
				.that(limelight.getCaptureLatency()).isEmpty();
	}

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
		Rotation3d rotation = new Rotation3d(Math.toRadians(6.82), Math.toRadians(-25.66), Math.toRadians(-173.14));
		Pose3d expectedPose = new Pose3d(7.35, 0.708, 0.91, rotation);
		assertThat(actualPose).isWithin(0.005).of(expectedPose);

		assertThat(locationalData.getBotPoseEstimate()).isPresent();
		var poseEstimate = locationalData.getBotPoseEstimate().get();
		assertThat(poseEstimate.timestampSeconds()).isGreaterThan(0.0);
		expectedPose = new Pose3d(7.35, 0.71, 0.0, new Rotation3d(0.0, 0.0, rotation.getZ()));
		assertThat(poseEstimate.pose()).isWithin(0.005).of(expectedPose);

		assertThat(locationalData.getBotPoseEstimateBlue()).isPresent();
		var blueEstimate = locationalData.getBotPoseEstimateBlue().get();
		assertThat(blueEstimate.timestampSeconds()).isGreaterThan(0.0);
		expectedPose = new Pose3d(15.62, 4.52, 0.0, new Rotation3d(0.0, 0.0, rotation.getZ()));
		assertThat(blueEstimate.pose()).isWithin(0.005).of(expectedPose);

		assertThat(locationalData.getBotPoseEstimateRed()).isPresent();
		var redEstimate = locationalData.getBotPoseEstimateRed().get();
		assertThat(redEstimate.timestampSeconds()).isWithin(0.005).of(blueEstimate.timestampSeconds());
		rotation = new Rotation3d(Math.toRadians(6.82), Math.toRadians(-25.66), Math.toRadians(6.86));
		expectedPose = new Pose3d(0.92, 3.10, 0, new Rotation3d(0.0, 0.0, rotation.getZ()));
		assertThat(redEstimate.pose()).isWithin(0.005).of(expectedPose);
	}

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
		Rotation3d rotation = new Rotation3d(Math.toRadians(-5.18), Math.toRadians(-24.32), Math.toRadians(-164.64));
		Pose3d expectedPose = new Pose3d(7.469, 0.81, 1.01, rotation);
		assertThat(actualPose).isWithin(0.005).of(expectedPose);

		assertThat(locationalData.getBotPoseEstimateBlue()).isPresent();
		var blueEstimate = locationalData.getBotPoseEstimateBlue().get();
		assertThat(blueEstimate.timestampSeconds()).isGreaterThan(0.0);
		var expectedYaw = new Rotation2d(-2.87);
		expectedPose = new Pose3d(15.74, 4.81, 0, new Rotation3d(expectedYaw));
		assertThat(blueEstimate.pose()).isWithin(0.005).of(expectedPose);

		assertThat(locationalData.getBotPoseEstimateRed()).isPresent();
		var redEstimate = locationalData.getBotPoseEstimateRed().get();
		expectedYaw = new Rotation2d(0.268);
		expectedPose = new Pose3d(0.80, 3.20, 0, new Rotation3d(expectedYaw));
		assertThat(redEstimate.pose()).isWithin(0.005).of(expectedPose);
		assertThat(blueEstimate.timestampSeconds()).isWithin(0.005).of(redEstimate.timestampSeconds());
	}
	
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

	@Test
	public final void getVisibleTags() throws Exception {
		JSONObject obj = readJSON("BotposeBlueRedTest.json");
		setJson(obj);
		Limelight limelight = createLimelight();
		assertHasTarget(limelight);
		assertThat(limelight.getLocationalData().getVisibleTags()).containsExactly(20);
	}

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
		assertThat(pose).translation()
			.isWithin(0.005)
			.of(new Translation3d(-3.87, 0.72, 0.31));
		assertThat(tagMap).hasSize(1);

		assertThat(locationalData.getBotPoseEstimate()).isPresent();
		assertThat(locationalData.getBotPoseEstimate().get().visibleAprilTags()).isEqualTo(tags);

		assertThat(locationalData.getBotPoseEstimateBlue()).isPresent();
		assertThat(locationalData.getBotPoseEstimateBlue().get().visibleAprilTags()).isEqualTo(tags);

		assertThat(locationalData.getBotPoseEstimateRed()).isPresent();
		assertThat(locationalData.getBotPoseEstimateRed().get().visibleAprilTags()).isEqualTo(tags);
	}

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
		assertThat(pose).translation().isWithin(0.005)
				.of(new Translation3d(-3.87, 0.72, 0.31));
	}

	protected abstract Limelight createLimelight();

	protected abstract void setJson(JSONObject json);

	private void uploadFieldMap(Limelight limelight) throws IOException {
		boolean updateLimelight = false;
		try (var stream = getClass().getResourceAsStream("frc2025r2.fmap")) {
			limelight.setFieldMap(stream, updateLimelight);
		}
	}

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

	private void assertHasTarget(Limelight limelight) {
		assertWithMessage("Should have target").that(limelight.hasTarget()).isTrue();
		assertWithMessage("Should have target").that(limelight.getLocationalData().hasTarget()).isTrue();
	}

	protected static void assertAlmostEqual(double expected, OptionalDouble actual, double delta) {
		actual.ifPresentOrElse(
						d -> assertEquals(expected, d, delta),
						() -> assertEquals(OptionalDouble.of(expected), actual));
	}
}
