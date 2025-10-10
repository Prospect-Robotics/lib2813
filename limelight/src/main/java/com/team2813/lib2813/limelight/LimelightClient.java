package com.team2813.lib2813.limelight;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;

/**
 * A client for communicating with a Limelight camera over HTTP.
 *
 * <p>Provides functionality for building HTTP requests to the Limelight's REST API.
 */
final class LimelightClient {

  /** The default HTTP port used by Limelight cameras. */
  static final int DEFAULT_PORT = 5807;

  /** The hostname or IP address of the Limelight. */
  private final String hostname;

  /** The port number to use for HTTP connections. */
  private final int port;

  /**
   * Exception thrown when an HTTP request could not be constructed.
   *
   * <p>Wraps a {@link URISyntaxException} to provide additional context about the failure.
   */
  static class HttpRequestException extends IOException {
    public HttpRequestException(String message, URISyntaxException e) {
      super(message, e);
    }
  }

  /**
   * Constructs a LimelightClient using the default port.
   *
   * @param hostname the hostname or IP address of the Limelight
   */
  LimelightClient(String hostname) {
    this(hostname, DEFAULT_PORT);
  }

  /**
   * Constructs a LimelightClient with a specific port.
   *
   * @param hostname the hostname or IP address of the Limelight
   * @param port the HTTP port to use
   */
  LimelightClient(String hostname, int port) {
    this.hostname = hostname;
    this.port = port;
  }

  /**
   * Creates an {@link HttpRequest.Builder} for the Limelight with the given path.
   *
   * <p>The URI is automatically constructed using the hostname and port configured for this client.
   *
   * @param path the HTTP path (e.g., "/results" or "/upload-fieldmap")
   * @return an {@link HttpRequest.Builder} ready for further configuration
   * @throws HttpRequestException if the URI is invalid or cannot be created
   */
  HttpRequest.Builder newRequestBuilder(String path) throws HttpRequestException {
    try {
      URI uri = new URI("http", null, hostname, port, path, null, null);
      return HttpRequest.newBuilder(uri);
    } catch (URISyntaxException e) {
      throw new HttpRequestException(
          String.format("Could not create URI to %s:%d for '%s'", hostname, port, path), e);
    }
  }
}
