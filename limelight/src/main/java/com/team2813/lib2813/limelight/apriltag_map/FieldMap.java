package com.team2813.lib2813.limelight.apriltag_map;

/**
 * Represents a collection of fiducials (AprilTags) in a field map.
 *
 * <p>This class is intended to be deserialized from JSON. The JSON should contain an array of
 * fiducials under the key "fiducials". Each fiducial is represented by a {@link Fiducial} object.
 *
 * <p>Example JSON format:
 * <pre>
 * {
 *   "fiducials": [
 *     { "id": 1, "transform": [ ...16 values... ] },
 *     { "id": 2, "transform": [ ...16 values... ] }
 *   ]
 * }
 * </pre>
 */
class FieldMap {

  /** Array of fiducials present in the field map. */
  public Fiducial[] fiducials;
}
