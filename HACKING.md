# Hacking

This doc contains information relevant to people contributing to
Workcraft. Read the [README](README.md) first if you haven't.

### Running

The startup scripts will only work as part of a dist folder. You can run
Workcraft directly after building the project via Gradle as follows:

    java -cp "$(ls -1 */build/libs/*.jar | tr '\n' ':')" org.workcraft.Console

### Code style

The code style is configured via
[checkstyle.xml](config/checkstyle/checkstyle.xml).

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
