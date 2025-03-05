package com.team2813.lib2813.limelight;

import com.team2813.lib2813.limelight.apriltag_map.Fiducial;
import com.team2813.lib2813.limelight.apriltag_map.FiducialRetriever;
import edu.wpi.first.math.geometry.Pose3d;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

class AprilTagMapPoseHelper {
  private static final int PORT = 5807;
  private final String hostname;
  private FiducialRetriever retriever;
  private static final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(20))
          .executor(Executors.newFixedThreadPool(1)).build();
  
  public AprilTagMapPoseHelper(String hostname) {
    this.hostname = hostname;
  }
  
  public void setFieldMap(InputStream stream, boolean updateLimelight) throws IOException {
    if (updateLimelight) {
      stream.mark(Integer.MAX_VALUE);
    }
    retriever = new FiducialRetriever(stream);
    if (updateLimelight) {
      stream.reset();
      HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.ofInputStream(() -> stream);
      sendRequest("/upload-fieldmap", publisher);
    }
  }

  private void sendRequest(String path, HttpRequest.BodyPublisher publisher) throws IOException {
    try {
      URI uri = new URI("http", null, hostname, PORT, path, null, null);
      client.send(HttpRequest.newBuilder(uri).POST(publisher).build(), HttpResponse.BodyHandlers.discarding());
    } catch (URISyntaxException e) {
      throw new IOException(String.format("Could not create URI for http://%s:%d%s", hostname, PORT, path), e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(String.format("Thread interrupted while trying to send request to http://%s:%d%s", hostname, PORT, path), e);
    }
  }

  public List<Pose3d> getVisibleTagPoses(Set<Integer> ids) {
    if (retriever == null) {
      return List.of();
    }
    return Arrays.stream(retriever.getFidicuals()).filter((fidicual) -> ids.contains(fidicual.getId())).map(Fiducial::getPosition).toList();
  }
}
