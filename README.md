# User Documentation

## Using the libraries

### Updating your dependencies

In your project's "gradle" directory, create a file named `libs.versions.toml` with the following
content:

```toml
[versions]
lib2813 = "2.0.0-rc-3"

[libraries]
lib2813-lib = { module = "com.team2813.lib2813:lib", version.ref="lib2813" }
lib2813-vendor-ctre = { module = "com.team2813.lib2813:vendor-ctre", version.ref="lib2813" }
lib2813-vendor-rev = { module = "com.team2813.lib2813:vendor-rev", version.ref="lib2813" }
lib2813-vision = { module = "com.team2813.lib2813:vision", version.ref="lib2813" }
lib2813-limelight = { module = "com.team2813.lib2813:limelight", version.ref="lib2813" }
lib2813-testing = { module = "com.team2813.lib2813:testing", version.ref="lib2813" }
```

In your `build.gradle`, update the `dependencies` section:

```groovy
dependencies {
    // Existing dependencies here
    
    // Lib2813 dependencies
    implementation libs.lib2813.lib
    implementation libs.lib2813.vendor-ctre
    implementation libs.lib2813.vendor-rev
    implementation libs.lib2813.vision
    implementation libs.lib2813.limelight
    testImplementation libs.lib2813.testing
}
```

Note: You do not need to include all the dependencies. For example, if you don't use REV Robotics
hardware (i.e. you don't have `REVLib.json` in your `vendordeps` directory) then you do not need
to add `libs.lib2813.vendor-rev` to your dependencies.

### Upgrading

To upgrade the version of the lib2813 libraries you are using, simply update the version string for
"lib2813" in `libs.versions.toml`.

### Vendordeps

- `com.team2813.lib2813:lib`:
  - `WPILibNewCommands.json`
- `com.team2813.lib2813:vendor-ctre`:
  - `Phoenix6.json`
- `com.team2813.lib2813:vendor-ctre`:
  - `REVLib.json`
- `com.team2813.lib2813:limelight`:
  - `Phoenix6.json`
- `com.team2813.lib2813:vision`:
  - `photonlib.json`
- `com.team2813.lib2813:testing`:
  - `WPILibNewCommands.json`
