This doc contains information relevant to people contributing to Workcraft.
Read the [README](README.md) first if you have not done it yet.

### Testing

Testing includes a collection of JUnit tests and checkstyle to enforce a sane 
code style throughout the Java codebase (see below). The tests should be run 
before proposing your changes for the merge into the master repo as follows:

    $ ./gradlew check

### Code style

The code style is configured via [checkstyle.xml](config/checkstyle/checkstyle.xml).

The style is similar to [Google's Java
style](https://google.github.io/styleguide/javaguide.html), but it is
more lax and indents with four spaces.

To give a quick overview of it, here is a code snippet showing the
basics:

```java
Class Foo {

    public static final int CONSTANT = 1;

    private boolean myField = true;

    public static void barMethod(int someInt,
            String someString) {
        if (someInt > 3 || someInt < 0) {
            someFunc(someInt);
        } else {
            someString = "value is " + someInt;
        }
        List<String> myList = new ArrayList<>();
    }
}
```

### Eclipse integration

[Eclipse IDE](https://www.eclipse.org/) is a convenient environment for
developing and debugging Workcraft. 

#### Integration of Gradle build system

When setting Eclipse from Gradle scripts it is important to separate its *Workspace* 
directory from the *Project* directory (otherwise Gradle integration may fail).

* As an example, create `workspace` directory and clone the Workcraft repo into it:
```
$ mkdir workspace
$ cd workspace
$ git clone git@github.com:tuura/workcraft.git
```

* Install [Buildship Gradle Integration](https://marketplace.eclipse.org/content/buildship-gradle-integration)
  plugin in Eclipse via the *Help->Instal New Software...* menu.

* In Eclipse select the `workspace` directory as the current *Workspace*.

* Import the project from Gradle config via the *File->Import...->Gradle*
  menu. Select `workspace/workcraft` as the *Project* directory. 
  Follow the import accepting the default settings.

* Import `WorkcraftRunner` project via the *File->Import...* as a
  *General->Existing Projects into Workspace* item.

* Create a *Java Application* runner with the following configuration:

  * Name: Workcraft
  * Project: WorkcraftRunner
  * Main class: org.workcraft.Console

#### Code style adjustments

The default code style of eclipse uses tabs for indentation. This 
contradicts to the checkstyle that requires 4 spaces for each level of 
indentation. Therefore Eclipse settings need to be modified as follows:

* Select *Windows->Preferences* menu. 
* Go to the *Java->Code Style->Formatter* section.
* Edit the indentation policy of *Eclipse [built-in]* profile by changing its tab policy to *Spaces only*.
* Save the modified profile under a new name and select it as the active profile.

#### Integration of backend tools

* Create symbolic link `workspace/tools` pointing to your the location of the backend tools for your platform. E.g. for Linux:
```
  $ cd workspace
  $ ln -s workcraft/dist-template/linux/tools
```

Choose *Run->Run Configurations...* menu and edit the Workcraft runner under *Java Application* section (the one created in the previous section). On the *Arguments* tab modify the *Working directory* so it points to `${workspace_loc}`.
Now workspace will be the current directory for Workcraft when started from Eclipse and the tools will be in the right place for Workcraft to locate the backend tools.

Note: Workcraft requires Java 1.7 or newer for a successful build. You may have several versions of Java installed with Java 1.7 being active system-wide. However, Eclipse may have a different version of Java set as its default. Check this under *Windows->Preferences->Java->Compiler* section.
