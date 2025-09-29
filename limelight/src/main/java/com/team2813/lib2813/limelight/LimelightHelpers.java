// LimelightHelpers v1.11 (REQUIRES LLOS 2025.0 OR LATER)

package com.team2813.lib2813.limelight;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.networktables.DoubleArrayEntry;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.TimestampedDoubleArray;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LimelightHelpers provides static methods and classes for interfacing with Limelight vision
 * cameras in FRC (FIRST Robotics Competition). This library supports all Limelight features 
 * including AprilTag tracking, Neural Networks, and standard color/retroreflective tracking.
 * 
 * <p>This helper class facilitates communication with Limelight cameras via NetworkTables and
 * HTTP requests, providing convenient methods for retrieving vision data, controlling camera
 * settings, and parsing results.
 * 
 * <p>Key features include:
 * <ul>
 *   <li>AprilTag/Fiducial marker detection and pose estimation</li>
 *   <li>Retroreflective target tracking</li>
 *   <li>Neural network classification and detection</li>
 *   <li>Barcode/QR code reading</li>
 *   <li>Robot localization (MegaTag, MegaTag2)</li>
 *   <li>IMU data integration</li>
 *   <li>Camera configuration and control</li>
 * </ul>
 *
 * <p>Copied from <a href="https://github.com/LimelightVision/limelightlib-wpijava">
 * github.com/LimelightVision/limelightlib-wpijava</a>
 * 
 * @version 1.11
 * @see <a href="https://docs.limelightvision.io">Limelight Documentation</a>
 * @author Limelight Vision
 */
class LimelightHelpers {

  /**
   * Cache for DoubleArrayEntry objects to avoid recreating NetworkTables entries.
   * This improves performance by reusing entries across multiple calls.
   * Key format: "tableName/entryName"
   */
  private static final Map<String, DoubleArrayEntry> doubleArrayEntries = new ConcurrentHashMap<>();

  /**
   * Represents a Color/Retroreflective Target Result extracted from JSON Output.
   * Contains pose information and targeting data for retroreflective vision targets
   * detected by color or retroreflective vision pipelines.
   * 
   * <p>This class provides access to:
   * <ul>
   *   <li>Camera pose relative to target</li>
   *   <li>Robot pose on field and relative to target</li>
   *   <li>Target pose relative to camera and robot</li>
   *   <li>Targeting metrics (area, offsets, skew)</li>
   * </ul>
   */
  public static class LimelightTarget_Retro {

    /** 
     * Camera pose in target space (6-element array: x, y, z, roll, pitch, yaw).
     * Positions in meters, angles in degrees.
     */
    @JsonProperty("t6c_ts")
    private double[] cameraPose_TargetSpace;

    /** 
     * Robot pose in field space (6-element array: x, y, z, roll, pitch, yaw).
     * Positions in meters, angles in degrees.
     */
    @JsonProperty("t6r_fs")
    private double[] robotPose_FieldSpace;

    /** 
     * Robot pose in target space (6-element array: x, y, z, roll, pitch, yaw).
     * Positions in meters, angles in degrees.
     */
    @JsonProperty("t6r_ts")
    private double[] robotPose_TargetSpace;

    /** 
     * Target pose in camera space (6-element array: x, y, z, roll, pitch, yaw).
     * Positions in meters, angles in degrees.
     */
    @JsonProperty("t6t_cs")
    private double[] targetPose_CameraSpace;

    /** 
     * Target pose in robot space (6-element array: x, y, z, roll, pitch, yaw).
     * Positions in meters, angles in degrees.
     */
    @JsonProperty("t6t_rs")
    private double[] targetPose_RobotSpace;

    /**
     * Gets the 3D camera pose in target space.
     * This represents where the camera is positioned relative to the detected target.
     * 
     * @return Pose3d representing camera position and orientation relative to the target
     */
    public Pose3d getCameraPose_TargetSpace() {
      return toPose3D(cameraPose_TargetSpace);
    }

    /**
     * Gets the 3D robot pose in field space.
     * This is the robot's estimated position on the field based on target detection.
     * 
     * @return Pose3d representing robot position and orientation on the field
     */
    public Pose3d getRobotPose_FieldSpace() {
      return toPose3D(robotPose_FieldSpace);
    }

    /**
     * Gets the 3D robot pose in target space.
     * This represents where the robot is positioned relative to the detected target.
     * 
     * @return Pose3d representing robot position and orientation relative to the target
     */
    public Pose3d getRobotPose_TargetSpace() {
      return toPose3D(robotPose_TargetSpace);
    }

    /**
     * Gets the 3D target pose in camera space.
     * This represents where the target is positioned relative to the camera.
     * 
     * @return Pose3d representing target position and orientation relative to the camera
     */
    public Pose3d getTargetPose_CameraSpace() {
      return toPose3D(targetPose_CameraSpace);
    }

    /**
     * Gets the 3D target pose in robot space.
     * This represents where the target is positioned relative to the robot's center.
     * 
     * @return Pose3d representing target position and orientation relative to the robot
     */
    public Pose3d getTargetPose_RobotSpace() {
      return toPose3D(targetPose_RobotSpace);
    }

    /**
     * Gets the 2D camera pose in target space (ignores z, roll, pitch).
     * Useful for planar navigation calculations.
     * 
     * @return Pose2d representing camera position and yaw relative to the target
     */
    public Pose2d getCameraPose_TargetSpace2D() {
      return toPose2D(cameraPose_TargetSpace);
    }

    /**
     * Gets the 2D robot pose in field space (ignores z, roll, pitch).
     * Useful for planar navigation and field positioning.
     * 
     * @return Pose2d representing robot position and yaw on the field
     */
    public Pose2d getRobotPose_FieldSpace2D() {
      return toPose2D(robotPose_FieldSpace);
    }

    /**
     * Gets the 2D robot pose in target space (ignores z, roll, pitch).
     * Useful for planar approach calculations.
     * 
     * @return Pose2d representing robot position and yaw relative to the target
     */
    public Pose2d getRobotPose_TargetSpace2D() {
      return toPose2D(robotPose_TargetSpace);
    }

    /**
     * Gets the 2D target pose in camera space (ignores z, roll, pitch).
     * Useful for planar targeting calculations.
     * 
     * @return Pose2d representing target position and yaw relative to the camera
     */
    public Pose2d getTargetPose_CameraSpace2D() {
      return toPose2D(targetPose_CameraSpace);
    }

    /**
     * Gets the 2D target pose in robot space (ignores z, roll, pitch).
     * Useful for planar targeting and robot-relative positioning.
     * 
     * @return Pose2d representing target position and yaw relative to the robot
     */
    public Pose2d getTargetPose_RobotSpace2D() {
      return toPose2D(targetPose_RobotSpace);
    }

    /** 
     * Target area as percentage of image (0-100).
     * Larger values indicate target is closer or larger in frame.
     */
    @JsonProperty("ta")
    public double ta;

    /** 
     * Horizontal offset from crosshair to target center in degrees.
     * Positive values indicate target is to the right of crosshair.
     */
    @JsonProperty("tx")
    public double tx;

    /** 
     * Vertical offset from crosshair to target center in degrees.
     * Positive values indicate target is above crosshair.
     */
    @JsonProperty("ty")
    public double ty;

    /** 
     * Horizontal offset from crosshair to target center in pixels.
     */
    @JsonProperty("txp")
    public double tx_pixels;

    /** 
     * Vertical offset from crosshair to target center in pixels.
     */
    @JsonProperty("typ")
    public double ty_pixels;

    /** 
     * Horizontal offset from principal point (no crosshair adjustment) in degrees.
     * More accurate than tx for calibrated cameras.
     */
    @JsonProperty("tx_nocross")
    public double tx_nocrosshair;

    /** 
     * Vertical offset from principal point (no crosshair adjustment) in degrees.
     * More accurate than ty for calibrated cameras.
     */
    @JsonProperty("ty_nocross")
    public double ty_nocrosshair;

    /** 
     * Target skew/rotation in degrees (-90 to 0).
     * Indicates the rotation of the target relative to camera orientation.
     */
    @JsonProperty("ts")
    public double ts;

    /**
     * Constructs a new LimelightTarget_Retro with default values.
     * Initializes all pose arrays to 6-element arrays of zeros.
     */
    public LimelightTarget_Retro() {
      cameraPose_TargetSpace = new double[6];
      robotPose_FieldSpace = new double[6];
      robotPose_TargetSpace = new double[6];
      targetPose_CameraSpace = new double[6];
      targetPose_RobotSpace = new double[6];
    }
  }

  /**
   * Represents an AprilTag/Fiducial Target Result extracted from JSON Output.
   * Contains pose information and targeting data for AprilTag fiducial markers.
   * 
   * <p>AprilTags are visual fiducial markers used extensively in FRC for robot
   * localization and pose estimation. This class provides comprehensive data about
   * detected AprilTags including their ID, family, and various pose transformations.
   * 
   * <p>This class provides access to:
   * <ul>
   *   <li>Fiducial ID and family information</li>
   *   <li>Camera pose relative to tag</li>
   *   <li>Robot pose on field and relative to tag</li>
   *   <li>Tag pose relative to camera and robot</li>
   *   <li>Targeting metrics (area, offsets, skew)</li>
   * </ul>
   */
  public static class LimelightTarget_Fiducial {

    /** 
     * Fiducial marker ID number.
     * For FRC AprilTags, this corresponds to specific field locations.
     */
    @JsonProperty("fID")
    public double fiducialID;

    /** 
     * Fiducial family name (e.g., "tag36h11", "tag16h5").
     * Different tag families have different error correction and detection properties.
     */
    @JsonProperty("fam")
    public String fiducialFamily;

    /** 
     * Camera pose in target space (6-element array: x, y, z, roll, pitch, yaw).
     * Positions in meters, angles in degrees.
     */
    @JsonProperty("t6c_ts")
    private double[] cameraPose_TargetSpace;

    /** 
     * Robot pose in field space (6-element array: x, y, z, roll, pitch, yaw).
     * Positions in meters, angles in degrees.
     * This is the robot's estimated global position based on this tag detection.
     */
    @JsonProperty("t6r_fs")
    private double[] robotPose_FieldSpace;

    /** 
     * Robot pose in target space (6-element array: x, y, z, roll, pitch, yaw).
     * Positions in meters, angles in degrees.
     */
    @JsonProperty("t6r_ts")
    private double[] robotPose_TargetSpace;

    /** 
     * Target pose in camera space (6-element array: x, y, z, roll, pitch, yaw).
     * Positions in meters, angles in degrees.
     */
    @JsonProperty("t6t_cs")
    private double[] targetPose_CameraSpace;

    /** 
     * Target pose in robot space (6-element array: x, y, z, roll, pitch, yaw).
     * Positions in meters, angles in degrees.
     */
    @JsonProperty("t6t_rs")
    private double[] targetPose_RobotSpace;

    /**
     * Gets the 3D camera pose in target space.
     * This represents where the camera is positioned relative to the detected AprilTag.
     * 
     * @return Pose3d representing camera position and orientation relative to the AprilTag
     */
    public Pose3d getCameraPose_TargetSpace() {
      return toPose3D(cameraPose_TargetSpace);
    }

    /**
     * Gets the 3D robot pose in field space.
     * This is the robot's estimated global position based on the AprilTag detection
     * and the known field layout.
     * 
     * @return Pose3d representing robot position and orientation on the field
     */
    public Pose3d getRobotPose_FieldSpace() {
      return toPose3D(robotPose_FieldSpace);
    }

    /**
     * Gets the 3D robot pose in target space.
     * This represents where the robot is positioned relative to the detected AprilTag.
     * 
     * @return Pose3d representing robot position and orientation relative to the AprilTag
     */
    public Pose3d getRobotPose_TargetSpace() {
      return toPose3D(robotPose_TargetSpace);
    }

    /**
     * Gets the 3D target pose in camera space.
     * This represents where the AprilTag is positioned relative to the camera.
     * Useful for direct targeting calculations.
     * 
     * @return Pose3d representing AprilTag position and orientation relative to the camera
     */
    public Pose3d getTargetPose_CameraSpace() {
      return toPose3D(targetPose_CameraSpace);
    }

    /**
     * Gets the 3D target pose in robot space.
     * This represents where the AprilTag is positioned relative to the robot's center.
     * Useful for robot-centric navigation and targeting.
     * 
     * @return Pose3d representing AprilTag position and orientation relative to the robot
     */
    public Pose3d getTargetPose_RobotSpace() {
      return toPose3D(targetPose_RobotSpace);
    }

    /**
     * Gets the 2D camera pose in target space (ignores z, roll, pitch).
     * Useful for planar navigation calculations relative to the tag.
     * 
     * @return Pose2d representing camera position and yaw relative to the AprilTag
     */
    public Pose2d getCameraPose_TargetSpace2D() {
      return toPose2D(cameraPose_TargetSpace);
    }

    /**
     * Gets the 2D robot pose in field space (ignores z, roll, pitch).
     * This is commonly used with WPILib's pose estimator for robot localization.
     * 
     * @return Pose2d representing robot position and yaw on the field
     */
    public Pose2d getRobotPose_FieldSpace2D() {
      return toPose2D(robotPose_FieldSpace);
    }

    /**
     * Gets the 2D robot pose in target space (ignores z, roll, pitch).
     * Useful for planar approach calculations to the tag.
     * 
     * @return Pose2d representing robot position and yaw relative to the AprilTag
     */
    public Pose2d getRobotPose_TargetSpace2D() {
      return toPose2D(robotPose_TargetSpace);
    }

    /**
     * Gets the 2D target pose in camera space (ignores z, roll, pitch).
     * Useful for planar targeting calculations.
     * 
     * @return Pose2d representing AprilTag position and yaw relative to the camera
     */
    public Pose2d getTargetPose_CameraSpace2D() {
      return toPose2D(targetPose_CameraSpace);
    }

    /**
     * Gets the 2D target pose in robot space (ignores z, roll, pitch).
     * Useful for planar targeting and robot-relative positioning.
     * 
     * @return Pose2d representing AprilTag position and yaw relative to the robot
     */
    public Pose2d getTargetPose_RobotSpace2D() {
      return toPose2D(targetPose_RobotSpace);
    }

    /** 
     * Target area as percentage of image (0-100).
     * Larger values indicate tag is closer or larger in frame.
     */
    @JsonProperty("ta")
    public double ta;

    /** 
     * Horizontal offset from crosshair to tag center in degrees.
     * Positive values indicate tag is to the right of crosshair.
     */
    @JsonProperty("tx")
    public double tx;

