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
