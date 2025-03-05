package com.team2813.lib2813.limelight;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import org.json.JSONObject;
import org.junit.Test;

import java.io.*;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

abstract class LimelightTestCase {

	@Test
	public final void emptyValues() {
		Limelight limelight = createLimelight();
		assertFalse("JSON should be empty", limelight.getCaptureLatency().isPresent());
	}

	@Test
	public final void absentTest1() throws Exception {
		JSONObject obj = readJSON("AbsentTest1.json");
		setJson(obj);
		Limelight limelight = createLimelight();
		LocationalData locationalData = limelight.getLocationalData();
		assertFalse(locationalData.getBotpose().isPresent());
		OptionalDouble actualCaptureLatency = locationalData.getCaptureLatency();
		assertAlmostEqual(37.40, actualCaptureLatency, 0.005);
		OptionalDouble actualTargetingLatency = locationalData.getTargetingLatency();
		assertAlmostEqual(38.95, actualTargetingLatency, 0.005);
	}

	@Test
	public final void absentTest2() throws Exception {
		JSONObject obj = readJSON("AbsentTest2.json");
		setJson(obj);
		Limelight limelight = createLimelight();
		LocationalData locationalData = limelight.getLocationalData();
		assertFalse(locationalData.getBotpose().isPresent());
		OptionalDouble actualCaptureLatency = locationalData.getCaptureLatency();
		assertAlmostEqual(37.40, actualCaptureLatency, 0.005);
		OptionalDouble actualTargetingLatency = locationalData.getTargetingLatency();
		assertAlmostEqual(54.64, actualTargetingLatency, 0.005);
	}

	@Test
	public final void presentTest1() throws Exception {
		JSONObject obj = readJSON("PresentTest1.json");
		setJson(obj);
		Limelight limelight = createLimelight();
		assertTrue(limelight.hasTarget());
		LocationalData locationalData = limelight.getLocationalData();
		Optional<Pose3d> actualPose = locationalData.getBotpose();
		assertTrue(actualPose.isPresent());
		OptionalDouble actualCaptureLatency = locationalData.getCaptureLatency();
		assertAlmostEqual(37.40, actualCaptureLatency, 0.005);
		OptionalDouble actualTargetingLatency = locationalData.getTargetingLatency();
		assertAlmostEqual(66.61, actualTargetingLatency, 0.005);
		Rotation3d rotation = new Rotation3d(Math.toRadians(6.817779398227925), Math.toRadians(-25.663211825857257), Math.toRadians(-173.13543891950323));
		Pose3d expectedPose = new Pose3d(7.3505718968031255, 0.7083545864687876, 0.9059300968047116, rotation);
		assertEquals(expectedPose, actualPose.orElse(null));
		assertAlmostEqual(3664865.25, limelight.getTimestamp(), 0.005);
	}

	@Test
	public final void presentTest2() throws Exception {
		JSONObject obj = readJSON("PresentTest2.json");
		setJson(obj);
		Limelight limelight = createLimelight();
		assertTrue(limelight.hasTarget());
		LocationalData locationalData = limelight.getLocationalData();
		Optional<Pose3d> actualPose = locationalData.getBotpose();
		assertTrue(actualPose.isPresent());
		OptionalDouble actualCaptureLatency = locationalData.getCaptureLatency();
		assertAlmostEqual(37.40, actualCaptureLatency, 0.005);
		OptionalDouble actualTargetingLatency = locationalData.getTargetingLatency();
		assertAlmostEqual(59.20, actualTargetingLatency, 0.005);
		Rotation3d rotation = new Rotation3d(Math.toRadians(-5.176760596073282), Math.toRadians(-24.321885146945643), Math.toRadians(-164.63614172918574));
		Pose3d expectedPose = new Pose3d(7.46915459715645, 0.8066093109325925, 1.0062389106931178, rotation);
		assertEquals(expectedPose, actualPose.orElse(null));
		assertTrue(limelight.getTimestamp().isPresent());
		assertEquals(941200.41, limelight.getTimestamp().getAsDouble(), 0.005);
	}
	
	@Test
	public final void botposeBlueTest() throws Exception {
		JSONObject obj = readJSON("BotposeBlueRedTest.json");
		setJson(obj);
		Limelight limelight = createLimelight();
    assertTrue(limelight.hasTarget());
		
		LocationalData locationalData = limelight.getLocationalData();
		Optional<Pose3d> botposeBlue = locationalData.getBotposeBlue();
		assertTrue(botposeBlue.isPresent());
		Pose3d actualPose = botposeBlue.get();
		
		Rotation3d expectedRotation = new Rotation3d(0, 0, Math.toRadians(-123.48705171149771));
		Pose3d expectedPose = new Pose3d(4.715193569870748, 5.203922172240444, 0, expectedRotation);
		double poseDiff = actualPose.getTranslation().getDistance(expectedPose.getTranslation());
		assertAlmostEqual(0, OptionalDouble.of(poseDiff), 0.05);
		Rotation3d rotationDiff = expectedRotation.minus(actualPose.getRotation());
		double angleDiff = rotationDiff.getAngle();
		assertAlmostEqual(0, OptionalDouble.of(angleDiff), Math.PI / 12.0);
	}
	
	@Test
	public final void botposeRedTest() throws Exception {
		JSONObject obj = readJSON("BotposeBlueRedTest.json");
		setJson(obj);
		Limelight limelight = createLimelight();
		assertTrue(limelight.hasTarget());
		
		LocationalData locationalData = limelight.getLocationalData();
		Optional<Pose3d> botposeBlue = locationalData.getBotposeRed();
		assertTrue(botposeBlue.isPresent());
		Pose3d actualPose = botposeBlue.get();
		
		Rotation3d expectedRotation = new Rotation3d(0, 0, Math.toRadians(56.51279624901089));
		Pose3d expectedPose = new Pose3d(11.825855015367468, 3.0070683933666404, 0, expectedRotation);
		double poseDiff = actualPose.getTranslation().getDistance(expectedPose.getTranslation());
		assertAlmostEqual(0, OptionalDouble.of(poseDiff), 0.05);
		Rotation3d rotationDiff = expectedRotation.minus(actualPose.getRotation());
		double angleDiff = rotationDiff.getAngle();
		assertAlmostEqual(0, OptionalDouble.of(angleDiff), Math.PI / 12.0);
	}
	
	@Test
	public final void visibleTagTest() throws Exception {
		JSONObject obj = readJSON("BotposeBlueRedTest.json");
		setJson(obj);
		Limelight limelight = createLimelight();
		assertTrue(limelight.hasTarget());
		
		Set<Integer> tags = limelight.getVisibleTags();
		assertEquals(Set.of(20), tags);
	}
	
	@Test
	public final void visibleTagLocationTest() throws Exception {
		JSONObject obj = readJSON("BotposeBlueRedTest.json");
		setJson(obj);
		Limelight limelight = createLimelight();
		
		List<Pose3d> apriltags = limelight.getLocatedAprilTags();
		assertEquals(1, apriltags.size());
	}

	protected abstract Limelight createLimelight();

	protected abstract void setJson(JSONObject json);

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

	protected static void assertAlmostEqual(double expected, OptionalDouble actual, double delta) {
		actual.ifPresentOrElse(
						d -> assertEquals(expected, d, delta),
						() -> assertEquals(OptionalDouble.of(expected), actual));
	}
}