    /** 
     * Vertical offset from crosshair to tag center in degrees.
     * Positive values indicate tag is above crosshair.
     */
    @JsonProperty("ty")
    public double ty;

    /** 
     * Horizontal offset from crosshair to tag center in pixels.
     */
    @JsonProperty("txp")
    public double tx_pixels;

    /** 
     * Vertical offset from crosshair to tag center in pixels.
     */
    @JsonProperty("typ")
    public double ty_pixels;

    /** 
     * Horizontal offset from principal point (no crosshair adjustment) in degrees.
     * More accurate than tx for calibrated cameras. Should be preferred for precision work.
     */
    @JsonProperty("tx_nocross")
    public double tx_nocrosshair;

    /** 
     * Vertical offset from principal point (no crosshair adjustment) in degrees.
     * More accurate than ty for calibrated cameras. Should be preferred for precision work.
     */
    @JsonProperty("ty_nocross")
    public double ty_nocrosshair;

    /** 
     * Target skew/rotation in degrees (-90 to 0).
     * Indicates the rotation of the tag relative to camera orientation.
     */
    @JsonProperty("ts")
    public double ts;

    /**
     * Constructs a new LimelightTarget_Fiducial with default values.
     * Initializes all pose arrays to 6-element arrays of zeros.
     */
    public LimelightTarget_Fiducial() {
      cameraPose_TargetSpace = new double[6];
      robotPose_FieldSpace = new double[6];
      robotPose_TargetSpace = new double[6];
      targetPose_CameraSpace = new double[6];
      targetPose_RobotSpace = new double[6];
    }
  }

  /**
   * Represents a Barcode Target Result extracted from JSON Output.
   * Supports various barcode formats including QR codes, DataMatrix, Code128, and others.
   * 
   * <p>Limelight can detect and decode various 1D and 2D barcodes, which can be useful
   * for game piece identification, autonomous mode selection, or other data encoding needs.
   * 
   * <p>Supported barcode families include:
   * <ul>
   *   <li>QR Code</li>
   *   <li>DataMatrix</li>
   *   <li>Code 128</li>
   *   <li>Code 39</li>
   *   <li>EAN/UPC</li>
   *   <li>And others</li>
   * </ul>
   */
  public static class LimelightTarget_Barcode {

    /** 
     * Barcode family type (e.g. "QR", "DataMatrix", "Code128").
     * Indicates the encoding format of the detected barcode.
     */
    @JsonProperty("fam")
    public String family;

    /** 
     * Decoded data content of the barcode as a string.
     * This is the actual information encoded in the barcode.
     */
    @JsonProperty("data")
    public String data;

    /** 
     * Horizontal offset from crosshair to barcode center in pixels.
     */
    @JsonProperty("txp")
    public double tx_pixels;

    /** 
     * Vertical offset from crosshair to barcode center in pixels.
     */
    @JsonProperty("typ")
    public double ty_pixels;

    /** 
     * Horizontal offset from crosshair to barcode center in degrees.
     * Positive values indicate barcode is to the right of crosshair.
     */
    @JsonProperty("tx")
    public double tx;

    /** 
     * Vertical offset from crosshair to barcode center in degrees.
     * Positive values indicate barcode is above crosshair.
     */
    @JsonProperty("ty")
    public double ty;

    /** 
     * Horizontal offset from principal point (no crosshair adjustment) in degrees.
     */
    @JsonProperty("tx_nocross")
    public double tx_nocrosshair;

    /** 
     * Vertical offset from principal point (no crosshair adjustment) in degrees.
     */
    @JsonProperty("ty_nocross")
    public double ty_nocrosshair;

    /** 
     * Target area as percentage of image (0-100).
     * Larger values indicate barcode is closer or larger in frame.
     */
    @JsonProperty("ta")
    public double ta;

    /** 
     * Corner points of the barcode bounding box in image coordinates.
     * Array format: [[x0, y0], [x1, y1], [x2, y2], [x3, y3]]
     * Useful for visualization or precise barcode localization.
     */
    @JsonProperty("pts")
    public double[][] corners;

    /**
     * Constructs a new LimelightTarget_Barcode with default values.
     */
    public LimelightTarget_Barcode() {}

    /**
     * Gets the barcode family type.
     * 
     * @return String representing the barcode format (e.g., "QR", "DataMatrix", "Code128")
     */
    public String getFamily() {
      return family;
    }
  }

  /**
   * Represents a Neural Classifier Pipeline Result extracted from JSON Output.
   * Used for neural network classification tasks (identifying what an object is).
   * 
   * <p>Neural classifiers categorize entire images or regions into predefined classes.
   * Unlike detectors, classifiers don't provide bounding boxes but instead classify
   * the primary object in view.
   * 
   * <p>Common FRC use cases:
   * <ul>
   *   <li>Game piece identification (cone vs cube)</li>
   *   <li>Color classification</li>
   *   <li>Orientation detection</li>
   *   <li>State recognition</li>
   * </ul>
   */
  public static class LimelightTarget_Classifier {

    /** 
     * Name of the detected class.
     * This is the human-readable label for the classification result.
     */
    @JsonProperty("class")
    public String className;

    /** 
     * Numeric ID of the detected class.
     * Useful for switch statements and numerical comparisons.
     */
    @JsonProperty("classID")
    public double classID;

    /** 
     * Confidence score of the classification (0-100).
     * Higher values indicate higher confidence in the classification result.
     */
    @JsonProperty("conf")
    public double confidence;

    /** 
     * Zone number if using zone-based classification.
     * Zones allow different classification behavior in different areas of the image.
     */
    @JsonProperty("zone")
    public double zone;

    /** 
     * Horizontal offset from crosshair to classification region center in degrees.
     */
    @JsonProperty("tx")
    public double tx;

    /** 
     * Horizontal offset from crosshair to classification region center in pixels.
     */
    @JsonProperty("txp")
    public double tx_pixels;

    /** 
     * Vertical offset from crosshair to classification region center in degrees.
     */
    @JsonProperty("ty")
    public double ty;

    /** 
     * Vertical offset from crosshair to classification region center in pixels.
     */
    @JsonProperty("typ")
    public double ty_pixels;

    /**
     * Constructs a new LimelightTarget_Classifier with default values.
     */
    public LimelightTarget_Classifier() {}
  }

  /**
   * Represents a Neural Detector Pipeline Result extracted from JSON Output.
   * Used for neural network object detection tasks (finding and classifying objects).
   * 
   * <p>Neural detectors locate objects within an image and classify them. Unlike
   * classifiers, detectors can find multiple objects and provide bounding box information.
   * 
   * <p>Common FRC use cases:
   * <ul>
   *   <li>Game piece detection and localization</li>
   *   <li>Multiple object tracking</li>
   *   <li>Robot detection</li>
   *   <li>Custom target identification</li>
   * </ul>
   */
  public static class LimelightTarget_Detector {

    /** 
     * Name of the detected class.
     * This is the human-readable label for what was detected.
     */
    @JsonProperty("class")
    public String className;

    /** 
     * Numeric ID of the detected class.
     * Useful for switch statements and numerical comparisons.
     */
    @JsonProperty("classID")
    public double classID;

    /** 
     * Confidence score of the detection (0-100).
     * Higher values indicate higher confidence that the object is present.
     */
    @JsonProperty("conf")
    public double confidence;

    /** 
     * Target area as percentage of image (0-100).
     * Size of the detection bounding box.
     */
    @JsonProperty("ta")
    public double ta;

    /** 
     * Horizontal offset from crosshair to detection center in degrees.
     * Positive values indicate detection is to the right of crosshair.
     */
    @JsonProperty("tx")
    public double tx;

    /** 
     * Vertical offset from crosshair to detection center in degrees.
     * Positive values indicate detection is above crosshair.
     */
    @JsonProperty("ty")
    public double ty;

    /** 
     * Horizontal offset from crosshair to detection center in pixels.
     */
    @JsonProperty("txp")
    public double tx_pixels;

    /** 
     * Vertical offset from crosshair to detection center in pixels.
     */
    @JsonProperty("typ")
    public double ty_pixels;

    /** 
     * Horizontal offset from principal point (no crosshair adjustment) in degrees.
     */
    @JsonProperty("tx_nocross")
    public double tx_nocrosshair;

    /** 
     * Vertical offset from principal point (no crosshair adjustment) in degrees.
     */
    @JsonProperty("ty_nocross")
    public double ty_nocrosshair;

    /**
     * Constructs a new LimelightTarget_Detector with default values.
     */
    public LimelightTarget_Detector() {}
  }

  /**
   * Limelight Results object, parsed from a Limelight's JSON results output.
   * Contains all targeting data, pose estimates, and metadata from a Limelight camera.
   * 
   * <p>This is the primary result container that aggregates all detection types:
   * retroreflective targets, AprilTags, neural detections, classifications, and barcodes.
   * It also includes robot pose estimates and timing information.
   * 
   * <p>Use {@link #getLatestResults(String)} to obtain this object from NetworkTables.
   */
  public static class LimelightResults {

    /** 
     * Error message if JSON parsing failed, null otherwise.
     * Check this field to ensure results are valid before use.
     */
    public String error;

    /** 
     * Current pipeline index (0-9).
     * Indicates which vision pipeline is currently active.
     */
    @JsonProperty("pID")
    public double pipelineID;

    /** 
     * Pipeline processing latency in milliseconds.
     * Time taken for the vision pipeline to process the image.
     */
    @JsonProperty("tl")
    public double latency_pipeline;

    /** 
     * Image capture latency in milliseconds.
     * Time between when the frame was captured and when processing began.
     */
    @JsonProperty("cl")
    public double latency_capture;

    /** 
     * JSON parsing latency in milliseconds (calculated locally).
     * Time taken to deserialize the JSON on the RoboRIO.
     */
    public double latency_jsonParse;

    /** 
     * Timestamp when Limelight published the results (in seconds).
     * Unix timestamp from the Limelight's internal clock.
     */
    @JsonProperty("ts")
    public double timestamp_LIMELIGHT_publish;

    /** 
     * Timestamp when the RoboRIO FPGA captured the image (in seconds).
     * FPGA timestamp synchronized with robot code timing.
     */
    @JsonProperty("ts_rio")
    public double timestamp_RIOFPGA_capture;

    /** 
     * Whether valid target(s) are detected.
     * True if at least one target meets the pipeline's criteria.
     */
    @JsonProperty("v")
    @JsonFormat(shape = Shape.NUMBER)
    public boolean valid;

    /** 
     * Robot pose in field space (6-element array: x, y, z, roll, pitch, yaw).
     * Positions in meters, angles in degrees.
     * Uses Limelight's internal coordinate system.
     */
    @JsonProperty("botpose")
    public double[] botpose;

    /** 
     * Robot pose in WPILib red alliance coordinate system.
     * Origin at red alliance driver station wall.
     */
    @JsonProperty("botpose_wpired")
    public double[] botpose_wpired;

    /** 
     * Robot pose in WPILib blue alliance coordinate system.
     * Origin at blue alliance driver station wall. Recommended for most use cases.
     */
    @JsonProperty("botpose_wpiblue")
    public double[] botpose_wpiblue;

    /** 
     * Number of AprilTags used for pose estimation.
     * Higher counts generally indicate more reliable pose estimates.
     */
    @JsonProperty("botpose_tagcount")
    public double botpose_tagcount;

    /** 
     * Distance in meters between the two farthest AprilTags used for pose.
     * Larger spans generally improve pose accuracy.
     */
    @JsonProperty("botpose_span")
    public double botpose_span;

    /** 
     * Average distance to AprilTags used for pose estimation in meters.
     * Closer tags generally provide more accurate pose estimates.
     */
    @JsonProperty("botpose_avgdist")
    public double botpose_avgdist;

    /** 
     * Average area of AprilTags used for pose estimation (0-100).
     * Larger areas indicate tags are closer and provide better resolution.
     */
    @JsonProperty("botpose_avgarea")
    public double botpose_avgarea;

    /** 
     * Camera pose in robot space (6-element array: x, y, z, roll, pitch, yaw).
     * Describes where the camera is mounted relative to robot center.
     */
    @JsonProperty("t6c_rs")
    public double[] camerapose_robotspace;

    /**
     * Gets the 3D robot pose from the botpose array.
     * Uses Limelight's internal coordinate system.
     * 
     * @return Pose3d representing robot position and orientation
     */
    public Pose3d getBotPose3d() {
      return toPose3D(botpose);
    }

    /**
     * Gets the 3D robot pose in WPILib red alliance coordinates.
     * Not recommended - use blue alliance for consistency.
     * 
     * @return Pose3d in red alliance coordinate system
     */
    public Pose3d getBotPose3d_wpiRed() {
      return toPose3D(botpose_wpired);
    }

    /**
     * Gets the 3D robot pose in WPILib blue alliance coordinates.
     * Recommended for most applications.
     * 
     * @return Pose3d in blue alliance coordinate system
     */
    public Pose3d getBotPose3d_wpiBlue() {
      return toPose3D(botpose_wpiblue);
    }

    /**
     * Gets the 2D robot pose from the botpose array.
     * Uses Limelight's internal coordinate system.
     * 
     * @return Pose2d representing robot position and yaw
     */
    public Pose2d getBotPose2d() {
      return toPose2D(botpose);
    }

    /**
     * Gets the 2D robot pose in WPILib red alliance coordinates.
     * Not recommended - use blue alliance for consistency.
     * 
     * @return Pose2d in red alliance coordinate system
     */
    public Pose2d getBotPose2d_wpiRed() {
      return toPose2D(botpose_wpired);
    }

    /**
     * Gets the 2D robot pose in WPILib blue alliance coordinates.
     * Recommended for use with WPILib pose estimators.
     * 
     * @return Pose2d in blue alliance coordinate system
     */
    public Pose2d getBotPose2d_wpiBlue() {
      return toPose2D(botpose_wpiblue);
    }

    /** 
     * Array of retroreflective target results.
     * Contains data for all detected retroreflective/color targets.
     */
    @JsonProperty("Retro")
    public LimelightTarget_Retro[] targets_Retro;

    /** 
     * Array of AprilTag/fiducial target results.
     * Contains data for all detected AprilTags.
     */
    @JsonProperty("Fiducial")
    public LimelightTarget_Fiducial[] targets_Fiducials;

    /** 
     * Array of neural classifier results.
     * Contains classification results if a classifier pipeline is active.
     */
    @JsonProperty("Classifier")
    public LimelightTarget_Classifier[] targets_Classifier;

