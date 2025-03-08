package com.team2813.lib2813.limelight;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;

final class LimelightClient {
  static final int DEFAULT_PORT = 5807;
  private final String hostname;
  private final int port;

  static class HttpRequestException extends IOException {
    public HttpRequestException(String message, URISyntaxException e) {
      super(message, e);
    }
  }

  LimelightClient(String hostname) {
    this(hostname, DEFAULT_PORT);
  }

  LimelightClient(String hostname, int port) {
    this.hostname = hostname;
    this.port = port;
  }

  /**
   * Creates an HttpRequest builder for the limelight with the given path.
   *
   * @throws HttpRequestException If the request could not be created
   */
  HttpRequest.Builder newRequestBuilder(String path) throws HttpRequestException {
    try {
      URI uri = new URI("http", null, hostname, port, path, null, null);
      return HttpRequest.newBuilder(uri);
    } catch (URISyntaxException e) {
      throw new HttpRequestException(String.format("Could not create URI to %s:%d for '%s'", hostname, port, path), e);
    }
  }
}
