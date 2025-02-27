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
import org.junit.ClassRule;
import org.junit.Test;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;

public class RestLimelightTest extends LimelightTestCase {
	@ClassRule
	public static final FakeLimelight fakeLimelight = new FakeLimelight();

	@After
	public void resetLimelights() {
		RestLimelight.eraseInstances();
	}

	@After
	public void resetFakeLimelight() {
		fakeLimelight.reset();
	}

	@Test
	public void equality() {
		Limelight a = RestLimelight.getDefaultLimelight();
		Limelight b = RestLimelight.getDefaultLimelight();
		assertEquals("Default limelight call returned different values", a, b);
		Limelight c = RestLimelight.getLimelight(RestLimelight.DEFAULT_ADDRESS);
		assertEquals(
				"Default limelights not equal to limelights named \"limelight\" (default)",
				a, c);
	}

	@Test
	public void fakeWorks() throws Exception {
		fakeLimelight.setResultsResponse(new JSONObject().put("v", 1));
		HttpRequest req = HttpRequest.newBuilder(new URI("http://localhost:5807/results")).GET().build();
		HttpClient client = HttpClient.newBuilder().executor(Executors.newSingleThreadExecutor()).build();
		HttpResponse<String> response = client.send(req, BodyHandlers.ofString());
		assertEquals("{\"v\":1}", response.body());
	}

	@Override
	protected Limelight createLimelight() {
		RestLimelight limelight = new RestLimelight("localhost");
		limelight.runThread();
		return limelight;
	}

	@Override
	protected void setJson(JSONObject json) {
		// limelight json schema has been updated to not json object "Results" in the root, and we want to test the new version, which does not.
		// The new version just has all json that was in "Results" in the root, so the json object in "Results" will essentially be the new schema.
		// Since we know that all the json objects in the resources folder have the "Results" json object, this should never fail.
		fakeLimelight.setResultsResponse(json.getJSONObject("Results"));
	}
}
