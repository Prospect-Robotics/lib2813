package com.team2813.lib2813.limelight;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static com.team2813.lib2813.limelight.Pose3dSubject.pose3ds;
import static org.junit.Assert.assertEquals;

import edu.wpi.first.math.geometry.Pose3d;
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

import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static com.team2813.lib2813.limelight.Pose2dSubject.pose2ds;
import static com.team2813.lib2813.limelight.Pose3dSubject.pose3ds;

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
		Optional<Pose3d> actualPose = locationalData.getBotpose();
		assertThat(actualPose).isPresent();
		Rotation3d rotation = new Rotation3d(Math.toRadians(6.82), Math.toRadians(-25.66), Math.toRadians(-173.14));
		Pose3d expectedPose = new Pose3d(7.35, 0.708, 0.91, rotation);
		assertAbout(pose3ds()).that(actualPose.get()).isWithin( 0.005).of(expectedPose);

		assertThat(locationalData.getBotPoseEstimate()).isPresent();
		var poseEstimate = locationalData.getBotPoseEstimate().get();
		assertThat(poseEstimate.timestampSeconds()).isGreaterThan(0.0);
    	expectedPose = new Pose3d(7.35, 0.71, 0.0, new Rotation3d(0.0, 0.0, rotation.getZ()));
		assertAbout(pose2ds()).that(poseEstimate.pose()).isWithin( 0.005).of(expectedPose);

		assertThat(locationalData.getBotPoseEstimateBlue()).isPresent();
		var blueEstimate = locationalData.getBotPoseEstimateBlue().get();
		assertThat(blueEstimate.timestampSeconds()).isGreaterThan(0.0);
		expectedPose = new Pose3d(15.62, 4.52, 0.0, new Rotation3d(0.0, 0.0, rotation.getZ()));
		assertAbout(pose2ds()).that(blueEstimate.pose()).isWithin( 0.005).of(expectedPose);

		assertThat(locationalData.getBotPoseEstimateRed()).isPresent();
		var redEstimate = locationalData.getBotPoseEstimateRed().get();
		assertThat(redEstimate.timestampSeconds()).isWithin(0.005).of(blueEstimate.timestampSeconds());
		rotation = new Rotation3d(Math.toRadians(6.82), Math.toRadians(-25.66), Math.toRadians(6.86));
		expectedPose = new Pose3d(0.92, 3.10, 0, new Rotation3d(0.0, 0.0, rotation.getZ()));
		assertAbout(pose2ds()).that(redEstimate.pose()).isWithin( 0.005).of(expectedPose);
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

		Rotation3d rotation = new Rotation3d(Math.toRadians(-5.176760596073282), Math.toRadians(-24.321885146945643), Math.toRadians(-164.63614172918574));
		Pose3d expectedPose = new Pose3d(7.46915459715645, 0.8066093109325925, 1.0062389106931178, rotation);
		assertThat(locationalData.getBotpose()).hasValue(expectedPose);

		var poseEstimate = locationalData.getBotPoseEstimate();
		assertThat(poseEstimate).isPresent();

		var blueEstimate = locationalData.getBotPoseEstimateBlue();
		assertThat(blueEstimate).isPresent();
		assertThat(blueEstimate.get().timestampSeconds()).isGreaterThan(0.0);
		expectedPose = new Pose3d(15.74002959715645, 4.8134593109325925, 1.00623891069311787116, rotation);
		assertEquals(blueEstimate.get().pose(), expectedPose.toPose2d());

		var redEstimate = locationalData.getBotPoseEstimateRed();
		assertThat(redEstimate).isPresent();
		rotation = new Rotation3d(Math.toRadians(-5.176760596073282), Math.toRadians(-24.321885146945643), Math.toRadians(15.363706231322912));
		expectedPose = new Pose3d(0.8017182624333765, 3.200260509139247, 1.0062389106931178, rotation);
		assertEquals(redEstimate.get().pose(), expectedPose.toPose2d());
		assertEquals(blueEstimate.get().timestampSeconds(), redEstimate.get().timestampSeconds(), 0.005);
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
		assertAbout(pose3ds()).that(actualPose).isWithin( 0.005).of(expectedPose);
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
		assertAbout(pose3ds()).that(actualPose).isWithin( 0.005).of(expectedPose);
	}

	@Test
	public final void getVisibleTags() throws Exception {
		JSONObject obj = readJSON("BotposeBlueRedTest.json");
		setJson(obj);
		Limelight limelight = createLimelight();
		assertHasTarget(limelight);
		Set<Integer> tags = limelight.getLocationalData().getVisibleTags();
		assertEquals(Set.of(20), tags);
	}

	@Test
	public final void getVisibleAprilTagPoses() throws Exception {
		JSONObject obj = readJSON("BotposeBlueRedTest.json");
		setJson(obj);
		Limelight limelight = createLimelight();
		assertHasTarget(limelight);
		uploadFieldMap(limelight);

		Map<Integer, Pose3d> tagMap = limelight.getLocationalData().getVisibleAprilTagPoses();
		assertEquals(Set.of(20), tagMap.keySet());
		Pose3d pose = tagMap.get(20);
		assertAbout(pose3ds())
			.that(pose).translation()
			.isWithin( 0.005)
			.of(new Translation3d(-3.87, 0.72, 0.31));
	}

	@Test
	public final void visibleTagLocation() throws Exception {
		JSONObject obj = readJSON("BotposeBlueRedTest.json");
		setJson(obj);
		Limelight limelight = createLimelight();
		uploadFieldMap(limelight);

		Set<Integer> visibleTags = limelight.getLocationalData().getVisibleTags();
		List<Pose3d> aprilTags = limelight.getLocatedAprilTags(visibleTags);
		assertEquals(1, aprilTags.size());
		Pose3d pose = aprilTags.get(0);
		assertAbout(pose3ds())
				.that(pose).translation()
				.isWithin( 0.005)
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