    /** 
     * Array of neural detector results.
     * Contains detection results if a detector pipeline is active.
     */
    @JsonProperty("Detector")
    public LimelightTarget_Detector[] targets_Detector;

    /** 
     * Array of barcode detection results.
     * Contains decoded barcode data.
     */
    @JsonProperty("Barcode")
    public LimelightTarget_Barcode[] targets_Barcode;

    /**
     * Constructs a new LimelightResults with default values.
     * Initializes all arrays to appropriate default sizes.
     */
    public LimelightResults() {
      botpose = new double[6];
      botpose_wpired = new double[6];
      botpose_wpiblue = new double[6];
      camerapose_robotspace = new double[6];
      targets_Retro = new LimelightTarget_Retro[0];
      targets_Fiducials = new LimelightTarget_Fiducial[0];
      targets_Classifier = new LimelightTarget_Classifier[0];
      targets_Detector = new LimelightTarget_Detector[0];
      targets_Barcode = new LimelightTarget_Barcode[0];
    }
  }

  /**
   * Represents a Limelight Raw Fiducial result from Limelight's NetworkTables output.
   * Provides quick access to basic AprilTag detection data without full JSON parsing.
   * 
   * <p>RawFiducials are lightweight representations of AprilTag detections available
   * directly from NetworkTables. They're faster to access than full JSON parsing
   * and useful when you only need basic tag information.
   */
  public static class RawFiducial {
    /** 
     * AprilTag ID number.
     * Corresponds to specific field locations in FRC.
     */
    public int id = 0;
    
    /** 
     * Horizontal offset from principal point in normalized coordinates (-1 to 1).
     * More accurate than pixel-based offsets for calibrated cameras.
     */
    public double txnc = 0;
    
    /** 
     * Vertical offset from principal point in normalized coordinates (-1 to 1).
     * More accurate than pixel-based offsets for calibrated cameras.
     */
    public double tync = 0;
    
    /** 
     * Target area as percentage of image (0-100).
     * Indicates size of tag in camera view.
     */
    public double ta = 0;
    
    /** 
     * Distance from camera to tag in meters.
     * Based on tag's known size and camera calibration.
     */
    public double distToCamera = 0;
    
    /** 
     * Distance from robot center to tag in meters.
     * Accounts for camera offset from robot center.
     */
    public double distToRobot = 0;
    
    /** 
     * Pose ambiguity (0 = unambiguous, higher = more ambiguous).
     * AprilTag pose estimation can have multiple solutions; this indicates confidence.
     * Values above 0.2 are generally considered ambiguous.
     */
    public double ambiguity = 0;

    /**
     * Constructs a RawFiducial with specified detection parameters.
     * 
     * @param id AprilTag ID number
     * @param txnc Horizontal offset from principal point (normalized -1 to 1)
     * @param tync Vertical offset from principal point (normalized -1 to 1)
     * @param ta Target area percentage (0-100)
     * @param distToCamera Distance from camera to tag in meters
     * @param distToRobot Distance from robot center to tag in meters
     * @param ambiguity Pose ambiguity value (0 = certain, higher = ambiguous)
     */
    public RawFiducial(
        int id,
        double txnc,
        double tync,
        double ta,
        double distToCamera,
        double distToRobot,
        double ambiguity) {
      this.id = id;
      this.txnc = txnc;
      this.tync = tync;
      this.ta = ta;
      this.distToCamera = distToCamera;
      this.distToRobot = distToRobot;
      this.ambiguity = ambiguity;
    }
  }

  /**
   * Represents a Limelight Raw Neural Detector result from Limelight's NetworkTables output.
   * Provides quick access to neural detector data including bounding box corners.
   * 
   * <p>RawDetections are lightweight representations of neural network detections available
   * directly from NetworkTables. They include bounding box corner coordinates for
   * visualization and precise object localization.
   */
  public static class RawDetection {
    /** 
     * Detected class ID.
     * Numeric identifier for the detected object class.
     */
    public int classId = 0;
    
    /** 
     * Horizontal offset from principal point in normalized coordinates (-1 to 1).
     */
    public double txnc = 0;
    
    /** 
     * Vertical offset from principal point in normalized coordinates (-1 to 1).
     */
    public double tync = 0;
    
    /** 
     * Target area as percentage of image (0-100).
     * Size of the detection bounding box.
     */
    public double ta = 0;
    
    /** X coordinate of corner 0 of bounding box (normalized -1 to 1) */
    public double corner0_X = 0;
    
    /** Y coordinate of corner 0 of bounding box (normalized -1 to 1) */
    public double corner0_Y = 0;
    
    /** X coordinate of corner 1 of bounding box (normalized -1 to 1) */
    public double corner1_X = 0;
    
    /** Y coordinate of corner 1 of bounding box (normalized -1 to 1) */
    public double corner1_Y = 0;
    
    /** X coordinate of corner 2 of bounding box (normalized -1 to 1) */
    public double corner2_X = 0;
    
    /** Y coordinate of corner 2 of bounding box (normalized -1 to 1) */
    public double corner2_Y = 0;
    
    /** X coordinate of corner 3 of bounding box (normalized -1 to 1) */
    public double corner3_X = 0;
    
    /** Y coordinate of corner 3 of bounding box (normalized -1 to 1) */
    public double corner3_Y = 0;

    /**
     * Constructs a RawDetection with specified detection parameters.
     * Corner coordinates define the bounding box of the detected object.
     * 
     * @param classId Detected class ID
     * @param txnc Horizontal offset from principal point (normalized)
     * @param tync Vertical offset from principal point (normalized)
     * @param ta Target area percentage
     * @param corner0_X X coordinate of bounding box corner 0
     * @param corner0_Y Y coordinate of bounding box corner 0
     * @param corner1_X X coordinate of bounding box corner 1
     * @param corner1_Y Y coordinate of bounding box corner 1
     * @param corner2_X X coordinate of bounding box corner 2
     * @param corner2_Y Y coordinate of bounding box corner 2
     * @param corner3_X X coordinate of bounding box corner 3
     * @param corner3_Y Y coordinate of bounding box corner 3
     */
    public RawDetection(
        int classId,
        double txnc,
        double tync,
        double ta,
        double corner0_X,
        double corner0_Y,
        double corner1_X,
        double corner1_Y,
        double corner2_X,
        double corner2_Y,
        double corner3_X,
        double corner3_Y) {
      this.classId = classId;
      this.txnc = txnc;
      this.tync = tync;
      this.ta = ta;
      this.corner0_X = corner0_X;
      this.corner0_Y = corner0_Y;
      this.corner1_X = corner1_X;
      this.corner1_Y = corner1_Y;
      this.corner2_X = corner2_X;
      this.corner2_Y = corner2_Y;
      this.corner3_X = corner3_X;
      this.corner3_Y = corner3_Y;
    }
  }

  /**
   * Represents a 3D Pose Estimate with metadata about the quality and source of the estimate.
   * Used primarily with vision-based localization systems like MegaTag and MegaTag2.
   * 
   * <p>PoseEstimate combines a robot pose with quality metrics that help determine how
   * reliable the estimate is. Use these metrics with WPILib's pose estimator to
   * dynamically adjust standard deviations based on detection quality.
   * 
   * <p>Quality indicators include:
   * <ul>
   *   <li>Tag count - more tags generally means better accuracy</li>
   *   <li>Tag span - wider spacing between tags improves accuracy</li>
   *   <li>Average distance - closer tags provide better resolution</li>
   *   <li>Average area - larger tags in view provide better data</li>
   *   <li>Ambiguity data for each tag</li>
   * </ul>
   */
  public static class PoseEstimate {
    /** 
     * The estimated 2D robot pose on the field.
     * Use with WPILib's pose estimator via addVisionMeasurement().
     */
    public Pose2d pose;
    
    /** 
     * Timestamp of the pose estimate in seconds (FPGA timestamp).
     * Synchronized with robot code timing for accurate pose integration.
     */
    public double timestampSeconds;
    
    /** 
     * Total latency of the pose estimate in milliseconds.
     * Includes capture and processing time.
     */
    public double latency;
    
    /** 
     * Number of AprilTags used to compute this pose estimate.
     * More tags generally provide more accurate and stable estimates.
     */
    public int tagCount;
    
    /** 
     * Distance between the farthest AprilTags used in meters.
     * Larger spans reduce angular error and improve accuracy.
     */
    public double tagSpan;
    
    /** 
     * Average distance to all AprilTags used in meters.
     * Closer tags provide better pixel resolution and accuracy.
     */
    public double avgTagDist;
    
    /** 
     * Average area of AprilTags used as percentage of image (0-100).
     * Larger areas mean better resolution and more accurate pose data.
     */
    public double avgTagArea;

    /** 
     * Raw fiducial data for all tags used in pose estimation.
     * Includes individual tag metrics like ambiguity and distance.
     */
    public RawFiducial[] rawFiducials;
    
    /** 
     * Whether this estimate was computed using the MegaTag2 algorithm.
     * MegaTag2 uses robot orientation data for improved accuracy.
     */
    public boolean isMegaTag2;

    /**
     * Constructs a PoseEstimate object with default values.
     * All numeric fields initialized to 0, pose to origin, arrays to empty.
     */
    public PoseEstimate() {
      this.pose = new Pose2d();
      this.timestampSeconds = 0;
      this.latency = 0;
      this.tagCount = 0;
      this.tagSpan = 0;
      this.avgTagDist = 0;
      this.avgTagArea = 0;
      this.rawFiducials = new RawFiducial[] {};
      this.isMegaTag2 = false;
    }

    /**
     * Constructs a PoseEstimate with all specified parameters.
     * 
     * @param pose The estimated 2D robot pose
     * @param timestampSeconds FPGA timestamp in seconds
     * @param latency Total latency in milliseconds
     * @param tagCount Number of AprilTags used
     * @param tagSpan Distance between farthest tags in meters
     * @param avgTagDist Average distance to tags in meters
     * @param avgTagArea Average tag area as percentage
     * @param rawFiducials Array of raw fiducial data for quality assessment
     * @param isMegaTag2 Whether MegaTag2 algorithm was used
     */
    public PoseEstimate(
        Pose2d pose,
        double timestampSeconds,
        double latency,
        int tagCount,
        double tagSpan,
        double avgTagDist,
        double avgTagArea,
        RawFiducial[] rawFiducials,
        boolean isMegaTag2) {

      this.pose = pose;
      this.timestampSeconds = timestampSeconds;
      this.latency = latency;
      this.tagCount = tagCount;
      this.tagSpan = tagSpan;
      this.avgTagDist = avgTagDist;
      this.avgTagArea = avgTagArea;
      this.rawFiducials = rawFiducials;
      this.isMegaTag2 = isMegaTag2;
    }
  }

  /**
   * Encapsulates the state of an internal Limelight IMU (Inertial Measurement Unit).
   * Contains orientation, angular velocity, and acceleration data.
   * 
   * <p>Limelight 3 and newer models include a built-in IMU that can provide robot
   * orientation data. This is particularly useful for MegaTag2 localization which
   * requires robot orientation input for optimal accuracy.
   * 
   * <p>IMU data includes:
   * <ul>
   *   <li>Orientation angles (roll, pitch, yaw)</li>
   *   <li>Angular velocities (gyroscope data)</li>
   *   <li>Linear accelerations (accelerometer data)</li>
   * </ul>
   */
  public static class IMUData {
    /** Robot yaw angle in degrees (heading, rotation around Z axis) */
    public double robotYaw = 0.0;
    
    /** Roll angle in degrees (rotation around X axis) */
    public double Roll = 0.0;
    
    /** Pitch angle in degrees (rotation around Y axis) */
    public double Pitch = 0.0;
    
    /** Yaw angle in degrees (rotation around Z axis) */
    public double Yaw = 0.0;
    
    /** Angular velocity around X axis in degrees per second */
    public double gyroX = 0.0;
    
    /** Angular velocity around Y axis in degrees per second */
    public double gyroY = 0.0;
    
    /** Angular velocity around Z axis in degrees per second */
    public double gyroZ = 0.0;
    
    /** Linear acceleration along X axis in G's (1G = 9.81 m/s²) */
    public double accelX = 0.0;
    
    /** Linear acceleration along Y axis in G's */
    public double accelY = 0.0;
    
    /** Linear acceleration along Z axis in G's */
    public double accelZ = 0.0;

    /**
     * Constructs an IMUData object with all values initialized to 0.
     */
    public IMUData() {}

    /**
     * Constructs an IMUData object from a double array.
     * Expected array format: [robotYaw, Roll, Pitch, Yaw, gyroX, gyroY, gyroZ, accelX, accelY, accelZ]
     * 
     * @param imuData Array of IMU values (must be at least 10 elements)
     */
    public IMUData(double[] imuData) {
      if (imuData != null && imuData.length >= 10) {
        this.robotYaw = imuData[0];
        this.Roll = imuData[1];
        this.Pitch = imuData[2];
        this.Yaw = imuData[3];
        this.gyroX = imuData[4];
        this.gyroY = imuData[5];
        this.gyroZ = imuData[6];
        this.accelX = imuData[7];
        this.accelY = imuData[8];
        this.accelZ = imuData[9];
      }
    }
  }

  /** 
   * Jackson ObjectMapper instance for JSON deserialization.
   * Lazily initialized on first use.
   */
  private static ObjectMapper mapper;

  /** 
   * When enabled, prints JSON parsing time to console in milliseconds.
   * Useful for performance debugging and optimization.
   * Set to true to enable performance profiling.
   */
  static boolean profileJSON = false;

  /**
   * Sanitizes the Limelight name by returning "limelight" if the input is null or empty.
   * This ensures a valid NetworkTables name even when no specific name is provided.
   * 
   * @param name The Limelight name to sanitize
   * @return The sanitized name, or "limelight" if input was null/empty
   */
  static final String sanitizeName(String name) {
    if (name == null || name.isEmpty()) {
      return "limelight";
    }
    return name;
  }

  /**
   * Takes a 6-length array of pose data and converts it to a Pose3d object.
   * Array format: [x, y, z, roll, pitch, yaw] where:
   * - x, y, z are in meters
   * - roll, pitch, yaw are in degrees
   * 
   * @param inData Array containing pose data [x, y, z, roll, pitch, yaw]
   * @return Pose3d object representing the pose, or empty Pose3d if invalid data
   */
  public static Pose3d toPose3D(double[] inData) {
    if (inData.length < 6) {
      // System.err.println("Bad LL 3D Pose Data!");
      return new Pose3d();
    }
    return new Pose3d(
        new Translation3d(inData[0], inData[1], inData[2]),
        new Rotation3d(
            Units.degreesToRadians(inData[3]),
            Units.degreesToRadians(inData[4]),
            Units.degreesToRadians(inData[5])));
  }

