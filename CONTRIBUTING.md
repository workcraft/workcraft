# Contributing to Workcraft

This doc contains information relevant to people contributing to Workcraft.
Read the [README.md](README.md) first if you have not done it yet.

### Testing

Testing includes a collection of [JUnit](https://junit.org/) tests and also
[checkstyle](https://github.com/checkstyle/checkstyle) and
[PMD](https://pmd.github.io/) checks to enforce a sane code style throughout
the Java codebase (see below). The tests should be run before proposing your
changes for the merge into the master as follows:

    ./gradlew check

### Code style

The code style is configured via
[config/checkstyle/checkstyle.xml](config/checkstyle/checkstyle.xml) and
[config/pmd/rules.xml](config/pmd/rules.xml). The style is similar to
[Google's Java style](https://google.github.io/styleguide/javaguide.html),
but it is more lax and indents with four spaces. To give a quick overview
of it, here is a code snippet showing the basics:

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

### InteliJ IDEA integration

[InteliJ IDEA](https://www.jetbrains.com/idea/) is the preferred development
environment for Workcraft. Generate IDEA project and Workcraft application
runner from Gradle config files as follows:

    ./gradlew idea

Now just start IDEA and open `workcraft.ipr` project file.

Check that a correct version of Java is selected in
`File->Project Structure...` dialog under `Project->Project SDK` section.

### Eclipse integration

[Eclipse IDE](https://www.eclipse.org/) is a convenient environment for
developing and debugging Workcraft. Generate Eclipse projects from Gradle
config files:

    ./gradlew eclipse

Start Eclipse and select `workcraft` as the current `Workspace` directory.
Import all Workcraft projects via the `File->Import...` and selecting the
`General->Existing Projects into Workspace` item.

Check that a correct version of Java is selected in `Windows->Preferences`
dialog under `Java->Compiler` section.

To run Workcraft from within Eclipse create a `Java Application` runner
with the following configuration:

  * Name: `Workcraft`
  * Project: `WorkcraftRunner`
  * Main class: `org.workcraft.Console`
  * Working directory (at Arguments tab): `${workspace_loc}`

If you decide to run JUnit tests from within Eclipse, e.g. for interactive
debugging, then you will need to change the Working directory of the test
run configuration to `${workspace_loc}`.

The default code style of eclipse uses tabs for indentation. This
contradicts to the checkstyle that requires 4 spaces for each level of
indentation. Therefore Eclipse settings need to be modified as follows:

  * Select `Windows->Preferences` menu and go to the
  `Java->Code Style->Formatter` section.
  * Edit the indentation policy of `Eclipse [built-in]` profile by
  changing its tab policy to `Spaces only`.
  * Save the modified profile under a new name and select it as the active
   profile.

### Common issues

  * If a wrong version of Java is used by Gradle, check what JRE/JDK 
  installations are available and which one is actually picked:

        ./gradlew javaToolchains

  * If Gradle complains about a missing `JAVA_HOME` environment variable even
  though it is set properly, the following may help in Debian-like systems:

        sudo ln -s /usr/lib/jvm/your-jdk /usr/lib/jvm/default-java

  * If you hit disk quota for the home directory due to Gradle using too
  much space, point ``GRADLE_USER_HOME`` environment variable to a different
  location
