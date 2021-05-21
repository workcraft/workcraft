# ![Workcraft logo](workcraft/WorkcraftCore/res/images/logo.png)

![Build status](https://github.com/workcraft/workcraft/workflows/CI/badge.svg)
[![Code maintainability](https://codeclimate.com/github/workcraft/workcraft/badges/gpa.svg)](https://codeclimate.com/github/workcraft/workcraft)
[![CII Best Practices](https://bestpractices.coreinfrastructure.org/projects/3775/badge)](https://bestpractices.coreinfrastructure.org/projects/3775)
[![Coverage](https://img.shields.io/coveralls/github/workcraft/workcraft.svg)](https://coveralls.io/github/workcraft/workcraft)
![Repo size](https://img.shields.io/github/repo-size/workcraft/workcraft.svg)
![Code size](https://img.shields.io/github/languages/code-size/workcraft/workcraft.svg)
[![Current release](https://img.shields.io/github/release/workcraft/workcraft.svg)](https://github.com/workcraft/workcraft/releases)

Workcraft is a cross-platform toolset to capture, simulate, synthesize
and verify graph models. It supports a wide range of popular graph
formalisms and provides a plugin-based framework to model and analyze
new model types. For more information about Workcraft look at
https://workcraft.org/.

### Getting source

Get Workcraft source code and a submodule for documentation and
platform-specific backend tools
([workcraft-dist-template](https://github.com/workcraft/workcraft-dist-template) repo):

    git clone https://github.com/workcraft/workcraft.git
    cd workcraft
    git submodule update --init --remote

### Building

Workcraft requires Java JDK 8 or newer for a successful build and is
assembled via [Gradle](https://gradle.org/).
It is tested with both [Open JDK](http://openjdk.java.net/) and
[Oracle JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html).

These instructions use `gradlew`, a wrapper that downloads and runs
a relatively new version of `gradle`. Alternatively a pre-installed
version can be used.

Use the `assemble` task to build the core and all the plugins:

    ./gradlew assemble

### Running

You can run Workcraft directly after building it:

    ./gradlew run

Note that Workcraft relies on backend tools for some of its functionality
and expects them in `tools` directory by default. Therefore create a
symbolic link pointing to the location of the backend tools for your
platform. E.g. for Linux:

    ln -s dist/template/linux/tools

---
If you would like to contribute to Workcraft development, then read
through the [CONTRIBUTING.md](CONTRIBUTING.md) document.