  /**
   * Takes a 6-length array of pose data and converts it to a Pose2d object.
   * Uses only x, y, and yaw components, ignoring z, roll, and pitch.
   * Array format: [x, y, z, roll, pitch, yaw] where:
   * - x, y are in meters
   * - yaw is in degrees
   * 
   * @param inData Array containing pose data [x, y, z, roll, pitch, yaw]
   * @return Pose2d object representing the pose, or empty Pose2d if invalid data
   */
  public static Pose2d toPose2D(double[] inData) {
    if (inData.length < 6) {
      // System.err.println("Bad LL 2D Pose Data!");
      return new Pose2d();
    }
    Translation2d tran2d = new Translation2d(inData[0], inData[1]);
    Rotation2d r2d = new Rotation2d(Units.degreesToRadians(inData[5]));
    return new Pose2d(tran2d, r2d);
  }

  /**
   * Converts a Pose3d object to an array of doubles in the format [x, y, z, roll, pitch, yaw].
   * Translation components are in meters, rotation components are in degrees.
   * Useful for sending pose data to the Limelight or storing in NetworkTables.
   * 
   * @param pose The Pose3d object to convert
   * @return A 6-element array containing [x, y, z, roll, pitch, yaw]
   */
  public static double[] pose3dToArray(Pose3d pose) {
    double[] result = new double[6];
    result[0] = pose.getTranslation().getX();
    result[1] = pose.getTranslation().getY();
    result[2] = pose.getTranslation().getZ();
    result[3] = Units.radiansToDegrees(pose.getRotation().getX());
    result[4] = Units.radiansToDegrees(pose.getRotation().getY());
    result[5] = Units.radiansToDegrees(pose.getRotation().getZ());
    return result;
  }

  /**
   * Converts a Pose2d object to an array of doubles in the format [x, y, z, roll, pitch, yaw].
   * Translation components are in meters, rotation components are in degrees.
   * Note: z, roll, and pitch will be 0 since Pose2d only contains x, y, and yaw.
   * 
   * @param pose The Pose2d object to convert
   * @return A 6-element array containing [x, y, 0, 0, 0, yaw]
   */
  public static double[] pose2dToArray(Pose2d pose) {
    double[] result = new double[6];
    result[0] = pose.getTranslation().getX();
    result[1] = pose.getTranslation().getY();
    result[2] = 0;
    result[3] = Units.radiansToDegrees(0);
    result[4] = Units.radiansToDegrees(0);
    result[5] = Units.radiansToDegrees(pose.getRotation().getRadians());
    return result;
  }

  /**
   * Extracts a single element from a double array at the specified position.
   * Returns 0 if the array is too short or position is out of bounds.
   * 
   * @param inData The source array
   * @param position The index to extract (0-based)
   * @return The value at the position, or 0 if position is out of bounds
   */
  private static double extractArrayEntry(double[] inData, int position) {
    if (inData.length < position + 1) {
      return 0;
    }
    return inData[position];
  }

  /**
   * Internal method to retrieve a bot pose estimate from NetworkTables.
   * Extracts pose data, latency, tag information, and raw fiducial data from
   * a timestamped NetworkTables entry.
   * 
   * <p>The pose array format is:
   * <ul>
   *   <li>[0-5]: x, y, z, roll, pitch, yaw</li>
   *   <li>[6]: latency (ms)</li>
   *   <li>[7]: tag count</li>
   *   <li>[8]: tag span (m)</li>
   *   <li>[9]: average tag distance (m)</li>
   *   <li>[10]: average tag area (%)</li>
   *   <li>[11+]: raw fiducial data (7 values per tag)</li>
   * </ul>
   * 
   * @param limelightName Name/identifier of the Limelight
   * @param entryName NetworkTables entry name to read from (e.g., "botpose_wpiblue")
   * @param isMegaTag2 Whether this is a MegaTag2 estimate
   * @return PoseEstimate object, or null if no data available
   */
  private static PoseEstimate getBotPoseEstimate(
      String limelightName, String entryName, boolean isMegaTag2) {
    DoubleArrayEntry poseEntry =
        LimelightHelpers.getLimelightDoubleArrayEntry(limelightName, entryName);

    TimestampedDoubleArray tsValue = poseEntry.getAtomic();
    double[] poseArray = tsValue.value;
    long timestamp = tsValue.timestamp;

    if (poseArray.length == 0) {
      // Handle the case where no data is available
      return null; // or some default PoseEstimate
    }

    var pose = toPose2D(poseArray);
    double latency = extractArrayEntry(poseArray, 6);
    int tagCount = (int) extractArrayEntry(poseArray, 7);
    double tagSpan = extractArrayEntry(poseArray, 8);
    double tagDist = extractArrayEntry(poseArray, 9);
    double tagArea = extractArrayEntry(poseArray, 10);

    // Convert server timestamp from microseconds to seconds and adjust for latency
    double adjustedTimestamp = (timestamp / 1000000.0) - (latency / 1000.0);

    RawFiducial[] rawFiducials = new RawFiducial[tagCount];
    int valsPerFiducial = 7;
    int expectedTotalVals = 11 + valsPerFiducial * tagCount;

    if (poseArray.length != expectedTotalVals) {
      // Don't populate fiducials if array length doesn't match expected format
    } else {
      for (int i = 0; i < tagCount; i++) {
        int baseIndex = 11 + (i * valsPerFiducial);
        int id = (int) poseArray[baseIndex];
        double txnc = poseArray[baseIndex + 1];
        double tync = poseArray[baseIndex + 2];
        double ta = poseArray[baseIndex + 3];
        double distToCamera = poseArray[baseIndex + 4];
        double distToRobot = poseArray[baseIndex + 5];
        double ambiguity = poseArray[baseIndex + 6];
        rawFiducials[i] = new RawFiducial(id, txnc, tync, ta, distToCamera, distToRobot, ambiguity);
      }
    }

    return new PoseEstimate(
        pose,
        adjustedTimestamp,
        latency,
        tagCount,
        tagSpan,
        tagDist,
        tagArea,
        rawFiducials,
        isMegaTag2);
  }

  /**
   * Gets the latest raw fiducial/AprilTag detection results from NetworkTables.
   * Provides quick access to basic tag data without full JSON parsing.
   * 
   * @param limelightName Name/identifier of the Limelight
   * @return Array of RawFiducial objects containing detection details, or empty array if none
   */
  public static RawFiducial[] getRawFiducials(String limelightName) {
    var entry = LimelightHelpers.getLimelightNTTableEntry(limelightName, "rawfiducials");
    var rawFiducialArray = entry.getDoubleArray(new double[0]);
    int valsPerEntry = 7;
    if (rawFiducialArray.length % valsPerEntry != 0) {
      return new RawFiducial[0];
    }

    int numFiducials = rawFiducialArray.length / valsPerEntry;
    RawFiducial[] rawFiducials = new RawFiducial[numFiducials];

    for (int i = 0; i < numFiducials; i++) {
      int baseIndex = i * valsPerEntry;
      int id = (int) extractArrayEntry(rawFiducialArray, baseIndex);
      double txnc = extractArrayEntry(rawFiducialArray, baseIndex + 1);
      double tync = extractArrayEntry(rawFiducialArray, baseIndex + 2);
      double ta = extractArrayEntry(rawFiducialArray, baseIndex + 3);
      double distToCamera = extractArrayEntry(rawFiducialArray, baseIndex + 4);
      double distToRobot = extractArrayEntry(rawFiducialArray, baseIndex + 5);
      double ambiguity = extractArrayEntry(rawFiducialArray, baseIndex + 6);

      rawFiducials[i] = new RawFiducial(id, txnc, tync, ta, distToCamera, distToRobot, ambiguity);
    }

    return rawFiducials;
  }

  /**
   * Gets the latest raw neural detector results from NetworkTables.
   * Provides quick access to detection data including bounding box corners.
   * 
   * @param limelightName Name/identifier of the Limelight
   * @return Array of RawDetection objects containing detection details, or empty array if none
   */
  public static RawDetection[] getRawDetections(String limelightName) {
    var entry = LimelightHelpers.getLimelightNTTableEntry(limelightName, "rawdetections");
    var rawDetectionArray = entry.getDoubleArray(new double[0]);
    int valsPerEntry = 12;
    if (rawDetectionArray.length % valsPerEntry != 0) {
      return new RawDetection[0];
    }

    int numDetections = rawDetectionArray.length / valsPerEntry;
    RawDetection[] rawDetections = new RawDetection[numDetections];

    for (int i = 0; i < numDetections; i++) {
      int baseIndex = i * valsPerEntry; // Starting index for this detection's data
      int classId = (int) extractArrayEntry(rawDetectionArray, baseIndex);
      double txnc = extractArrayEntry(rawDetectionArray, baseIndex + 1);
      double tync = extractArrayEntry(rawDetectionArray, baseIndex + 2);
      double ta = extractArrayEntry(rawDetectionArray, baseIndex + 3);
      double corner0_X = extractArrayEntry(rawDetectionArray, baseIndex + 4);
      double corner0_Y = extractArrayEntry(rawDetectionArray, baseIndex + 5);
      double corner1_X = extractArrayEntry(rawDetectionArray, baseIndex + 6);
      double corner1_Y = extractArrayEntry(rawDetectionArray, baseIndex + 7);
      double corner2_X = extractArrayEntry(rawDetectionArray, baseIndex + 8);
      double corner2_Y = extractArrayEntry(rawDetectionArray, baseIndex + 9);
      double corner3_X = extractArrayEntry(rawDetectionArray, baseIndex + 10);
      double corner3_Y = extractArrayEntry(rawDetectionArray, baseIndex + 11);

      rawDetections[i] =
          new RawDetection(
              classId, txnc, tync, ta, corner0_X, corner0_Y, corner1_X, corner1_Y, corner2_X,
              corner2_Y, corner3_X, corner3_Y);
    }

    return rawDetections;
  }

  /**
   * Prints detailed information about a PoseEstimate to standard output.
   * Includes timestamp, latency, tag count, tag span, average tag distance,
   * average tag area, and detailed information about each detected fiducial.
   * 
   * <p>Useful for debugging and understanding pose estimate quality.
   * 
   * @param pose The PoseEstimate object to print. If null, prints "No PoseEstimate available."
   */
  public static void printPoseEstimate(PoseEstimate pose) {
    if (pose == null) {
      System.out.println("No PoseEstimate available.");
      return;
    }

    System.out.printf("Pose Estimate Information:%n");
    System.out.printf("Timestamp (Seconds): %.3f%n", pose.timestampSeconds);
    System.out.printf("Latency: %.3f ms%n", pose.latency);
    System.out.printf("Tag Count: %d%n", pose.tagCount);
    System.out.printf("Tag Span: %.2f meters%n", pose.tagSpan);
    System.out.printf("Average Tag Distance: %.2f meters%n", pose.avgTagDist);
    System.out.printf("Average Tag Area: %.2f%% of image%n", pose.avgTagArea);
    System.out.printf("Is MegaTag2: %b%n", pose.isMegaTag2);
    System.out.println();

    if (pose.rawFiducials == null || pose.rawFiducials.length == 0) {
      System.out.println("No RawFiducials data available.");
      return;
    }

    System.out.println("Raw Fiducials Details:");
    for (int i = 0; i < pose.rawFiducials.length; i++) {
      RawFiducial fiducial = pose.rawFiducials[i];
      System.out.printf(" Fiducial #%d:%n", i + 1);
      System.out.printf("  ID: %d%n", fiducial.id);
      System.out.printf("  TXNC: %.2f%n", fiducial.txnc);
      System.out.printf("  TYNC: %.2f%n", fiducial.tync);
      System.out.printf("  TA: %.2f%n", fiducial.ta);
      System.out.printf("  Distance to Camera: %.2f meters%n", fiducial.distToCamera);
      System.out.printf("  Distance to Robot: %.2f meters%n", fiducial.distToRobot);
      System.out.printf("  Ambiguity: %.2f%n", fiducial.ambiguity);
      System.out.println();
    }
  }

  /**
   * Validates whether a PoseEstimate contains usable data.
   * Checks that the pose is non-null and contains raw fiducial data.
   * 
   * @param pose The PoseEstimate to validate
   * @return True if the pose is non-null and contains raw fiducial data
   */
  public static Boolean validPoseEstimate(PoseEstimate pose) {
    return pose != null && pose.rawFiducials != null && pose.rawFiducials.length != 0;
  }

  /**
   * Gets the NetworkTable instance for a Limelight camera.
   * All Limelight data is published to a table named after the camera.
   * 
   * @param tableName Name/identifier of the Limelight
   * @return NetworkTable instance for the specified Limelight
   */
  public static NetworkTable getLimelightNTTable(String tableName) {
    return NetworkTableInstance.getDefault().getTable(sanitizeName(tableName));
  }

  /**
   * Flushes NetworkTables to ensure all queued updates are sent immediately.
   * Call this after setting multiple values to ensure they're sent together
   * and processed by the Limelight in the same update cycle.
   * 
   * <p>Particularly important when using SetRobotOrientation for MegaTag2.
   */
  public static void Flush() {
    NetworkTableInstance.getDefault().flush();
  }

  /**
   * Gets a specific NetworkTables entry for a Limelight camera.
   * 
   * @param tableName Name/identifier of the Limelight
   * @param entryName Name of the NetworkTables entry
   * @return NetworkTableEntry for the specified entry
   */
  public static NetworkTableEntry getLimelightNTTableEntry(String tableName, String entryName) {
    return getLimelightNTTable(tableName).getEntry(entryName);
  }

  /**
   * Gets a DoubleArrayEntry for a Limelight, using a cache to avoid recreating entries.
   * This improves performance by reusing entry objects across multiple calls.
   * 
   * @param tableName Name/identifier of the Limelight
   * @param entryName Name of the double array entry
   * @return Cached or newly created DoubleArrayEntry
   */
  public static DoubleArrayEntry getLimelightDoubleArrayEntry(String tableName, String entryName) {
    String key = tableName + "/" + entryName;
    return doubleArrayEntries.computeIfAbsent(
        key,
        k -> {
          NetworkTable table = getLimelightNTTable(tableName);
          return table.getDoubleArrayTopic(entryName).getEntry(new double[0]);
        });
  }

  /**
   * Gets a double value from NetworkTables for a Limelight entry.
   * 
   * @param tableName Name/identifier of the Limelight
   * @param entryName Name of the entry
   * @return Double value, or 0.0 if not found
   */
  public static double getLimelightNTDouble(String tableName, String entryName) {
    return getLimelightNTTableEntry(tableName, entryName).getDouble(0.0);
  }

