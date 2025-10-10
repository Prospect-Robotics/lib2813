package com.team2813.lib2813.limelight;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import org.json.JSONObject;
import org.junit.rules.ExternalResource;

/**
 * Mock HTTP server that simulates a Limelight camera for testing purposes.
 *
 * <p>This class extends JUnit's {@link ExternalResource} to provide automatic setup and teardown of
 * a fake HTTP server that mimics the Limelight REST API. It listens on port 5807 and handles two
 * endpoints:
 *
 * <ul>
 *   <li>{@code /results} - GET endpoint that returns vision data in JSON format
 *   <li>{@code /upload-fieldmap} - POST endpoint that accepts field map uploads
 * </ul>
 *
 * <p>Usage example in a JUnit test:
 *
 * <pre>{@code
 * @Rule
 * public FakeLimelight fakeLimelight = new FakeLimelight();
 *
 * @Test
 * public void testLimelightConnection() {
 *     JSONObject response = new JSONObject();
 *     response.put("v", 1);
 *     fakeLimelight.setResultsResponse(response);
 *     // Test code here
 * }
 * }</pre>
 */
public class FakeLimelight extends ExternalResource {
  /** Logger for tracking fake Limelight activity. */
  private static final Logger logger = Logger.getLogger("FakeLimelight");

  /** The underlying HTTP server instance. */
  HttpServer server;

  /**
   * Sets up the fake Limelight server before each test. Creates an HTTP server on port 5807 with
   * endpoints for results and field map uploads.
   *
   * @throws Throwable if server creation or startup fails
   */
  @Override
  protected void before() throws Throwable {
    server = HttpServer.create(new InetSocketAddress(5807), 0);
    server.createContext("/results", resultsResponse);
    server.createContext("/upload-fieldmap", fieldMapResponse);
    server.setExecutor(Executors.newSingleThreadExecutor());
    server.start();
  }

  /**
   * Tears down the fake Limelight server after each test. Stops the server with a 2-second grace
   * period and clears the server reference.
   */
  @Override
  protected void after() {
    server.stop(2);
    server = null;
  }

  /**
   * HTTP handler that simulates the Limelight's GET /results endpoint.
   *
   * <p>This handler responds to GET requests with a configurable JSON body representing vision data
   * from the Limelight. It returns 405 Method Not Allowed for non-GET requests.
   */
  private static class FakeGet implements HttpHandler {
    /** The JSON response body to return. */
    private String body;

    /**
     * Sets the response body that will be returned by GET requests.
     *
     * @param body the JSON string to return
     */
    public void setBody(String body) {
      this.body = body;
    }

    /**
     * Gets the current response body.
     *
     * @return the JSON string that will be returned
     */
    public String getBody() {
      return body;
    }

    /**
     * Handles incoming HTTP requests to the /results endpoint.
     *
     * @param exchange the HTTP exchange containing request and response
     * @throws IOException if an I/O error occurs during response writing
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      logger.info("Request for results received");
      if (!"GET".equals(exchange.getRequestMethod())) {
        exchange.sendResponseHeaders(405, -1);
        logger.warning(String.format("Unexpected request method: %s", exchange.getRequestMethod()));
        return;
      }
      exchange.sendResponseHeaders(200, body.length());
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(body.getBytes());
      }
    }
  }

  /**
   * HTTP handler that simulates the Limelight's POST /upload-fieldmap endpoint.
   *
   * <p>This handler accepts POST requests with field map data and stores the uploaded content for
   * verification in tests. It returns 405 Method Not Allowed for non-POST requests.
   */
  private static class FakeFieldMap implements HttpHandler {
    /** The most recently uploaded field map content. */
    private String post;

    /**
     * Handles incoming HTTP requests to the /upload-fieldmap endpoint. Reads and stores the POST
     * body content for later retrieval.
     *
     * @param exchange the HTTP exchange containing request and response
     * @throws IOException if an I/O error occurs during request reading
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      logger.info("Request for field map received");
      if (!"POST".equals(exchange.getRequestMethod())) {
        exchange.sendResponseHeaders(405, -1);
        logger.warning(String.format("Unexpected request method: %s", exchange.getRequestMethod()));
        return;
      }
      try (InputStream is = exchange.getRequestBody()) {
        byte[] data = is.readAllBytes();
        post = new String(data, StandardCharsets.UTF_8);
      }
      exchange.sendResponseHeaders(200, -1);
    }
  }

  /** Handler for the /results GET endpoint. */
  private final FakeGet resultsResponse = new FakeGet();

  /** Handler for the /upload-fieldmap POST endpoint. */
  private final FakeFieldMap fieldMapResponse = new FakeFieldMap();

  /**
   * Resets the fake Limelight state by clearing the results response body. Useful for ensuring a
   * clean state between test cases.
   */
  public void reset() {
    resultsResponse.setBody("");
  }

  /**
   * Sets the JSON response that will be returned by the /results endpoint.
   *
   * @param response the JSONObject containing vision data to return
   */
  public void setResultsResponse(JSONObject response) {
    resultsResponse.setBody(response.toString());
  }

  /**
   * Gets the most recently uploaded field map content.
   *
   * @return the field map data as a string, or null if no field map has been uploaded
   */
  public String getFieldMap() {
    return fieldMapResponse.post;
  }

  /**
   * Gets the current results response as a JSONObject.
   *
   * @return a JSONObject parsed from the current response body
   * @throws org.json.JSONException if the response body is not valid JSON
   */
  public JSONObject getResultsResponse() {
    return new JSONObject(resultsResponse.getBody());
  }
}
