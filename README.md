# User Documentation

## Using the libraries

### Updating your dependencies

> [!NOTE]
> The lib2813 jars are not yet published to Maven Central. For the time being, you need to publish
> them to Maven Local. See [the Contributing page](CONTRIBUTING.md#publishing-to-maven-local) for details.

In your project's "gradle" directory, create a file named `libs.versions.toml` with the following content:

```toml
[versions]
lib2813 = "2.0.0-rc-1"

[libraries]
lib2813-lib = { module = "com.team2813.lib2813:lib", version.ref="lib2813" }
lib2813-vision = { module = "com.team2813.lib2813:vision", version.ref="lib2813" }
lib2813-limelight = { module = "com.team2813.lib2813:limelight", version.ref="lib2813" }
lib2813-testing = { module = "com.team2813.lib2813:testing", version.ref="lib2813" }
```

In your `build.gradle`, update the `dependencies` section:

```groovy
dependencies {
    // Existing dependencies
    implementation libs.lib2813.lib
    implementation libs.lib2813.vision
    implementation libs.lib2813.limelight
    testImplementation libs.lib2813.testing
}
```

Note that you do not need to include all the dependencies. See the [Runtime dependencies](#runtime-dependencies)
section for details.

In addition be sure to include Maven Local in the list of repositories in your `build.gradle`
(needed until we publish to Maven Central):

```groovy
repositories {
    mavenLocal()
    mavenCentral()
}
```

### Upgrading

To upgrade the version of the lib2813 libraries you are using, simply update the version string for "lib2813" in
`libs.versions.toml`.

### Vendordeps

- `com.team2813.lib2813:lib`:
  - `WPILibNewCommands.json`
  - `Phoenix6.json` (if using Phoenix motors)
  - `REVLib.json` (if using REV Robotics motors)
- `com.team2813.lib2813:vision`:
  - `photonlib.json`
- `com.team2813.lib2813:testing`:
  - `WPILibNewCommands.json`