  /**
   * Sets a double value in NetworkTables for a Limelight entry.
   * 
   * @param tableName Name/identifier of the Limelight
   * @param entryName Name of the entry
   * @param val Value to set
   */
  public static void setLimelightNTDouble(String tableName, String entryName, double val) {
    getLimelightNTTableEntry(tableName, entryName).setDouble(val);
  }

  /**
   * Sets a double array value in NetworkTables for a Limelight entry.
   * 
   * @param tableName Name/identifier of the Limelight
   * @param entryName Name of the entry
   * @param val Array of values to set
   */
  public static void setLimelightNTDoubleArray(String tableName, String entryName, double[] val) {
    getLimelightNTTableEntry(tableName, entryName).setDoubleArray(val);
  }

  /**
   * Gets a double array from NetworkTables for a Limelight entry.
   * 
   * @param tableName Name/identifier of the Limelight
   * @param entryName Name of the entry
   * @return Double array, or empty array if not found
   */
  public static double[] getLimelightNTDoubleArray(String tableName, String entryName) {
    return getLimelightNTTableEntry(tableName, entryName).getDoubleArray(new double[0]);
  }

  /**
   * Gets a string value from NetworkTables for a Limelight entry.
   * 
   * @param tableName Name/identifier of the Limelight
   * @param entryName Name of the entry
   * @return String value, or empty string if not found
   */
  public static String getLimelightNTString(String tableName, String entryName) {
    return getLimelightNTTableEntry(tableName, entryName).getString("");
  }

  /**
   * Gets a string array from NetworkTables for a Limelight entry.
   * 
   * @param tableName Name/identifier of the Limelight
   * @param entryName Name of the entry
   * @return String array, or empty array if not found
   */
  public static String[] getLimelightNTStringArray(String tableName, String entryName) {
    return getLimelightNTTableEntry(tableName, entryName).getStringArray(new String[0]);
  }

  /**
   * Constructs a URL for making HTTP requests to a Limelight camera.
   * Limelights expose an HTTP API on port 5807 for certain operations.
   * 
   * @param tableName Name/identifier of the Limelight
   * @param request The HTTP request path (e.g., "capturesnapshot")
   * @return URL object for the request, or null if malformed
   */
  public static URL getLimelightURLString(String tableName, String request) {
    String urlString = "http://" + sanitizeName(tableName) + ".local:5807/" + request;
    URL url;
    try {
      url = new URL(urlString);
      return url;
    } catch (MalformedURLException e) {
      System.err.println("bad LL URL");
    }
    return null;
  }

  /////
  ///// Basic Target Information Getters
  /////

  /**
   * Does the Limelight have a valid target?
   * 
   * @param limelightName Name of the Limelight camera ("" for default)
   * @return True if a valid target is present, false otherwise
   */
  public static boolean getTV(String limelightName) {
    return 1.0 == getLimelightNTDouble(limelightName, "tv");
  }

  /**
   * Gets the horizontal offset from the crosshair to the target in degrees.
   * Positive values indicate target is to the right of crosshair.
   * 
   * @param limelightName Name of the Limelight camera ("" for default)
   * @return Horizontal offset angle in degrees (typically -29.8 to 29.8)
   */
  public static double getTX(String limelightName) {
    return getLimelightNTDouble(limelightName, "tx");
  }

  /**
   * Gets the vertical offset from the crosshair to the target in degrees.
   * Positive values indicate target is above crosshair.
   * 
   * @param limelightName Name of the Limelight camera ("" for default)
   * @return Vertical offset angle in degrees (typically -24.85 to 24.85)
   */
  public static double getTY(String limelightName) {
    return getLimelightNTDouble(limelightName, "ty");
  }

  /**
   * Gets the horizontal offset from the principal pixel/point to the target in degrees.
   * This is the most accurate 2d metric if you are using a calibrated camera and
   * you don't need adjustable crosshair functionality.
   * 
   * @param limelightName Name of the Limelight camera ("" for default)
   * @return Horizontal offset angle in degrees from principal point
   */
  public static double getTXNC(String limelightName) {
    return getLimelightNTDouble(limelightName, "txnc");
  }

  /**
   * Gets the vertical offset from the principal pixel/point to the target in degrees.
   * This is the most accurate 2d metric if you are using a calibrated camera and
   * you don't need adjustable crosshair functionality.
   * 
   * @param limelightName Name of the Limelight camera ("" for default)
   * @return Vertical offset angle in degrees from principal point
   */
  public static double getTYNC(String limelightName) {
    return getLimelightNTDouble(limelightName, "tync");
  }

  /**
   * Gets the target area as a percentage of the image (0-100%).
   * Larger values indicate target is closer or larger in frame.
   * 
   * @param limelightName Name of the Limelight camera ("" for default)
   * @return Target area percentage (0-100)
   */
  public static double getTA(String limelightName) {
    return getLimelightNTDouble(limelightName, "ta");
  }

  /**
   * T2D is an array that contains several targeting metrics in one convenient package.
   * Array format: [targetValid, targetCount, targetLatency, captureLatency, tx, ty,
   * txnc, tync, ta, tid, targetClassIndexDetector, targetClassIndexClassifier,
   * targetLongSidePixels, targetShortSidePixels, targetHorizontalExtentPixels,
   * targetVerticalExtentPixels, targetSkewDegrees]
   * 
   * @param limelightName Name of the Limelight camera
   * @return 17-element array containing comprehensive targeting data
   */
  public static double[] getT2DArray(String limelightName) {
    return getLimelightNTDoubleArray(limelightName, "t2d");
  }

  /**
   * Gets the number of targets currently detected by the active pipeline.
   * 
   * @param limelightName Name of the Limelight camera
   * @return Number of detected targets
   */
  public static int getTargetCount(String limelightName) {
    double[] t2d = getT2DArray(limelightName);
    if (t2d.length == 17) {
      return (int) t2d[1];
    }
    return 0;
  }

  /**
   * Gets the classifier class index from the currently running neural classifier pipeline.
   * 
   * @param limelightName Name of the Limelight camera
   * @return Class index from classifier pipeline
   */
  public static int getClassifierClassIndex(String limelightName) {
    double[] t2d = getT2DArray(limelightName);
    if (t2d.length == 17) {
      return (int) t2d[10];
    }
    return 0;
  }

  /**
   * Gets the detector class index from the primary result of the currently running
   * neural detector pipeline.
   * 
   * @param limelightName Name of the Limelight camera
   * @return Class index from detector pipeline
   */
  public static int getDetectorClassIndex(String limelightName) {
    double[] t2d = getT2DArray(limelightName);
    if (t2d.length == 17) {
      return (int) t2d[11];
    }
    return 0;
  }

  /**
   * Gets the current neural classifier result class name.
   * 
   * @param limelightName Name of the Limelight camera
   * @return Class name string from classifier pipeline (e.g., "cone", "cube")
   */
  public static String getClassifierClass(String limelightName) {
    return getLimelightNTString(limelightName, "tcclass");
  }

  /**
   * Gets the primary neural detector result class name.
   * 
   * @param limelightName Name of the Limelight camera
   * @return Class name string from detector pipeline (e.g., "cone", "cube")
   */
  public static String getDetectorClass(String limelightName) {
    return getLimelightNTString(limelightName, "tdclass");
  }

  /**
   * Gets the pipeline's processing latency contribution.
   * This is the time spent processing the image through the vision pipeline.
   * 
   * @param limelightName Name of the Limelight camera
   * @return Pipeline latency in milliseconds
   */
  public static double getLatency_Pipeline(String limelightName) {
    return getLimelightNTDouble(limelightName, "tl");
  }

  /**
   * Gets the capture latency.
   * This is the time between when the frame was captured and when processing began.
   * 
   * @param limelightName Name of the Limelight camera
   * @return Capture latency in milliseconds
   */
  public static double getLatency_Capture(String limelightName) {
    return getLimelightNTDouble(limelightName, "cl");
  }

  /**
   * Gets the active pipeline index.
   * 
   * @param limelightName Name of the Limelight camera
   * @return Current pipeline index (0-9)
   */
  public static double getCurrentPipelineIndex(String limelightName) {
    return getLimelightNTDouble(limelightName, "getpipe");
  }

  /**
   * Gets the current pipeline type string.
   * 
   * @param limelightName Name of the Limelight camera
   * @return Pipeline type string (e.g. "retro", "apriltag", "neural_detector")
   */
  public static String getCurrentPipelineType(String limelightName) {
    return getLimelightNTString(limelightName, "getpipetype");
  }

  /**
   * Gets the full JSON results dump from the Limelight.
   * Contains all targeting data in JSON format for advanced parsing.
   * 
   * @param limelightName Name of the Limelight camera
   * @return JSON string containing all current results
   */
  public static String getJSONDump(String limelightName) {
    return getLimelightNTString(limelightName, "json");
  }

  /////
  ///// Pose Getters (Raw Arrays)
  /////

  /**
   * Gets robot pose array in Limelight coordinate system.
   * 
   * @deprecated Use {@link #getBotPose(String)} instead
   * @param limelightName Name of the Limelight camera
   * @return 6-element array [x, y, z, roll, pitch, yaw]
   */
  @Deprecated
  public static double[] getBotpose(String limelightName) {
    return getLimelightNTDoubleArray(limelightName, "botpose");
  }

  /**
   * Gets robot pose array in WPILib red alliance coordinates.
   * 
   * @deprecated Use {@link #getBotPose_wpiRed(String)} instead
   * @param limelightName Name of the Limelight camera
   * @return 6-element array [x, y, z, roll, pitch, yaw]
   */
  @Deprecated
  public static double[] getBotpose_wpiRed(String limelightName) {
    return getLimelightNTDoubleArray(limelightName, "botpose_wpired");
  }

  /**
   * Gets robot pose array in WPILib blue alliance coordinates.
   * 
   * @deprecated Use {@link #getBotPose_wpiBlue(String)} instead
   * @param limelightName Name of the Limelight camera
   * @return 6-element array [x, y, z, roll, pitch, yaw]
   */
  @Deprecated
  public static double[] getBotpose_wpiBlue(String limelightName) {
    return getLimelightNTDoubleArray(limelightName, "botpose_wpiblue");
  }

  /**
   * Gets robot pose array in Limelight coordinate system.
   * 
   * @param limelightName Name of the Limelight camera
   * @return 6-element array [x, y, z, roll, pitch, yaw] in meters and degrees
   */
  public static double[] getBotPose(String limelightName) {
    return getLimelightNTDoubleArray(limelightName, "botpose");
  }

  /**
   * Gets robot pose array in WPILib red alliance coordinate system.
   * Origin at red alliance driver station wall.
   * 
   * @param limelightName Name of the Limelight camera
   * @return 6-element array [x, y, z, roll, pitch, yaw] in meters and degrees
   */
  public static double[] getBotPose_wpiRed(String limelightName) {
    return getLimelightNTDoubleArray(limelightName, "botpose_wpired");
  }

  /**
   * Gets robot pose array in WPILib blue alliance coordinate system.
   * Origin at blue alliance driver station wall. Recommended for most use cases.
   * 
   * @param limelightName Name of the Limelight camera
   * @return 6-element array [x, y, z, roll, pitch, yaw] in meters and degrees
   */
  public static double[] getBotPose_wpiBlue(String limelightName) {
    return getLimelightNTDoubleArray(limelightName, "botpose_wpiblue");
  }

  /**
   * Gets robot pose in target space as an array.
   * Represents where the robot is relative to the detected target.
   * 
   * @param limelightName Name of the Limelight camera
   * @return 6-element array [x, y, z, roll, pitch, yaw] in meters and degrees
   */
  public static double[] getBotPose_TargetSpace(String limelightName) {
    return getLimelightNTDoubleArray(limelightName, "botpose_targetspace");
  }

  /**
   * Gets camera pose in target space as an array.
   * Represents where the camera is relative to the detected target.
   * 
   * @param limelightName Name of the Limelight camera
   * @return 6-element array [x, y, z, roll, pitch, yaw] in meters and degrees
   */
  public static double[] getCameraPose_TargetSpace(String limelightName) {
    return getLimelightNTDoubleArray(limelightName, "camerapose_targetspace");
  }

  /**
   * Gets target pose in camera space as an array.
   * Represents where the target is relative to the camera.
   * 
   * @param limelightName Name of the Limelight camera
   * @return 6-element array [x, y, z, roll, pitch, yaw] in meters and degrees
   */
  public static double[] getTargetPose_CameraSpace(String limelightName) {
    return getLimelightNTDoubleArray(limelightName, "targetpose_cameraspace");
  }

  /**
   * Gets target pose in robot space as an array.
   * Represents where the target is relative to the robot center.
   * 
   * @param limelightName Name of the Limelight camera
   * @return 6-element array [x, y, z, roll, pitch, yaw] in meters and degrees
   */
  public static double[] getTargetPose_RobotSpace(String limelightName) {
    return getLimelightNTDoubleArray(limelightName, "targetpose_robotspace");
  }

  /**
   * Gets the RGB color values detected by the color pipeline.
   * Useful for color-based target identification.
   * 
   * @param limelightName Name of the Limelight camera
   * @return Array of color values [R, G, B] (0-255)
   */
  public static double[] getTargetColor(String limelightName) {
    return getLimelightNTDoubleArray(limelightName, "tc");
  }

  /**
   * Gets the current pipeline's fiducial ID of the primary target.
   * 
   * @param limelightName Name of the Limelight camera
   * @return Fiducial/AprilTag ID number
   */
  public static double getFiducialID(String limelightName) {
    return getLimelightNTDouble(limelightName, "tid");
  }

  /**
   * Gets the neural network classifier class ID string.
   * 
   * @param limelightName Name of the Limelight camera
   * @return Class name string from the neural classifier
   */
  public static String getNeuralClassID(String limelightName) {
    return getLimelightNTString(limelightName, "tclass");
  }

  /**
   * Gets raw barcode data as a string array.
   * Each element contains the decoded string from a detected barcode.
   * 
   * @param limelightName Name of the Limelight camera
   * @return Array of decoded barcode strings
   */
  public static String[] getRawBarcodeData(String limelightName) {
    return getLimelightNTStringArray(limelightName, "rawbarcodes");
  }

  /////
  ///// Pose Getters (Pose3d Objects)
  /////

