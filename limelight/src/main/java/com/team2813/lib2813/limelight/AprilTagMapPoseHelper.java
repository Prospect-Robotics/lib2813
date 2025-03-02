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
  private final String hostname;
  private FiducialRetriever retriever;
  private static final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(20))
          .executor(Executors.newFixedThreadPool(1)).build();
  
  public AprilTagMapPoseHelper(String hostname) {
    this.hostname = hostname;
  }
  
  public void setFieldMap(InputStream stream, boolean updateLimelight) {
    if (updateLimelight) {
      stream.mark(Integer.MAX_VALUE);
    }
    retriever = new FiducialRetriever(stream);
    if (updateLimelight) {
      URI uri;
      try {
        uri = new URI("http", null, hostname, 5807, "/upload-fieldmap", null, null);
      } catch (URISyntaxException e) {
        throw new RuntimeException(e);
      }
      HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.ofInputStream(() -> {
        try {
          stream.reset();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        return stream;
      });
      try {
        client.send(HttpRequest.newBuilder(uri).POST(publisher).build(), HttpResponse.BodyHandlers.discarding());
      } catch (IOException | InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }
  public List<Pose3d> getVisibleTagPoses(Set<Integer> ids) {
    if (retriever == null) {
      return List.of();
    }
    return Arrays.stream(retriever.getFidicuals()).filter((fidicual) -> ids.contains(fidicual.getId())).map(Fiducial::getPosition).toList();
  }
}
