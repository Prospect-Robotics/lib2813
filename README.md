# User Documentation

## adding submodule
In order to use, go to your WPILib project directory (the inner one, where the build.gradle file is), and run the command
```
git submodule add https://github.com/Prospect-Robotics/lib2813
```
## adding submodule to gradle
The following lines needs to be added to your settings.gradle to make the lib usable:
```
includeBuild('lib2813') {
    dependencySubstitution {
        substitute module('com.team2813:lib2813') using project(':lib')
    }
}
```
Whatever text that is in the module parentheses is the text that will need to be in an `implementation` statement to depend on the library.
So, after adding the lines to the settings.gradle, this line in the dependencies block will refer to the library.
```
implementation "com.team2813:lib2813"
```
Finally, in order to guarantee that the library jars are created before GradleRIO referees to them, add the following line to your build.gradle
```
downloadDepsPreemptively.dependsOn gradle.includedBuild("lib2813").task(":lib:jar")
```

## Fixing vscode jank
As of version 1.85.1, vscode doesn't work properly with gradle composite builds without the old buildServer. To use the old buildServer, add the following line
to your settings.json
```
"java.gradle.buildServer.enabled": "off",
```
This isn't strictly necessary, but without it vscode will not be able to do code completion from things in the library, and tell the user that there are errors,
when gradle builds fine.

## Cloning a repository with a git submodule
When cloning a repository with a git submodule, git will not automatically get the files in the submodules. in order to do this, run the command
```
git submodule update --init --recursive
```
This command will recursively initialize all submodules.

This code is still in development, and apis are still subject to change. The most likely thing to get removed is the swerve api, as ctre recently released their own.

## Developer Documentation

To ignore code reformatting when running `git blame` run:

```shell
git config blame.ignoreRevsFile .git-blame-ignore-revs
```