  /**
   * Gets the robot's 3D pose in field space using Limelight's internal coordinate system.
   * 
   * <p>This method retrieves the robot's position and orientation as calculated by the Limelight
   * based on AprilTag detections and the known field layout. The pose is in Limelight's native
   * coordinate system.
   * 
   * <p><b>Note:</b> For WPILib integration, prefer {@link #getBotPose3d_wpiBlue(String)} which
   * uses WPILib's standard blue alliance coordinate system.
   * 
   * @param limelightName Name/identifier of the Limelight camera (use "" for default "limelight")
   * @return Pose3d object representing robot position (meters) and orientation (radians)
   */
  public static Pose3d getBotPose3d(String limelightName) {
    // Retrieve the 6-element pose array from NetworkTables
    double[] poseArray = getLimelightNTDoubleArray(limelightName, "botpose");
    // Convert array format [x, y, z, roll, pitch, yaw] to Pose3d object
    return toPose3D(poseArray);
  }

  /**
   * Gets the robot's 3D pose in the WPILib Red Alliance Coordinate System.
   * 
   * <p><b>Not Recommended:</b> Red alliance coordinates can be confusing as field orientation
   * changes between alliances. Use {@link #getBotPose3d_wpiBlue(String)} instead for consistency.
   * 
   * <p>In the red alliance coordinate system:
   * <ul>
   *   <li>Origin (0,0) is at the red alliance driver station wall</li>
   *   <li>+X extends toward the blue alliance wall</li>
   *   <li>+Y extends to the left when facing the blue alliance wall</li>
   *   <li>Yaw of 0° means robot is facing the blue alliance wall</li>
   * </ul>
   * 
   * @param limelightName Name/identifier of the Limelight camera
   * @return Pose3d in red alliance field space (meters and radians)
   */
  public static Pose3d getBotPose3d_wpiRed(String limelightName) {
    // Retrieve red alliance pose from NetworkTables
    double[] poseArray = getLimelightNTDoubleArray(limelightName, "botpose_wpired");
    return toPose3D(poseArray);
  }

  /**
   * Gets the robot's 3D pose in the WPILib Blue Alliance Coordinate System.
   * 
   * <p><b>Recommended:</b> This is the standard coordinate system for WPILib pose estimation
   * and should be used with {@code SwerveDrivePoseEstimator.addVisionMeasurement()} or
   * {@code DifferentialDrivePoseEstimator.addVisionMeasurement()}.
   * 
   * <p>In the blue alliance coordinate system:
   * <ul>
   *   <li>Origin (0,0) is at the blue alliance driver station wall</li>
   *   <li>+X extends toward the red alliance wall</li>
   *   <li>+Y extends to the left when facing the red alliance wall</li>
   *   <li>Yaw of 0° means robot is facing the red alliance wall</li>
   * </ul>
   * 
   * <p><b>Example usage:</b>
   * <pre>{@code
   * Pose3d robotPose = LimelightHelpers.getBotPose3d_wpiBlue("limelight");
   * // Convert to 2D for odometry: robotPose.toPose2d()
   * }</pre>
   *
   * @param limelightName Name/identifier of the Limelight camera
   * @return Pose3d in blue alliance field space (meters and radians)
   */
  public static Pose3d getBotPose3d_wpiBlue(String limelightName) {
    // Retrieve blue alliance pose from NetworkTables
    double[] poseArray = getLimelightNTDoubleArray(limelightName, "botpose_wpiblue");
    return toPose3D(poseArray);
  }

  /**
   * Gets the robot's 3D pose relative to the currently tracked target's coordinate system.
   * 
   * <p>This provides the robot's position and orientation with respect to the detected target
   * (AprilTag or other fiducial). Useful for target-relative navigation where you want to
   * approach or align with a specific target.
   * 
   * <p>In target space:
   * <ul>
   *   <li>Origin (0,0,0) is at the target's center</li>
   *   <li>Coordinate axes depend on target orientation</li>
   *   <li>For AprilTags: +Z extends out from tag face, +X is right, +Y is down</li>
   * </ul>
   * 
   * <p><b>Example usage:</b>
   * <pre>{@code
   * // Get pose relative to detected AprilTag
   * Pose3d robotToTag = LimelightHelpers.getBotPose3d_TargetSpace("limelight");
   * double distanceToTag = robotToTag.getTranslation().getNorm();
   * }</pre>
   *
   * @param limelightName Name/identifier of the Limelight camera
   * @return Pose3d representing robot position/orientation relative to the target
   */
  public static Pose3d getBotPose3d_TargetSpace(String limelightName) {
    // Retrieve target-space pose from NetworkTables
    double[] poseArray = getLimelightNTDoubleArray(limelightName, "botpose_targetspace");
    return toPose3D(poseArray);
  }

  /**
   * Gets the camera's 3D pose relative to the currently tracked target's coordinate system.
   * 
   * <p>This provides the camera's position and orientation with respect to the detected target.
   * Different from {@link #getBotPose3d_TargetSpace(String)} in that it gives the camera
   * position rather than the robot center position.
   * 
   * <p>Useful for:
   * <ul>
   *   <li>Verifying camera calibration and mounting</li>
   *   <li>Direct camera-to-target distance calculations</li>
   *   <li>Debugging vision system setup</li>
   * </ul>
   *
   * @param limelightName Name/identifier of the Limelight camera
   * @return Pose3d representing camera position/orientation relative to the target
   */
  public static Pose3d getCameraPose3d_TargetSpace(String limelightName) {
    // Retrieve camera-to-target pose from NetworkTables
    double[] poseArray = getLimelightNTDoubleArray(limelightName, "camerapose_targetspace");
    return toPose3D(poseArray);
  }

  /**
   * Gets the target's 3D pose relative to the camera's coordinate system.
   * 
   * <p>This provides where the target is located from the camera's perspective. Useful for
   * direct targeting calculations where you want to point the camera (and therefore robot)
   * at a specific target.
   * 
   * <p>In camera space:
   * <ul>
   *   <li>Origin (0,0,0) is at the camera's optical center</li>
   *   <li>+Z extends forward (into the scene)</li>
   *   <li>+X extends right, +Y extends down (standard camera coordinates)</li>
   * </ul>
   * 
   * <p><b>Example usage:</b>
   * <pre>{@code
   * Pose3d targetPose = LimelightHelpers.getTargetPose3d_CameraSpace("limelight");
   * double targetDistance = targetPose.getTranslation().getZ(); // Forward distance
   * double targetAngle = Math.atan2(targetPose.getTranslation().getX(), 
   *                                   targetPose.getTranslation().getZ());
   * }</pre>
   *
   * @param limelightName Name/identifier of the Limelight camera
   * @return Pose3d representing target position/orientation relative to the camera
   */
  public static Pose3d getTargetPose3d_CameraSpace(String limelightName) {
    // Retrieve target-in-camera-space pose from NetworkTables
    double[] poseArray = getLimelightNTDoubleArray(limelightName, "targetpose_cameraspace");
    return toPose3D(poseArray);
  }

  /**
   * Gets the target's 3D pose relative to the robot's coordinate system.
   * 
   * <p>This provides where the target is located from the robot center's perspective,
   * accounting for the camera's mounting position on the robot. This is the most useful
   * target pose for robot-centric navigation and control.
   * 
   * <p>In robot space:
   * <ul>
   *   <li>Origin (0,0,0) is at the robot's center (as configured)</li>
   *   <li>+X extends forward from robot front</li>
   *   <li>+Y extends left, +Z extends up</li>
   * </ul>
   * 
   * <p><b>Example usage:</b>
   * <pre>{@code
   * Pose3d targetPose = LimelightHelpers.getTargetPose3d_RobotSpace("limelight");
   * Translation3d targetTranslation = targetPose.getTranslation();
   * 
   * // Calculate angle to target for turret control
   * double angleToTarget = Math.atan2(targetTranslation.getY(), 
   *                                     targetTranslation.getX());
   * // Calculate distance for shooter velocity
   * double distanceToTarget = targetTranslation.getNorm();
   * }</pre>
   *
   * @param limelightName Name/identifier of the Limelight camera
   * @return Pose3d representing target position/orientation relative to robot center
   */
  public static Pose3d getTargetPose3d_RobotSpace(String limelightName) {
    // Retrieve target-in-robot-space pose from NetworkTables
    double[] poseArray = getLimelightNTDoubleArray(limelightName, "targetpose_robotspace");
    return toPose3D(poseArray);
  }

  /**
   * Gets the camera's 3D pose relative to the robot's coordinate system.
   * 
   * <p>This represents where the camera is mounted on the robot. This value should match
   * your camera's physical mounting position and is used internally for transforming
   * between camera space and robot space.
   * 
   * <p>Useful for:
   * <ul>
   *   <li>Verifying camera mounting configuration</li>
   *   <li>Debugging coordinate transformations</li>
   *   <li>Custom pose estimation calculations</li>
   * </ul>
   * 
   * <p><b>Note:</b> This can be configured via {@link #setCameraPose_RobotSpace(String, double, 
   * double, double, double, double, double)} or through the Limelight web interface.
   *
   * @param limelightName Name/identifier of the Limelight camera
   * @return Pose3d representing camera mounting position/orientation relative to robot center
   */
  public static Pose3d getCameraPose3d_RobotSpace(String limelightName) {
    // Retrieve camera mounting pose from NetworkTables
    double[] poseArray = getLimelightNTDoubleArray(limelightName, "camerapose_robotspace");
    return toPose3D(poseArray);
  }

  /////
  ///// Pose Getters (Pose2d Objects) - For Odometry Integration
  /////

  /**
   * Gets the robot's 2D pose in WPILib blue alliance coordinates.
   * 
   * <p>This is a convenience method that extracts just the x, y, and yaw from the full 3D pose,
   * making it ready for direct use with WPILib's 2D odometry and pose estimation systems.
   * 
   * <p><b>Recommended for:</b> Direct integration with WPILib pose estimators
   * 
   * <p><b>Example usage:</b>
   * <pre>{@code
   * // Simple approach - just get the pose
   * Pose2d visionPose = LimelightHelpers.getBotPose2d_wpiBlue("limelight");
   * }</pre>
   *
   * @param limelightName Name/identifier of the Limelight camera
   * @return Pose2d in blue alliance coordinates (meters and radians)
   */
  public static Pose2d getBotPose2d_wpiBlue(String limelightName) {
    // Get the raw pose array (includes 3D data)
    double[] result = getBotPose_wpiBlue(limelightName);
    // Convert to 2D, discarding z, roll, pitch
    return toPose2D(result);
  }

  /**
   * Gets the MegaTag1 2D pose estimate with timestamp and quality metrics for WPILib blue alliance.
   * 
   * <p><b>MegaTag1</b> uses multiple AprilTags simultaneously to compute a more accurate robot
   * pose. This method returns not just the pose, but also timing and quality information needed
   * for optimal pose estimator integration.
   * 
   * <p>The returned {@link PoseEstimate} includes:
   * <ul>
   *   <li>Robot pose (Pose2d)</li>
   *   <li>FPGA timestamp for proper temporal alignment</li>
   *   <li>Total latency (capture + processing)</li>
   *   <li>Number of tags used (more is better)</li>
   *   <li>Tag span (wider spacing is better)</li>
   *   <li>Average tag distance and area (quality metrics)</li>
   * </ul>
   * 
   * <p><b>Example usage with pose estimator:</b>
   * <pre>{@code
   * PoseEstimate estimate = LimelightHelpers.getBotPoseEstimate_wpiBlue("limelight");
   * 
   * if (LimelightHelpers.validPoseEstimate(estimate)) {
   *     // Adjust standard deviations based on tag count and distance
   *     Matrix<N3, N1> stdDevs = VecBuilder.fill(
   *         0.7 / estimate.tagCount,  // X std dev
   *         0.7 / estimate.tagCount,  // Y std dev  
   *         9999999                    // Theta std dev (trust gyro more)
   *     );
   *     
   *     poseEstimator.addVisionMeasurement(
   *         estimate.pose,
   *         estimate.timestampSeconds,
   *         stdDevs
   *     );
   * }
   * }</pre>
   *
   * @param limelightName Name/identifier of the Limelight camera
   * @return PoseEstimate with pose, timestamp, and quality metrics, or null if no data available
   */
  public static PoseEstimate getBotPoseEstimate_wpiBlue(String limelightName) {
    // Get pose estimate from the standard botpose_wpiblue entry
    return getBotPoseEstimate(limelightName, "botpose_wpiblue", false);
  }

  /**
   * Gets the MegaTag2 2D pose estimate with timestamp and quality metrics for WPILib blue alliance.
   * 
   * <p><b>MegaTag2</b> is an advanced localization algorithm that incorporates robot orientation
   * data (from your gyro/IMU) to improve pose accuracy. This can significantly reduce pose jumps
   * and improve overall reliability.
   * 
   * <p><b>CRITICAL:</b> You MUST call {@link #SetRobotOrientation(String, double, double, double,
   * double, double, double)} or {@link #SetRobotOrientation_NoFlush(String, double, double, double,
   * double, double, double)} before calling this method in your periodic loop. MegaTag2 requires
   * current robot orientation to function correctly.
   * 
   * <p><b>Advantages of MegaTag2:</b>
   * <ul>
   *   <li>More stable pose estimates with less jumping</li>
   *   <li>Better disambiguation of pose solutions</li>
   *   <li>Improved accuracy, especially at longer distances</li>
   *   <li>Better handling of partial tag visibility</li>
   * </ul>
   * 
   * <p><b>Example usage:</b>
   * <pre>{@code
   * // In your subsystem's periodic() method:
   * 
   * // 1. Update robot orientation (required for MegaTag2)
   * LimelightHelpers.SetRobotOrientation(
   *     "limelight",
   *     m_gyro.getYaw(),           // Robot yaw
   *     m_gyro.getRate(),          // Yaw rate (optional but helpful)
   *     0, 0, 0, 0                 // Pitch and roll (usually not needed)
   * );
   * 
   * // 2. Get MegaTag2 pose estimate
   * PoseEstimate estimate = LimelightHelpers.getBotPoseEstimate_wpiBlue_MegaTag2("limelight");
   * 
   * // 3. Use with pose estimator
   * if (LimelightHelpers.validPoseEstimate(estimate)) {
   *     // Calculate dynamic standard deviations based on quality
   *     double xyStdDev = 0.5;
   *     if (estimate.tagCount >= 2) xyStdDev = 0.3;
   *     if (estimate.avgTagDist > 4.0) xyStdDev *= 2;
   *     
   *     poseEstimator.addVisionMeasurement(
   *         estimate.pose,
   *         estimate.timestampSeconds,
   *         VecBuilder.fill(xyStdDev, xyStdDev, 9999999)
   *     );
   * }
   * }</pre>
   *
   * @param limelightName Name/identifier of the Limelight camera
   * @return PoseEstimate with pose, timestamp, and quality metrics, or null if no data available
   * @see #SetRobotOrientation(String, double, double, double, double, double, double)
   */
  public static PoseEstimate getBotPoseEstimate_wpiBlue_MegaTag2(String limelightName) {
    // Get pose estimate from the MegaTag2 (ORB = Orientation-based Robot Localization) entry
    return getBotPoseEstimate(limelightName, "botpose_orb_wpiblue", true);
  }

