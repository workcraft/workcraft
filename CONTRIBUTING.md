# Contributing to Workcraft

This doc contains information relevant to people contributing to Workcraft.
Read the [README.md](README.md) first if you have not done it yet.

### Testing

Testing includes a collection of JUnit tests and checkstyle to enforce a sane
code style throughout the Java codebase (see below). The tests should be run
before proposing your changes for the merge into the master repo as follows:

    ./gradlew check

### Code style

The code style is configured via [checkstyle.xml](config/checkstyle/checkstyle.xml).

The style is similar to [Google's Java style](https://google.github.io/styleguide/javaguide.html),
but it is more lax and indents with four spaces.

To give a quick overview of it, here is a code snippet showing the
basics:

```java
class Foo {

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

1. Clone the Workcraft git repo and set it as the current directory:

        git clone https://github.com/tuura/workcraft.git
        cd workcraft

2. Generate Java parser classes from JavaCC grammar files:

        ./gradlew compileJavacc

3. Generate Eclipse projects from Gradle config files:

        ./gradlew eclipse

4. Start Eclipse and select `workcraft` as the current `Workspace` directory.

5. Import all `workcraft` projects via the `File->Import...` as a
  `General->Existing Projects into Workspace` item.

6. Create a `Java Application` runner with the following configuration:

  * Name: Workcraft
  * Project: WorkcraftRunner
  * Main class: org.workcraft.Console

#### Code style adjustments

The default code style of eclipse uses tabs for indentation. This
contradicts to the checkstyle that requires 4 spaces for each level of
indentation. Therefore Eclipse settings need to be modified as follows:

1. Select `Windows->Preferences` menu.

2. Go to the `Java->Code Style->Formatter` section.

3. Edit the indentation policy of `Eclipse [built-in]` profile by
   changing its tab policy to `Spaces only`.

4. Save the modified profile under a new name and select it as the active
   profile.

#### Integration of backend tools

1. Create symbolic link pointing to the location of the backend tools for
   your platform, e.g. for Linux:

        ln -s dist-template/linux/tools

2. Choose `Run->Run Configurations...` menu and edit the Workcraft runner
  under `Java Application` section (the one created in the previous
  section): on the `Arguments` tab modify the `Working directory` so
  it points to `${workspace_loc}`.

This will set `workcraft` as the current directory when Workcraft is
started from Eclipse and the tools directory will be in the right place
to locate the backend tools.

Note: Workcraft requires Java 1.7 or newer for a successful build. You
may have several versions of Java installed with Java 1.7 being active
system-wide. However, Eclipse may have a different version of Java set
as its default. Check this under `Windows->Preferences->Java->Compiler`
section.

### Common issues

If Gradle complains about a missing `JAVA_HOME` env var even though it
is set properly, the following may help in Debian-like systems:

    sudo ln -s /usr/lib/jvm/your-jdk /usr/lib/jvm/default-java
