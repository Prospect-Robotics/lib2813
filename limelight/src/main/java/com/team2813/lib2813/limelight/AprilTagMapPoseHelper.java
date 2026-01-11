/*
Copyright 2025 Prospect Robotics SWENext Club

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
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

class AprilTagMapPoseHelper {
  private final LimelightClient limelightClient;
  private FiducialRetriever retriever;
  private static final HttpClient client =
      HttpClient.newBuilder()
          .connectTimeout(Duration.ofMillis(20))
          .executor(Executors.newFixedThreadPool(1))
          .build();

  public AprilTagMapPoseHelper(LimelightClient client) {
    this.limelightClient = client;
  }

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

  public List<Pose3d> getVisibleTagPoses(Set<Integer> ids) {
    if (retriever == null) {
      return List.of();
    }
    return retriever.getFiducialMap().values().stream()
        .filter(fiducial -> ids.contains(fiducial.getId()))
        .map(Fiducial::getPosition)
        .toList();
  }

  public Optional<Pose3d> getTagPose(int id) {
    if (retriever == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(retriever.getFiducialMap().get(id)).map(Fiducial::getPosition);
  }
}
