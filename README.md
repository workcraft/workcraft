# ![Workcraft logo](logo.png)
[![Build Status](https://travis-ci.org/tuura/workcraft.svg?branch=master)](https://travis-ci.org/tuura/workcraft) [![Coverage Status](https://coveralls.io/repos/github/tuura/workcraft/badge.svg?branch=master)](https://coveralls.io/github/tuura/workcraft?branch=master)

Workcraft is a cross-platform toolset to capture, simulate, synthesize
and verify graph models. It supports a wide range of popular graph
formalisms and provides a plugin-based framework to model and analyze
new model types. For more information about Workcraft look at
http://workcraft.org/.

### Building

Workcraft requires Java JDK 1.7 or newer for a successful build and
is assembled via [Gradle](https://gradle.org/). It is tested with
both [Open JDK](http://openjdk.java.net/) and [Oracle JDK]
(http://www.oracle.com/technetwork/java/javase/downloads/index.html).
These instructions use `gradlew`, a wrapper that downloads and runs
version `2.12`. Alternatively a pre-installed version of `gradle` can
be used.

Use the `assemble` task to build the core and all the plugins:

    ./gradlew assemble

### Running

You can run Workcraft directly after building it:

    ./workcraft

### Miscellaneous

Help and tutorial pages are available in the
[workcraft-doc](https://github.com/tuura/workcraft-doc) repo.

Templates for building Windows and Linux distributions of Workcraft are
available in the [workcraft-dist-template](https://github.com/tuura/workcraft-dist-template)
repo. This includes the binaries of backend tools, gate libraries and
other platform-specific content.

If you would like to contribute to Workcraft development, then read
through the [CONTRIBUTING.md](CONTRIBUTING.md) document.
