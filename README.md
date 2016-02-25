# ![Workcraft logo](logo.png)
[![Build Status](https://travis-ci.org/tuura/workcraft.svg?branch=master)](https://travis-ci.org/tuura/workcraft)

Workcraft is a cross-platform toolset to capture, simulate, synthesize
and verify graph models. It supports a wide range of popular graph
formalisms and provides a plugin-based framework to model and analyze
new model types. For more information about Workcraft look at 
[workcraft.org](http://workcraft.org/).

### Building

Workcraft is built via [Gradle](https://gradle.org/). These instructions
use `gradlew`, a wrapper that will download version `2.11` for you. If
you want to run your own gradle, you can.

Use the `assemble` task to build the core and all the plugins:

    $ ./gradlew assemble

If Gradle complains about a missing `JAVA_HOME` env var even though it
is set properly, the following may help in Debian-like systems:

    $ sudo ln -s /usr/lib/jvm/your-jdk /usr/lib/jvm/default-java

### Running

The startup scripts will only work as part of a dist folder. You can run
Workcraft directly after building the project via Gradle as follows:

    $ java -cp "$(ls -1 */build/libs/*.jar | tr '\n' ':')" org.workcraft.Console

### Miscellanious

Help and tutorial pages are available in the
[workcraft-doc](https://github.com/tuura/workcraft-doc) repo.

Templates for building Windows and Linux distributions of Workcraft are 
available in the [workcraft-doc](https://github.com/tuura/workcraft-dist-template) 
repo. This includes the binaries of backend tools, gate libraries and
other platform-specific content.

If you would like to contribute to Workcraft development, then read 
through the [HACKING](HACKING.md) document.
