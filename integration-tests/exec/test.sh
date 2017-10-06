SPECIAL_LINE=" == expected js line"

# Development version
./workcraft -nogui -exec:<(echo "
    print('$SPECIAL_LINE');
    exit();
") \
| grep -q "$SPECIAL_LINE" || error "Workcraft (development) did not start up correctly"


# Release version
if [[ $OSTYPE == darwin* ]]; then
    ./dist.sh --force osx >/dev/null || error "dist.sh did not succeed"
    WORKCRAFT="dist/osx/Workcraft.app/Contents/MacOS/Workcraft"
else
    ./dist.sh --force linux >/dev/null || error "dist.sh did not succeed"
    WORKCRAFT="dist/linux/workcraft/workcraft"
fi

$WORKCRAFT -nogui -exec:<(echo "
    print('$SPECIAL_LINE');
    exit();
") \
| grep -q "$line" || error "Workcraft (release) did not start up correctly"
