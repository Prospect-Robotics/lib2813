package com.team2813.lib2813.limelight;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.units.Units;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.Assert.*;
import static org.junit.Assume.*;

@RunWith(Parameterized.class)
public class AprilTagLocationTest {
  @Rule
  public final FakeLimelight fakeLimelight = new FakeLimelight();
  @Parameterized.Parameters(name = "ID {0}")
  public static Collection<?> data() {
    return IntStream.rangeClosed(1, 22).boxed().toList();
  }
  
  @Parameterized.Parameter
  public int tagId;
  
  @Test
  public void aprilTag2025Test() throws Exception {
    assumeTrue(String.format("2025 field does not have tag id %d", tagId), AprilTagPoseHelper.isValid2025Tag(tagId));
    var helper = getHelper("frc2025r2.fmap");
    List<Pose3d> poses = helper.getVisibleTagPoses(Set.of(tagId));
    assertEquals("Expected a one element list", 1, poses.size());
    Pose3d pose = poses.get(0);
    assertAlmostEquals(AprilTagPoseHelper.get2025TagLocation(tagId), pose);
  }
  
  private AprilTagMapPoseHelper getHelper(String resource) throws IOException {
    AprilTagMapPoseHelper helper = new AprilTagMapPoseHelper(new LimelightClient("localhost"));
    try (var stream = getClass().getResourceAsStream(resource)) {
      helper.setFieldMap(stream, false);
    }
    return helper;
  }
  
  private static double DEGREE_1_RADIANS = Units.Degrees.of(1).in(Units.Radians);
  
  private void assertAlmostEquals(Pose3d expected, Pose3d actual) {
    String message = String.format("Expected \"%s\", but was, \"%s\"", expected, actual);
    double totalDiff = expected.getTranslation().minus(actual.getTranslation()).getDistance(new Translation3d(0, 0, 0));
    assertTrue(message, Math.abs(totalDiff) < 0.01);
    Rotation3d rotDiff = expected.getRotation().minus(actual.getRotation());
    assertTrue(message, Math.abs(MathUtil.angleModulus(rotDiff.getMeasureAngle().in(Units.Radians))) < DEGREE_1_RADIANS);
  }
}
