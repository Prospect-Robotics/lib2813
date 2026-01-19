# Contributing to lib2813

We welcome contributions!

## What to Contribute

- Bug reports
- Bug fixes (consider filing a bug report first)
- Feature additions (please create an issue first)

## Pull Request Guidelines

- Code should be well documented.
- Please consider writing tests. Tests give us assurance that new changes do not break older functionality.
- We loosely follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html). Format the code 
  using `./gradlew spotlessApply`.
- Write a [good change description](https://google.github.io/eng-practices/review/developer/cl-descriptions.html)

We may ask you to test locally on your robot code in simulation mode.

## Getting Started

If you need to make changes to lib213, you can either clone the repository directly, or you
can include it as a submodule for your robot code.

### Option 1: Developing on a Clone

To clone the repository, run:

```shell
git clone --recurse-submodules https://github.com/Prospect-Robotics/Robot2025.git
```

### Option 2: Developing via Submodules

#### 1. Adding the lib2813 repo as a submodule

When making changes to lib2813, it is often helpful to build the code along with your robot code.
To do that, you can add the  lib2813 repo as a submodule.

Before adding the submodule, it is recommended that you set the `submodule.stickyRecursiveClone`
git config option to `true` to make working with submodule easier (see
[this StackOverflow answer](https://stackoverflow.com/a/53622660) for details about this option).
To do this, run the following command from any directory:
```shell
git config --global submodule.stickyRecursiveClone true
```

To add the lib2813 submodule, go to your robot project directory (where the `vendordeps` directory is), and run
this command:
```shell
git submodule add https://github.com/Prospect-Robotics/lib2813
```

#### 2. Updating Gradle files

Add the following lines needs to be added to your `settings.gradle`:
```
includeBuild('lib2813') {
    dependencySubstitution {
        substitute module('com.team2813.lib2813:lib') using project(':lib')
    }
}
```

Next, remove the version numbers for the lib2813 dependencies in your `build.gradle`:

```
implementation "com.team2813.lib2813:lib"
```

Next, add the following lines to your `build.gradle`:

Finally, in order to guarantee that the library jars are created before GradleRIO referees to them, add the following
lines to your `build.gradle`:
```groovy
downloadDepsPreemptively.dependsOn gradle.includedBuild('lib2813').task(':lib:jar')
downloadDepsPreemptively.dependsOn gradle.includedBuild('lib2813').task(':testing:jar')
downloadDepsPreemptively.dependsOn gradle.includedBuild('lib2813').task(':limelight:jar')
```

#### 3. Fixing vscode jank
As of version 1.85.1, vscode doesn't work properly with gradle composite builds without the old buildServer. To use the old buildServer, add the following line
to your settings.json
```
"java.gradle.buildServer.enabled": "off",
```
This isn't strictly necessary, but without it vscode will not be able to do code completion from things in the library, and tell the user that there are errors,
when gradle builds fine.

## Tips and Tricks

### Publishing to Maven Local

It can often be useful to publish jars locally and test with a real or simulated robot.

To publish to Maven Local, run:

```shell
./gradlew publishToMavenLocal -Pversion=2.0.0-test-123
```

(replace "test-123" with some unique identifier)

In your robot's `build.gradle` file, be sure to include Maven Local in your repositories:

```groovy
repositories {
    mavenLocal()
    mavenCentral()
}
```

Then update your `build.gradle` to reference the version that you published locally.

### Cloning a repository with a git submodule
When cloning a repository with a git submodule, git will not automatically get the files in the submodules. in order to do this, run the command
```
git submodule update --init --recursive
```
This command will recursively initialize all submodules.

### Getting blame data

To ignore code reformatting when running `git blame` run:

```shell
git config blame.ignoreRevsFile .git-blame-ignore-revs
```
