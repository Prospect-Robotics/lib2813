package com.team2813.lib2813.limelight;

import com.team2813.lib2813.limelight.apriltag_map.Fiducial;
import com.team2813.lib2813.limelight.apriltag_map.FiducialRetriever;
import edu.wpi.first.math.geometry.Pose3d;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 * Helper class for working with AprilTag field maps and retrieving poses of tags.
 *
 * <p>This class interacts with a {@link LimelightClient} to optionally upload field maps to a
 * Limelight device, and locally stores a {@link FiducialRetriever} for pose lookups.
 */
class AprilTagMapPoseHelper {

  /** The Limelight client used to send HTTP requests to the camera. */
  private final LimelightClient limelightClient;

  /** The local fiducial retriever, initialized when a field map is set. */
  private FiducialRetriever retriever;

  /** Shared HTTP client used for uploading field maps to the Limelight. */
  private static final HttpClient client =
      HttpClient.newBuilder()
          .connectTimeout(Duration.ofMillis(20))
          .executor(Executors.newFixedThreadPool(1))
          .build();

  /**
   * Constructs a new helper using the specified Limelight client.
   *
   * @param client the Limelight client to use for HTTP interactions
   */
  public AprilTagMapPoseHelper(LimelightClient client) {
    this.limelightClient = client;
  }

  /**
   * Sets the field map for tag lookups and optionally uploads it to the Limelight.
   *
   * @param stream an {@link InputStream} containing the JSON-encoded field map
   * @param updateLimelight if true, also uploads the field map to the connected Limelight
   * @throws IOException if reading the input stream fails
   */
  public void setFieldMap(InputStream stream, boolean updateLimelight) throws IOException {
    if (!updateLimelight) {
      retriever = new FiducialRetriever(stream);
    } else {
      byte[] bytes = stream.readAllBytes();
      retriever = new FiducialRetriever(new ByteArrayInputStream(bytes));
      HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.ofByteArray(bytes);

      HttpRequest request =
          limelightClient.newRequestBuilder("/upload-fieldmap").POST(publisher).build();
      try {
        client.send(request, HttpResponse.BodyHandlers.discarding());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(
            String.format("Thread interrupted while trying to send request to %s", request.uri()),
            e);
      }
    }
  }

  /**
   * Returns the poses of the visible tags with the specified IDs.
   *
   * @param ids the set of tag IDs to retrieve
   * @return a list of {@link Pose3d} for the tags found in the field map; empty if none
   */
  public List<Pose3d> getVisibleTagPoses(Set<Integer> ids) {
    if (retriever == null) {
      return List.of();
    }
    return retriever.getFiducialMap().values().stream()
        .filter(fiducial -> ids.contains(fiducial.getId()))
        .map(Fiducial::getPosition)
        .toList();
  }

  /**
   * Returns the pose of a single tag by ID.
   *
   * @param id the ID of the tag
   * @return an {@link Optional} containing the tag's {@link Pose3d} if present, otherwise empty
   */
  public Optional<Pose3d> getTagPose(int id) {
    if (retriever == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(retriever.getFiducialMap().get(id)).map(Fiducial::getPosition);
  }
}
