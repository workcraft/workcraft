# Workcraft

[![Build Status](https://travis-ci.org/tuura/workcraft.svg?branch=master)](https://travis-ci.org/tuura/workcraft)

Workcraft is a cross-platform toolset to capture, simulate, synthesize
and verify graph models. It supports a wide range of popular graph
formalisms and provides a plugin-based framework to model and analyze
new model types.

### Building from the command line

Workcraft relies on [Gradle CI](http://gradle.org/) build sustem, version 2.7 or newer is required.
Install Gradle build automation system:
```shell
$ sudo apt-get install gradle
```

Build the Workcraft core and all the plugins:
```shell
$ gradle assemble
```

If Gradle complains about missing `JAVA_HOME` even though it is set properly, the following may help in Ubuntu (replace `<JAVA-JDK>` by your Java JDK installation):
```shell
$ sudo /usr/lib/jvm/<JAVA-JDK> /usr/lib/jvm/default-java
```  

### Building in Eclipse

[Eclipse IDE](http://www.eclipse.org/)	is a convenient environemnt for developing and debuging Workcraft. When setting Eclipse from Gradle scripts it is important to separate its *Workspace* directory from the *Project* directory (otherwise Gradle integration may fail). 
* As an example, create `workcraft-workspace` directory and clone the workcraft git repo into it:
```shell
$ mkdir workcraft-workspace
$ cd workcraft-workspace
$ git clone https://github.com/tuura/workcraft workcraft-master
```

* Install [Buildship Gradle Integration](http://marketplace.eclipse.org/content/buildship-gradle-integration) plugin in Eclipse via	*Help->Instal New Software…* menu.

* In Eclipse select the `workcraft-workspace` directory as the current *Workspace*.

* Import the project from Gradle config via *File->Import..->Gradle* menu. Select `workcraft-workspace/workcraft-master` as the *Project* directory. Follow the import accepting the default settings.

* Import `WorkcraftRunner` project via *File->Import…* as a *General->Existing Projects into Workspace* item.

* Create a *Java Application* runner with the following configuration:
  * Name: Workcraft
  * Project: WorkcraftRunner
  * Main class: org.workcraft.Console

### Running

Use a startup script to set up the path to Java JVM and the classpath for Workcraft
plugins:
  * `workcraft.bat` BAT file for Windows.
  * `workcraft` Bash script for Linux and unix-like systems.

### Help and Tutorials

Help and tutorial pages are available in the
[workcraft-doc](https://github.com/tuura/workcraft-doc) repo.

For more information about Workcraft look at [workcraft.org](http://workcraft.org/).