  /**
   * Gets the robot's 2D pose in WPILib red alliance coordinates.
   * 
   * <p><b>Not Recommended:</b> Use {@link #getBotPose2d_wpiBlue(String)} instead for consistency.
   * 
   * @param limelightName Name/identifier of the Limelight camera
   * @return Pose2d in red alliance coordinates (meters and radians)
   */
  public static Pose2d getBotPose2d_wpiRed(String limelightName) {
    // Get raw red alliance pose array
    double[] result = getBotPose_wpiRed(limelightName);
    // Convert to 2D
    return toPose2D(result);
  }

  /**
   * Gets a basic pose estimate in Limelight's internal coordinate system.
   * 
   * <p>For most FRC applications, use {@link #getBotPoseEstimate_wpiBlue(String)} instead
   * which provides the pose in WPILib's standard blue alliance coordinates.
   * 
   * @param limelightName Name/identifier of the Limelight camera
   * @return PoseEstimate in Limelight coordinates, or null if no data available
   */
  public static PoseEstimate getBotPoseEstimate(String limelightName) {
    // Get pose estimate from the standard botpose entry (Limelight coordinates)
    return getBotPoseEstimate(limelightName, "botpose", false);
  }

  /**
   * Gets the MegaTag1 pose estimate for red alliance coordinates.
   * 
   * <p><b>Not Recommended:</b> Use {@link #getBotPoseEstimate_wpiBlue(String)} instead.
   *
   * @param limelightName Name/identifier of the Limelight camera
   * @return PoseEstimate in red alliance coordinates, or null if no data available
   */
  public static PoseEstimate getBotPoseEstimate_wpiRed(String limelightName) {
    // Get pose estimate from red alliance entry
    return getBotPoseEstimate(limelightName, "botpose_wpired", false);
  }

  /**
   * Gets the MegaTag2 pose estimate for red alliance coordinates.
   * 
   * <p><b>Not Recommended:</b> Use {@link #getBotPoseEstimate_wpiBlue_MegaTag2(String)} instead.
   * 
   * <p><b>CRITICAL:</b> You MUST call {@link #SetRobotOrientation(String, double, double, double,
   * double, double, double)} before using this method.
   *
   * @param limelightName Name/identifier of the Limelight camera
   * @return PoseEstimate in red alliance coordinates, or null if no data available
   */
  public static PoseEstimate getBotPoseEstimate_wpiRed_MegaTag2(String limelightName) {
    // Get MegaTag2 pose estimate from red alliance ORB entry
    return getBotPoseEstimate(limelightName, "botpose_orb_wpired", true);
  }

  /**
   * Gets the robot's 2D pose in Limelight's internal coordinate system.
   * 
   * <p>For WPILib integration, prefer {@link #getBotPose2d_wpiBlue(String)} instead.
   *
   * @param limelightName Name/identifier of the Limelight camera
   * @return Pose2d in Limelight coordinates (meters and radians)
   */
  public static Pose2d getBotPose2d(String limelightName) {
    // Get raw pose array in Limelight coordinates
    double[] result = getBotPose(limelightName);
    // Convert to 2D
    return toPose2D(result);
  }

  /////
  ///// IMU Data
  /////

  /**
   * Gets the current IMU (Inertial Measurement Unit) data from the Limelight.
   * 
   * <p>Limelight 3 and newer models include a built-in 6-axis IMU that provides orientation
   * and motion data. This IMU can be used for robot localization, particularly with MegaTag2.
   * 
   * <p>The IMU provides:
   * <ul>
   *   <li><b>Orientation:</b> Roll, pitch, and yaw angles</li>
   *   <li><b>Gyroscope:</b> Angular velocities around X, Y, Z axes</li>
   *   <li><b>Accelerometer:</b> Linear accelerations along X, Y, Z axes</li>
   * </ul>
   * 
   * <p><b>Coordinate System:</b>
   * <ul>
   *   <li>X: Right (from camera perspective)</li>
   *   <li>Y: Down</li>
   *   <li>Z: Forward (into scene)</li>
   * </ul>
   * 
   * <p><b>Units:</b>
   * <ul>
   *   <li>Angles: Degrees</li>
   *   <li>Angular velocities: Degrees per second</li>
   *   <li>Accelerations: G's (1G = 9.81 m/s²)</li>
   * </ul>
   * 
   * <p><b>Example usage:</b>
   * <pre>{@code
   * IMUData imu = LimelightHelpers.getIMUData("limelight");
   * 
   * // Get robot heading
   * double heading = imu.robotYaw;
   * 
   * // Get angular velocity for velocity tracking
   * double turnRate = imu.gyroZ;
   * 
   * // Detect high acceleration events
   * double totalAccel = Math.sqrt(
   *     imu.accelX * imu.accelX + 
   *     imu.accelY * imu.accelY + 
   *     imu.accelZ * imu.accelZ
   * );
   * }</pre>
   * 
   * <p><b>Note:</b> Returns an IMUData object with all zeros if data is invalid or unavailable.
   * Always check if your Limelight model supports IMU before relying on this data.
   *
   * @param limelightName Name/identifier of the Limelight camera
   * @return IMUData object containing all current IMU measurements, or all zeros if unavailable
   */
  public static IMUData getIMUData(String limelightName) {
    // Retrieve IMU array from NetworkTables
    // Format: [robotYaw, Roll, Pitch, Yaw, gyroX, gyroY, gyroZ, accelX, accelY, accelZ]
    double[] imuData = getLimelightNTDoubleArray(limelightName, "imu");
    
    // Validate array length (should be 10 elements)
    if (imuData == null || imuData.length < 10) {
      // Return zeroed IMUData if data is missing or invalid
      return new IMUData();
    }
    
    // Parse array into IMUData object
    return new IMUData(imuData);
  }

  /////
  ///// Camera Control Methods
  /////

  /**
   * Sets the active vision pipeline.
   * 
   * <p>Limelight supports up to 10 pipelines (0-9), each with different vision processing
   * settings. Use this to switch between different tracking modes during a match.
   * 
   * <p><b>Common pipeline uses:</b>
   * <ul>
   *   <li>Pipeline 0: AprilTag tracking for localization</li>
   *   <li>Pipeline 1: Retroreflective target tracking</li>
   *   <li>Pipeline 2: Neural network detector for game pieces</li>
   * </ul>
   * 
   * <p><b>Example usage:</b>
   * <pre>{@code
   * // Switch to AprilTag pipeline during auto
   * LimelightHelpers.setPipelineIndex("limelight", 0);
   * 
   * // Switch to game piece detector during teleop
   * LimelightHelpers.setPipelineIndex("limelight", 2);
   * }</pre>
   *
   * @param limelightName Name of the Limelight camera
   * @param pipelineIndex Pipeline index to activate (0-9)
   */
  public static void setPipelineIndex(String limelightName, int pipelineIndex) {
    setLimelightNTDouble(limelightName, "pipeline", pipelineIndex);
  }

  /**
   * Sets the priority AprilTag ID for tracking.
   * 
   * <p>When multiple AprilTags are visible, setting a priority ID tells the Limelight to
   * prefer that specific tag for targeting calculations. The primary target will be the
   * priority tag if it's visible, otherwise it falls back to normal sorting.
   * 
   * <p><b>Use cases:</b>
   * <ul>
   *   <li>Targeting a specific scoring location</li>
   *   <li>Prioritizing closer/more relevant field elements</li>
   *   <li>Improving localization with strategically placed tags</li>
   * </ul>
   * 
   * <p><b>Example usage:</b>
   * <pre>{@code
   * // Target tag #4 (e.g., speaker center tag)
   * LimelightHelpers.setPriorityTagID("limelight", 4);
   * 
   * // Clear priority (set to -1 or use any invalid ID)
   * LimelightHelpers.setPriorityTagID("limelight", -1);
   * }</pre>
   *
   * @param limelightName Name of the Limelight camera
   * @param ID AprilTag ID to prioritize, or -1 to clear priority
   */
  public static void setPriorityTagID(String limelightName, int ID) {
    setLimelightNTDouble(limelightName, "priorityid", ID);
  }

  /////
  ///// LED Control Methods
  /////

  /**
   * Sets LED mode to be controlled by the current pipeline's configuration.
   * 
   * <p>Each pipeline can have its own LED mode setting configured in the Limelight web interface.
   * This is the default and recommended mode - let each pipeline control its own LEDs based on
   * what tracking mode is active.
   * 
   * <p><b>Example:</b> Your AprilTag pipeline might have LEDs off, while your retroreflective
   * pipeline has them on.
   *
   * @param limelightName Name of the Limelight camera
   */
  public static void setLEDMode_PipelineControl(String limelightName) {
    // ledMode 0 = pipeline control
    setLimelightNTDouble(limelightName, "ledMode", 0);
  }

  /**
   * Forces the LEDs off regardless of pipeline settings.
   * 
   * <p>Useful for conserving power, reducing visual distractions, or when LEDs aren't needed
   * (e.g., when using AprilTags or neural networks that don't require illumination).
   * 
   * <p><b>Example usage:</b>
   * <pre>{@code
   * // Turn off LEDs during auto when using AprilTags
   * LimelightHelpers.setLEDMode_ForceOff("limelight");
   * }</pre>
   *
   * @param limelightName Name of the Limelight camera
   */
  public static void setLEDMode_ForceOff(String limelightName) {
    // ledMode 1 = force off
    setLimelightNTDouble(limelightName, "ledMode", 1);
  }

  /**
   * Forces the LEDs to blink regardless of pipeline settings.
   * 
   * <p>Creates a blinking pattern useful for visual feedback or signaling. Can be used to
   * indicate robot states, warn drivers, or debug vision system operation.
   * 
   * <p><b>Example usage:</b>
   * <pre>{@code
   * // Blink LEDs when target is acquired
   * if (LimelightHelpers.getTV("limelight")) {
   *     LimelightHelpers.setLEDMode_ForceBlink("limelight");
   * }
   * }</pre>
   *
   * @param limelightName Name of the Limelight camera
   */
  public static void setLEDMode_ForceBlink(String limelightName) {
    // ledMode 2 = force blink
    setLimelightNTDouble(limelightName, "ledMode", 2);
  }

  /**
   * Forces the LEDs on at full brightness regardless of pipeline settings.
   * 
   * <p>Ensures LEDs are always on, overriding pipeline configuration. Necessary for retroreflective
   * tracking where consistent illumination is critical for reliable target detection.
   * 
   * <p><b>Example usage:</b>
   * <pre>{@code
   * // Ensure LEDs are on for retroreflective tape tracking
   * LimelightHelpers.setLEDMode_ForceOn("limelight");
   * }</pre>
   *
   * @param limelightName Name of the Limelight camera
   */
  public static void setLEDMode_ForceOn(String limelightName) {
    // ledMode 3 = force on
    setLimelightNTDouble(limelightName, "ledMode", 3);
  }

  /////
  ///// Stream Control Methods
  /////

  /**
   * Enables standard side-by-side stream mode.
   * 
   * <p>Displays both the primary and secondary camera streams (or processing views) side by side.
   * This is the default streaming mode and is useful for seeing both the raw camera feed and
   * the processed output simultaneously.
   * 
   * <p><b>Stream modes:</b>
   * <ul>
   *   <li>Standard: Side-by-side view of both streams</li>
   *   <li>PiP Main: Secondary stream in corner of primary</li>
   *   <li>PiP Secondary: Primary stream in corner of secondary</li>
   * </ul>
   *
   * @param limelightName Name of the Limelight camera
   */
  public static void setStreamMode_Standard(String limelightName) {
    // stream 0 = standard side-by-side
    setLimelightNTDouble(limelightName, "stream", 0);
  }

  /**
   * Enables Picture-in-Picture mode with the secondary stream displayed in the corner.
   * 
   * <p>Shows the primary stream in full view with the secondary stream as a small overlay
   * in the corner. Useful when you want to focus on one view but still monitor the other.
   *
   * @param limelightName Name of the Limelight camera
   */
  public static void setStreamMode_PiPMain(String limelightName) {
    // stream 1 = PiP with secondary in corner
    setLimelightNTDouble(limelightName, "stream", 1);
  }

  /**
   * Enables Picture-in-Picture mode with the primary stream displayed in the corner.
   * 
   * <p>Shows the secondary stream in full view with the primary stream as a small overlay
   * in the corner. Useful for monitoring processed output while viewing raw camera feed.
   *
   * @param limelightName Name of the Limelight camera
   */
  public static void setStreamMode_PiPSecondary(String limelightName) {
    // stream 2 = PiP with primary in corner
    setLimelightNTDouble(limelightName, "stream", 2);
  }

  /////
  ///// Advanced Camera Configuration
  /////

  /**
   * Sets a dynamic crop window for the camera to limit the processing region.
   * 
   * <p>Cropping restricts vision processing to a specific region of the camera's field of view.
   * This can significantly improve performance by reducing the area that needs to be analyzed,
   * and can help eliminate false detections from irrelevant parts of the image.
   * 
   * <p><b>IMPORTANT:</b> The crop window in the Limelight web interface must be set to fully
   * open (uncropped) for this dynamic cropping to work. This method overrides the UI setting.
   * 
   * <p><b>Coordinate System:</b>
   * <ul>
   *   <li>-1.0 = left/bottom edge of image</li>
   *   <li>0.0 = center of image</li>
   *   <li>+1.0 = right/top edge of image</li>
   * </ul>
   * 
   * <p><b>Example usage:</b>
   * <pre>{@code
   * // Crop to center 50% of image (horizontally and vertically)
   * LimelightHelpers.setCropWindow("limelight", -0.25, 0.25, -0.25, 0.25);
   * 
   * // Crop to upper half of image (useful for high targets)
   * LimelightHelpers.setCropWindow("limelight", -1.0, 1.0, 0.0, 1.0);
   * 
   * // Crop to right side (useful for side-mounted camera)
   * LimelightHelpers.setCropWindow("limelight", 0.0, 1.0, -1.0, 1.0);
   * 
   * // Reset to full image
   * LimelightHelpers.setCropWindow("limelight", -1.0, 1.0, -1.0, 1.0);
   * }</pre>
   *
   * @param limelightName Name of the Limelight camera
   * @param cropXMin Minimum X value - left edge of crop region (-1 to 1)
   * @param cropXMax Maximum X value - right edge of crop region (-1 to 1)
   * @param cropYMin Minimum Y value - bottom edge of crop region (-1 to 1)
   * @param cropYMax Maximum Y value - top edge of crop region (-1 to 1)
   */
  public static void setCropWindow(
      String limelightName, double cropXMin, double cropXMax, double cropYMin, double cropYMax) {
    // Build array with crop boundaries
    double[] entries = new double[4];
    entries[0] = cropXMin;
    entries[1] = cropXMax;
    entries[2] = cropYMin;
    entries[3] = cropYMax;
    // Send to NetworkTables
    setLimelightNTDoubleArray(limelightName, "crop", entries);
  }

