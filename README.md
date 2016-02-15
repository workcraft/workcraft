# Workcraft

[![Build Status](https://travis-ci.org/tuura/workcraft.svg?branch=master)](https://travis-ci.org/tuura/workcraft)

Workcraft is a cross-platform toolset to capture, simulate, synthesize
and verify graph models. It supports a wide range of popular graph
formalisms and provides a plugin-based framework to model and analyze
new model types.

### Building

Workcraft is built via [Gradle](https://gradle.org/). These instructions
use `gradlew`, a wrapper that will download version `2.10` for you. If
you want to run your own gradle, you can.

Use the `assemble` task to build the core and all the plugins:

    $ ./gradlew assemble

If Gradle complains about a missing `JAVA_HOME` env var even though it
is set properly, the following may help in Debian-like systems:

    $ sudo ln -s /usr/lib/jvm/your-jdk /usr/lib/jvm/default-java

### Running the tests

    $ ./gradlew check

On top of running the JUnit tests, this command will also run
[checkstyle](https://github.com/checkstyle/checkstyle) in order to
enforce a sane code style throughout the Java codebase.

### Building in Eclipse

[Eclipse IDE](https://www.eclipse.org/) is a convenient environment for
developing and debugging Workcraft. When setting Eclipse from Gradle
scripts it is important to separate its *Workspace* directory from the
*Project* directory (otherwise Gradle integration may fail).

* As an example, create `workcraft-workspace` directory and clone the Workcraft repo into it:
```
$ mkdir workcraft-workspace
$ cd workcraft-workspace
$ git clone https://github.com/tuura/workcraft workcraft-master
```
* Install [Buildship Gradle Integration](https://marketplace.eclipse.org/content/buildship-gradle-integration)
  plugin in Eclipse via the *Help->Instal New Software...* menu.

* In Eclipse select the `workcraft-workspace` directory as the current
  *Workspace*.

* Import the project from Gradle config via the *File->Import...->Gradle*
  menu. Select `workcraft-workspace/workcraft-master` as the *Project*
  directory. Follow the import accepting the default settings.

* Import `WorkcraftRunner` project via the *File->Import...* as a
  *General->Existing Projects into Workspace* item.

* Create a *Java Application* runner with the following configuration:

  * Name: Workcraft
  * Project: WorkcraftRunner
  * Main class: org.workcraft.Console

### Running

Use a startup script to set up the path to Java JVM and the classpath for Workcraft
plugins:

  * `workcraft.bat` - BAT file for Windows.
  * `workcraft` - Bash script for Linux and unix-like systems.

### Help and Tutorials

Help and tutorial pages are available in the
[workcraft-doc](https://github.com/tuura/workcraft-doc) repo.

For more information about Workcraft look at [workcraft.org](http://workcraft.org/).
