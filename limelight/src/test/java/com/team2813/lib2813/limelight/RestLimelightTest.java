package com.team2813.lib2813.limelight;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import org.json.JSONObject;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * Unit tests for {@link RestLimelight}.
 *
 * <p>This test suite uses a {@link FakeLimelight} HTTP server to simulate REST responses from a
 * physical Limelight device. It validates Limelight instance management, HTTP endpoint behavior,
 * and field map uploads.
 */
public class RestLimelightTest extends LimelightTestCase {

  /**
   * Fake Limelight instance running as an embedded HTTP server.
   *
   * <p>Used to simulate responses from a real Limelight device without requiring hardware.
   */
  @ClassRule public static final FakeLimelight fakeLimelight = new FakeLimelight();

  /** Resets any cached {@link RestLimelight} instances after each test. */
  @After
  public void resetLimelights() {
    RestLimelight.eraseInstances();
  }

  /**
   * Resets the {@link FakeLimelight} after each test, ensuring no test state leaks between test
   * methods.
   */
  @After
  public void resetFakeLimelight() {
    fakeLimelight.reset();
  }

  /**
   * Verifies that calls to obtain the default {@link RestLimelight} return the same instance, and
   * that the default instance is equal to one retrieved by name.
   */
  @Test
  public void equality() {
    Limelight a = RestLimelight.getDefaultLimelight();
    Limelight b = RestLimelight.getDefaultLimelight();
    assertEquals("Default limelight call returned different values", a, b);

    Limelight c = RestLimelight.getLimelight(RestLimelight.DEFAULT_ADDRESS);
    assertEquals("Default limelights not equal to limelights named \"limelight\" (default)", a, c);
  }

  /**
   * Ensures the {@link FakeLimelight} responds correctly to REST requests.
   *
   * @throws Exception if the HTTP client request fails
   */
  @Test
  public void fakeWorks() throws Exception {
    fakeLimelight.setResultsResponse(new JSONObject().put("v", 1));
    HttpRequest req =
        HttpRequest.newBuilder(new URI("http://localhost:5807/results")).GET().build();
    HttpClient client =
        HttpClient.newBuilder().executor(Executors.newSingleThreadExecutor()).build();
    HttpResponse<String> response = client.send(req, BodyHandlers.ofString());
    assertEquals("{\"v\":1}", response.body());
  }

  /**
   * Verifies that uploading a field map to a {@link RestLimelight} updates the {@link
   * FakeLimelight} server as expected.
   *
   * @throws Exception if reading the field map resource or uploading fails
   */
  @Test
  public void setFieldMapWorks() throws Exception {
    Limelight limelight = createLimelight();
    String resourceName = "frc2025r2.fmap";

    // Upload field map from resources
    try (InputStream is = getClass().getResourceAsStream(resourceName)) {
      limelight.setFieldMap(is, true);
    }
    String fieldMap = fakeLimelight.getFieldMap();
    assertNotNull(fieldMap);

    // Verify expected field map content matches
    String expectedFieldMap;
    try (InputStream is = getClass().getResourceAsStream(resourceName)) {
      expectedFieldMap = new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }
    assertEquals(expectedFieldMap, fieldMap);
  }

  /**
   * Creates a new {@link RestLimelight} instance for testing.
   *
   * @return a {@code RestLimelight} bound to "localhost"
   */
  @Override
  protected Limelight createLimelight() {
    RestLimelight limelight = new RestLimelight("localhost");
    limelight.runThread();
    return limelight;
  }

  /**
   * Sets the Limelight JSON results response in the {@link FakeLimelight}.
   *
   * <p>Unlike older schemas, the new Limelight JSON schema does not include a {@code "Results"}
   * object at the root. Instead, all fields are inlined. Since resource test files still contain
   * {@code "Results"}, this method extracts and forwards that sub-object.
   *
   * @param json the full Limelight JSON, expected to contain a {@code "Results"} object
   */
  @Override
  protected void setJson(JSONObject json) {
    fakeLimelight.setResultsResponse(json.getJSONObject("Results"));
  }
}
