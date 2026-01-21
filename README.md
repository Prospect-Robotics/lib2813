# User Documentation

## Using the libraries

### Updating your dependencies

> [!NOTE]
> The lib2813 jars are not yet published to Maven Central. For the time being, you need to publish
> them to Maven Local. See [the Contributing page](CONTRIBUTING.md#publishing-to-maven-local) for details.

In your `build.gradle`, update the `dependencies` section:

```groovy
dependencies {
    // Existing dependencies
    implementation 'com.team2813.lib2813:lib:2.0.0-rc-1'
    implementation 'com.team2813.lib2813:vision:2.0.0-rc-1'
    implementation 'com.team2813.lib2813:limelight:2.0.0-rc-1'
    testImplementation 'com.team2813.lib2813:testing:2.0.0-rc-1'
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

### Vendordeps

- `com.team2813.lib2813:lib`:
  - `WPILibNewCommands.json`
  - `Phoenix6.json` (if using Phoenix motors)
  - `REVLib.json` (if using REV Robotics motors)
- `com.team2813.lib2813:vision`:
  - `photonlib.json`
- `com.team2813.lib2813:testing`:
  - `WPILibNewCommands.json`
