# Maintainer Documentation

## Publishing to Maven Central

Before publishing to Maven Central, consider publishing to Maven Local.

1. Run `./gradlew build` to build the code, run the tests, and verify that there are no formatting
   issues
2. Update the version string in `publishing-conventions.gradle`.
3. Commit your changes
4. Run `export VERSION="2.0.x"` (replacing "2.0.x" with the version)
5. Add a git label by running `git tag -a -m "lib2813 ${VERSION} release" "${VERSION}"`
6. Push to GitHub and merge to main
7. Pull from main
8. Copy `lib2813-maven-publishing.tar.gz.gpg` from [SW_Secrets](https://drive.google.com/drive/u/0/folders/1Jeea26SZV5YXSZJpCgnhf4oy-nHNJX9E)
Decrypt `lib2813-maven-publishing.tar.gz.gpg`
8Copy the last five lines of the decrypted file to your personal `gradle.properties` file
9. Run `./gradlew publishToMavenCentral`
10. Celebrate!
