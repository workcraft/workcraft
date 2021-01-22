SPECIAL_LINE=" == expected js line"

SCRIPT="print('$SPECIAL_LINE'); exit();"
if [[ $OSTYPE == msys* ]]; then
    ARGS="-exec:\\\"$SCRIPT\\\""
else
    ARGS="-exec:\"$SCRIPT\""
fi

# Development version
./gradlew run --args="$ARGS" \
| grep -q "^$SPECIAL_LINE" || error "Workcraft (development) did not start up correctly"

# Release version
case $OSTYPE in
    darwin*)
        dist/run.sh osx --force >/dev/null || error "dist/run.sh did not succeed"
        WORKCRAFT_CORE_JAR="dist/result/osx/Workcraft.app/Contents/Resources/bin/WorkcraftCore.jar"
        ;;
    linux*)
        dist/run.sh linux --force >/dev/null || error "dist/run.sh did not succeed"
        WORKCRAFT_CORE_JAR="dist/result/linux/workcraft/bin/WorkcraftCore.jar"
        ;;
    msys*)
        dist/run.sh windows --force >/dev/null || error "dist/run.sh did not succeed"
        WORKCRAFT_CORE_JAR="dist/result/windows/workcraft/bin/WorkcraftCore.jar"
        ;;
    *)
        error "Unsupported OS type $OSTYPE"
        ;;
esac

java -classpath $WORKCRAFT_CORE_JAR org.workcraft.Console -exec:"$SCRIPT" \
| grep -q "^$SPECIAL_LINE" || error "Workcraft (release) did not start up correctly"
