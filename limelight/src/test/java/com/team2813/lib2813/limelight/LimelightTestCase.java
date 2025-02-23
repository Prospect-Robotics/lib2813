package com.team2813.lib2813.limelight;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;

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
