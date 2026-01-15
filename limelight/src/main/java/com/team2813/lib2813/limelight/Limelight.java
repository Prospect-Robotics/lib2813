/*
Copyright 2024-2025 Prospect Robotics SWENext Club

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

import edu.wpi.first.wpilibj.Filesystem;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public interface Limelight {

  /**
   * Gets the limelight with the default name.
   *
   * @return the {@link Limelight} object for interfacing with the limelight
   */
  static Limelight getDefaultLimelight() {
    return RestLimelight.getDefaultLimelight();
  }

  /** Gets an object for getting locational data. */
  LocationalData getLocationalData();

  void setFieldMap(InputStream stream, boolean updateLimelight) throws IOException;

  /**
   * Sets the field map for the limelight with a file in the deploy directory. Additionally, this
   * may also upload the field map to the Limelight if desired. This will likely be a slow
   * operation, and should not be regularly called.
   *
   * @param filepath The path to the file from the deploy directory (using UNIX file separators)
   * @param updateLimelight If the limelight should be updated with this field map
   * @throws IOException If the given filepath does not exist in the deploy directory or could not
   *     be read
   */
  default void setFieldMap(String filepath, boolean updateLimelight) throws IOException {
    File file = new File(Filesystem.getDeployDirectory(), filepath);
    try (var stream = new FileInputStream(file)) {
      setFieldMap(stream, updateLimelight);
    }
  }
}
