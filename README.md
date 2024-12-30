# About

This Gradle Plugin unpacks the source artifacts of a project's dependencies
into the directory `build/unpackedJars` and adds it to the build path.

It is intended to be used for dependency management in GWT and JSweet
projects. For these kind of projects, some source code transformations may
need to be applied to each dependency's source code before they can be
processed with the respective compiler / transpiler.

# License

This software is released under the terms of the GNU Lesser General Public
License.

See  [LGPL.md](LGPL.md) and [GPL.md](GPL.md) for details.

# Development and testing

Before publishing the plugin to the Gradle plugin portal or the Topobyte Maven
repository, it should be tested locally.
To help with that, there are a few testing-projects available within
this repository that use the locally built version. In order to test them, first
build the current version:

    ./gradlew -PpublishForTesting publish

This will put the plugin artifacts and metadata into a local Maven repository in
directory `maven-repo`.
The testing projects reside in the `test-local` directory and are configured to
prioritize this local repository over the official plugin portal repository.
Have a look at what the script `test.sh` does.
It tests the plugin in different scenarios, especially using different
combinations of JDK and Gradle versions.