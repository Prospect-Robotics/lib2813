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
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;

public class LimelightTest {
	@Rule
	public final FakeLimelight fakeLimelight = new FakeLimelight();

	@BeforeClass
	public static void enableAllLogs() {
		DataCollection.enableTesting();
	}

	@After
	public void resetLimelights() {
		Limelight.eraseInstances();
	}

	@Test
	public void equality() {
		Limelight a = Limelight.getDefaultLimelight();
		Limelight b = Limelight.getDefaultLimelight();
		assertEquals("Default limelight call returned different values", a, b);
		Limelight c = Limelight.getLimelight(Limelight.DEFAULT_ADDRESS);
		assertEquals(
				"Default limelights not equal to limelights named \"limelight\" (default)",
				a, c);
	}

	@Test
	public void emptyValues() {
		Limelight limelight = createLimelight();
		assertFalse("NetworkTables should be empty", limelight.getCaptureLatency().isPresent());
	}

	@Test
	public void targetTest() throws Exception {
		Limelight limelight = createLimelight();
		assertFalse(limelight.hasTarget());
		JSONObject a = new JSONObject().put("v", 1);
		fakeLimelight.setResultsResponse(new JSONObject().put("Results", a));
		limelight.runThread();
		assertTrue(limelight.hasTarget());
	}

	JSONObject readJSON(String fileName) throws IOException {
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

	@Test
	public void absentTest1() throws Exception {
		JSONObject obj = readJSON("AbsentTest1.json");
		fakeLimelight.setResultsResponse(obj);
		Limelight limelight = createLimelight();
		assertFalse(limelight.getLocationalData().getBotpose().isPresent());
	}

	@Test
	public void absentTest2() throws Exception {
		JSONObject obj = readJSON("AbsentTest2.json");
		fakeLimelight.setResultsResponse(obj);
		Limelight limelight = createLimelight();
		assertFalse(limelight.getLocationalData().getBotpose().isPresent());
	}

	@Test
	public void presentTest1() throws Exception {
		JSONObject obj = readJSON("PresentTest1.json");
		fakeLimelight.setResultsResponse(obj);
		Limelight limelight = createLimelight();
		assertTrue(limelight.hasTarget());
		Optional<Pose3d> actualPose = limelight.getLocationalData().getBotpose();
		assertTrue(actualPose.isPresent());
		Rotation3d rotation = new Rotation3d(Math.toRadians(6.817779398227925), Math.toRadians(-25.663211825857257), Math.toRadians(-173.13543891950323));
		Pose3d expectedPose = new Pose3d(7.3505718968031255, 0.7083545864687876, 0.9059300968047116, rotation);
		assertEquals(expectedPose, actualPose.orElse(null));
	}

	@Test
	public void presentTest2() throws Exception {
		JSONObject obj = readJSON("PresentTest2.json");
		fakeLimelight.setResultsResponse(obj);
		Limelight limelight = createLimelight();
		assertTrue(limelight.hasTarget());
		Optional<Pose3d> actualPose = limelight.getLocationalData().getBotpose();
		assertTrue(actualPose.isPresent());
		Rotation3d rotation = new Rotation3d(Math.toRadians(-5.176760596073282), Math.toRadians(-24.321885146945643), Math.toRadians(-164.63614172918574));
		Pose3d expectedPose = new Pose3d(7.46915459715645, 0.8066093109325925, 1.0062389106931178, rotation);
		assertEquals(expectedPose, actualPose.orElse(null));
	}

	private Limelight createLimelight() {
		Limelight limelight = new Limelight("localhost");
		limelight.runThread();
		return limelight;
	}

	@Test
	public void fakeWorks() throws Exception {
		fakeLimelight.setResultsResponse(new JSONObject().put("v", 1));
		HttpRequest req = HttpRequest.newBuilder(new URI("http://localhost:5807/results")).GET().build();
		HttpClient client = HttpClient.newBuilder().executor(Executors.newSingleThreadExecutor()).build();
		HttpResponse<String> response = client.send(req, BodyHandlers.ofString());
		assertEquals("{\"v\":1}", response.body());
	}
}