  /**
   * Sets a 3D offset point for fiducial (AprilTag) tracking.
   * 
   * <p>This allows you to track a point offset from the center of the AprilTag. Instead of
   * getting the robot's pose relative to the tag center, you can get the pose relative to
   * a point of interest near the tag (e.g., a scoring position).
   * 
   * <p><b>Use cases:</b>
   * <ul>
   *   <li>Tracking a scoring position that's offset from the tag</li>
   *   <li>Targeting a specific game element near a tag</li>
   *   <li>Accounting for tag placement relative to field features</li>
   * </ul>
   * 
   * <p><b>Coordinate System:</b> Offsets are relative to the tag's coordinate system:
   * <ul>
   *   <li>+X extends right from tag</li>
   *   <li>+Y extends down from tag</li>
   *   <li>+Z extends out from tag face (toward camera)</li>
   * </ul>
   * 
   * <p><b>Example usage:</b>
   * <pre>{@code
   * // Track a point 0.5m to the right and 0.3m forward from the tag
   * LimelightHelpers.setFiducial3DOffset("limelight", 0.5, 0.0, 0.3);
   * 
   * // Reset to tag center
   * LimelightHelpers.setFiducial3DOffset("limelight", 0.0, 0.0, 0.0);
   * }</pre>
   * 
   * @param limelightName Name of the Limelight camera
   * @param offsetX X offset in meters (right is positive)
   * @param offsetY Y offset in meters (down is positive)
   * @param offsetZ Z offset in meters (away from tag is positive)
   */
  public static void setFiducial3DOffset(
      String limelightName, double offsetX, double offsetY, double offsetZ) {
    // Build offset array [x, y, z]
    double[] entries = new double[3];
    entries[0] = offsetX;
    entries[1] = offsetY;
    entries[2] = offsetZ;
    // Send to NetworkTables
    setLimelightNTDoubleArray(limelightName, "fiducial_offset_set", entries);
  }

  /////
  ///// MegaTag2 Configuration
  /////

  /**
   * Sets robot orientation values for the MegaTag2 localization algorithm.
   * 
   * <p><b>CRITICAL for MegaTag2:</b> This method MUST be called periodically (in your subsystem's
   * periodic() or in your main robot periodic()) before retrieving MegaTag2 pose estimates.
   * MegaTag2 uses robot orientation to improve pose accuracy and disambiguate solutions.
   * 
   * <p><b>This method automatically flushes NetworkTables</b> to ensure the orientation data is
   * sent immediately. For better performance when setting multiple values, use
   * {@link #SetRobotOrientation_NoFlush(String, double, double, double, double, double, double)}
   * and call {@link #Flush()} once after all updates.
   * 
   * <p><b>Coordinate System:</b> All angles follow standard FRC conventions:
   * <ul>
   *   <li><b>Yaw:</b> 0° = facing red alliance wall, increases counterclockwise</li>
   *   <li><b>Pitch:</b> 0° = level, positive = nose up</li>
   *   <li><b>Roll:</b> 0° = level, positive = right side down</li>
   * </ul>
   * 
   * <p><b>Parameter Importance:</b>
   * <ul>
   *   <li><b>yaw:</b> REQUIRED - This is the most critical parameter</li>
   *   <li><b>yawRate:</b> Helpful but optional - improves motion prediction</li>
   *   <li><b>pitch, pitchRate, roll, rollRate:</b> Usually unnecessary for ground robots</li>
   * </ul>
   * 
   * <p><b>Example usage (minimal - just yaw):</b>
   * <pre>{@code
   * // In your vision subsystem's periodic():
   * public void periodic() {
   *     // Update robot orientation for MegaTag2
   *     LimelightHelpers.SetRobotOrientation(
   *         "limelight",
   *         m_gyro.getYaw(),  // Robot heading from gyro
   *         0, 0, 0, 0, 0     // Other parameters can be zero
   *     );
   *     
   *     // Now safe to use MegaTag2
   *     PoseEstimate pose = LimelightHelpers.getBotPoseEstimate_wpiBlue_MegaTag2("limelight");
   * }
   * }</pre>
   * 
   * <p><b>Example usage (with yaw rate):</b>
   * <pre>{@code
   * LimelightHelpers.SetRobotOrientation(
   *     "limelight",
   *     m_gyro.getYaw(),      // Current heading
   *     m_gyro.getRate(),     // Angular velocity (helps with moving robots)
   *     0, 0, 0, 0            // Pitch and roll usually not needed
   * );
   * }</pre>
   *
   * @param limelightName Name/identifier of the Limelight camera
   * @param yaw Robot yaw angle in degrees (0 = facing red alliance wall)
   * @param yawRate Angular velocity of yaw in degrees per second (optional, can be 0)
   * @param pitch Robot pitch angle in degrees (optional, usually 0 for ground robots)
   * @param pitchRate Angular velocity of pitch in degrees per second (optional, usually 0)
   * @param roll Robot roll angle in degrees (optional, usually 0 for ground robots)
   * @param rollRate Angular velocity of roll in degrees per second (optional, usually 0)
   */
  public static void SetRobotOrientation(
      String limelightName,
      double yaw,
      double yawRate,
      double pitch,
      double pitchRate,
      double roll,
      double rollRate) {
    // Call internal method with flush enabled
    SetRobotOrientation_INTERNAL(
        limelightName, yaw, yawRate, pitch, pitchRate, roll, rollRate, true);
  }

  /**
   * Sets robot orientation for MegaTag2 without automatically flushing NetworkTables.
   * 
   * <p>Use this variant when setting multiple NetworkTables values in sequence to improve
   * performance. You must manually call {@link #Flush()} after all updates to ensure
   * the data is sent to the Limelight.
   * 
   * <p><b>Example usage:</b>
   * <pre>{@code
   * // Set multiple values efficiently
   * LimelightHelpers.SetRobotOrientation_NoFlush("limelight", yaw, yawRate, 0, 0, 0, 0);
   * LimelightHelpers.setPipelineIndex("limelight", 0);
   * LimelightHelpers.setPriorityTagID("limelight", 4);
   * // Flush once after all updates
   * LimelightHelpers.Flush();
   * }</pre>
   *
   * @param limelightName Name/identifier of the Limelight camera
   * @param yaw Robot yaw angle in degrees
   * @param yawRate Angular velocity of yaw in degrees per second
   * @param pitch Robot pitch angle in degrees
   * @param pitchRate Angular velocity of pitch in degrees per second
   * @param roll Robot roll angle in degrees
   * @param rollRate Angular velocity of roll in degrees per second
   * @see #SetRobotOrientation(String, double, double, double, double, double, double)
   */
  public static void SetRobotOrientation_NoFlush(
      String limelightName,
      double yaw,
      double yawRate,
      double pitch,
      double pitchRate,
      double roll,
      double rollRate) {
    // Call internal method with flush disabled
    SetRobotOrientation_INTERNAL(
        limelightName, yaw, yawRate, pitch, pitchRate, roll, rollRate, false);
  }

  /**
   * Internal implementation for setting robot orientation.
   * 
   * <p>Handles packing orientation data into an array and optionally flushing NetworkTables.
   * This method is private and called by the public SetRobotOrientation methods.
   * 
   * @param limelightName Name/identifier of the Limelight camera
   * @param yaw Robot yaw angle in degrees
   * @param yawRate Angular velocity of yaw in degrees per second
   * @param pitch Robot pitch angle in degrees
   * @param pitchRate Angular velocity of pitch in degrees per second
   * @param roll Robot roll angle in degrees
   * @param rollRate Angular velocity of roll in degrees per second
   * @param flush Whether to flush NetworkTables after setting the value
   */
  private static void SetRobotOrientation_INTERNAL(
      String limelightName,
      double yaw,
      double yawRate,
      double pitch,
      double pitchRate,
      double roll,
      double rollRate,
      boolean flush) {

    // Pack orientation data into array format expected by Limelight
    // Array order: [yaw, yawRate, pitch, pitchRate, roll, rollRate]
    double[] entries = new double[6];
    entries[0] = yaw;
    entries[1] = yawRate;
    entries[2] = pitch;
    entries[3] = pitchRate;
    entries[4] = roll;
    entries[5] = rollRate;
    
    // Send to NetworkTables under the robot_orientation_set key
    setLimelightNTDoubleArray(limelightName, "robot_orientation_set", entries);
    
    // Flush if requested to ensure immediate transmission
    if (flush) {
      Flush();
    }
  }

  /**
   * Configures the IMU mode for MegaTag2 Localization
   *
   * @param limelightName Name/identifier of the Limelight
   * @param mode IMU mode.
   */
  public static void SetIMUMode(String limelightName, int mode) {
    setLimelightNTDouble(limelightName, "imumode_set", mode);
  }

  /**
   * Sets the 3D point-of-interest offset for the current fiducial pipeline.
   * https://docs.limelightvision.io/docs/docs-limelight/pipeline-apriltag/apriltag-3d#point-of-interest-tracking
   *
   * @param limelightName Name/identifier of the Limelight
   * @param x X offset in meters
   * @param y Y offset in meters
   * @param z Z offset in meters
   */
  public static void SetFidcuial3DOffset(String limelightName, double x, double y, double z) {

    double[] entries = new double[3];
    entries[0] = x;
    entries[1] = y;
    entries[2] = z;
    setLimelightNTDoubleArray(limelightName, "fiducial_offset_set", entries);
  }

  /**
   * Overrides the valid AprilTag IDs that will be used for localization. Tags not in this list will
   * be ignored for robot pose estimation.
   *
   * @param limelightName Name/identifier of the Limelight
   * @param validIDs Array of valid AprilTag IDs to track
   */
  public static void SetFiducialIDFiltersOverride(String limelightName, int[] validIDs) {
    double[] validIDsDouble = new double[validIDs.length];
    for (int i = 0; i < validIDs.length; i++) {
      validIDsDouble[i] = validIDs[i];
    }
    setLimelightNTDoubleArray(limelightName, "fiducial_id_filters_set", validIDsDouble);
  }

  /**
   * Sets the downscaling factor for AprilTag detection. Increasing downscale can improve
   * performance at the cost of potentially reduced detection range.
   *
   * @param limelightName Name/identifier of the Limelight
   * @param downscale Downscale factor. Valid values: 1.0 (no downscale), 1.5, 2.0, 3.0, 4.0. Set to
   *     0 for pipeline control.
   */
  public static void SetFiducialDownscalingOverride(String limelightName, float downscale) {
    int d = 0; // pipeline
    if (downscale == 1.0) {
      d = 1;
    }
    if (downscale == 1.5) {
      d = 2;
    }
    if (downscale == 2) {
      d = 3;
    }
    if (downscale == 3) {
      d = 4;
    }
    if (downscale == 4) {
      d = 5;
    }
    setLimelightNTDouble(limelightName, "fiducial_downscale_set", d);
  }

  /**
   * Sets the camera pose relative to the robot.
   *
   * @param limelightName Name of the Limelight camera
   * @param forward Forward offset in meters
   * @param side Side offset in meters
   * @param up Up offset in meters
   * @param roll Roll angle in degrees
   * @param pitch Pitch angle in degrees
   * @param yaw Yaw angle in degrees
   */
  public static void setCameraPose_RobotSpace(
      String limelightName,
      double forward,
      double side,
      double up,
      double roll,
      double pitch,
      double yaw) {
    double[] entries = new double[6];
    entries[0] = forward;
    entries[1] = side;
    entries[2] = up;
    entries[3] = roll;
    entries[4] = pitch;
    entries[5] = yaw;
    setLimelightNTDoubleArray(limelightName, "camerapose_robotspace_set", entries);
  }

  /////
  /////

  public static void setPythonScriptData(String limelightName, double[] outgoingPythonData) {
    setLimelightNTDoubleArray(limelightName, "llrobot", outgoingPythonData);
  }

  public static double[] getPythonScriptData(String limelightName) {
    return getLimelightNTDoubleArray(limelightName, "llpython");
  }

  /////
  /////

  /** Asynchronously take snapshot. */
  public static CompletableFuture<Boolean> takeSnapshot(String tableName, String snapshotName) {
    return CompletableFuture.supplyAsync(
        () -> {
          return SYNCH_TAKESNAPSHOT(tableName, snapshotName);
        });
  }

  private static boolean SYNCH_TAKESNAPSHOT(String tableName, String snapshotName) {
    URL url = getLimelightURLString(tableName, "capturesnapshot");
    try {
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      if (snapshotName != null && !snapshotName.isEmpty()) {
        connection.setRequestProperty("snapname", snapshotName);
      }

      int responseCode = connection.getResponseCode();
      if (responseCode == 200) {
        return true;
      } else {
        System.err.println("Bad LL Request");
      }
    } catch (IOException e) {
      System.err.println(e.getMessage());
    }
    return false;
  }

  /**
   * Gets the latest JSON results output and returns a LimelightResults object.
   *
   * @param limelightName Name of the Limelight camera
   * @return LimelightResults object containing all current target data
   */
  public static LimelightResults getLatestResults(String limelightName) {

    long start = System.nanoTime();
    LimelightHelpers.LimelightResults results = new LimelightHelpers.LimelightResults();
    if (mapper == null) {
      mapper =
          new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    try {
      results = mapper.readValue(getJSONDump(limelightName), LimelightResults.class);
    } catch (JsonProcessingException e) {
      results.error = "lljson error: " + e.getMessage();
    }

    long end = System.nanoTime();
    double millis = (end - start) * .000001;
    results.latency_jsonParse = millis;
    if (profileJSON) {
      System.out.printf("lljson: %.2f\r\n", millis);
    }

    return results;
  }
}
