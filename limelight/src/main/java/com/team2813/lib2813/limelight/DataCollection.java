package com.team2813.lib2813.limelight;

import static com.ctre.phoenix6.Utils.getCurrentTimeSeconds;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.BodySubscribers;
import java.net.http.HttpResponse.ResponseInfo;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Executors;
import org.json.JSONObject;

/**
 * Background task that collects data from a Limelight camera via HTTP requests.
 *
 * <p>This class periodically fetches JSON data from the Limelight `/results` endpoint, timestamps
 * the response, and stores the latest result for retrieval.
 */
class DataCollection implements Runnable {

  /** Shared HTTP client with short timeout and fixed thread pool. */
  private static final HttpClient client =
      HttpClient.newBuilder()
          .connectTimeout(Duration.ofMillis(20))
          .executor(Executors.newFixedThreadPool(2))
          .build();

  /** HTTP request to retrieve Limelight results. */
  private final HttpRequest dumpRequest;

  /** Stores the most recent result and its response timestamp. */
  private volatile Optional<Result> lastResult;

  /**
   * Constructs a DataCollection instance for a given Limelight client.
   *
   * @param limelightClient client for building HTTP requests
   * @throws RuntimeException if creating the HTTP request fails
   */
  public DataCollection(LimelightClient limelightClient) {
    lastResult = Optional.empty();
    try {
      dumpRequest = limelightClient.newRequestBuilder("/results").GET().build();
    } catch (LimelightClient.HttpRequestException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Represents a single Limelight result.
   *
   * @param json the parsed JSON object returned by Limelight
   * @param responseTimestamp the time the response was received (seconds)
   */
  record Result(JSONObject json, double responseTimestamp) {}

  /** Custom body handler to parse the HTTP response into a {@link Result} with timestamp. */
  private static class JSONHandler implements BodyHandler<Result> {

    @Override
    public BodySubscriber<Result> apply(ResponseInfo responseInfo) {
      // Capture the timestamp before parsing the JSON body.
      double responseTimestamp = getCurrentTimeSeconds();

      // Map the string response body into a Result object
      return BodySubscribers.mapping(
          BodyHandlers.ofString(Charset.defaultCharset()).apply(responseInfo),
          body -> new Result(new JSONObject(body), responseTimestamp));
    }
  }

  /** Single reusable instance of the JSONHandler. */
  private static final JSONHandler handler = new JSONHandler();

  /** Updates the most recent result from an HTTP response. */
  private void updateJSON(HttpResponse<Result> response) {
    Result result = response.body();
    lastResult = Optional.of(result);
  }

  /**
   * Executes the HTTP request to fetch Limelight results and stores the latest Result.
   *
   * <p>If the request is interrupted or fails, {@code lastResult} is cleared.
   */
  @Override
  public void run() {
    try {
      updateJSON(client.send(dumpRequest, handler));
    } catch (InterruptedException e) {
      lastResult = Optional.empty();
      Thread.currentThread().interrupt(); // preserve interrupt status
    } catch (Exception e) {
      lastResult = Optional.empty();
    }
  }

  /**
   * Returns the most recent fetched result.
   *
   * @return an {@link Optional} containing the latest {@link Result}, or empty if unavailable
   */
  public Optional<Result> getMostRecent() {
    return lastResult;
  }
}
