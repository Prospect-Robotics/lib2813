package com.team2813.lib2813.limelight;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.Executors;

import org.json.JSONObject;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

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
		Limelight a = Limelight.getLimelight("localhost");
		Limelight b = Limelight.getLimelight("localhost");

		assertEquals(Limelight.DEFAULT_ADDRESS, a.getName());
		assertEquals("Default limelight call returned different values", a, b);
		Limelight c = Limelight.getLimelight(Limelight.DEFAULT_ADDRESS);
		assertEquals(
				"Default limelights not equal to limelights named \"limelight\" (default)",
				a, c);
	}

	@Test
	public void emptyValues() {
		Limelight limelight = new Limelight("localhost");
		assertFalse("NetworkTables should be empty", limelight.getCaptureLatency().isPresent());
	}

	@Test
	public void targetTest() throws Exception {
		Limelight limelight = new Limelight("localhost");
		limelight.runThread();
		assertFalse(limelight.hasTarget());
		fakeLimelight.setResultsResponse(new JSONObject().put("v", 1));
		limelight.runThread();
		assertTrue(limelight.hasTarget());
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
