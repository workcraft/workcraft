SPECIAL_LINE=" == expected js line"

# Development version
./workcraft -exec:"print('$SPECIAL_LINE'); exit();" \
| grep -q "$SPECIAL_LINE" || error "Workcraft (development) did not start up correctly"


# Release version
if [[ $OSTYPE == darwin* ]]; then
    dist/run.sh --force osx >/dev/null || error "dist/run.sh did not succeed"
    WORKCRAFT="dist/result/osx/Workcraft.app/Contents/MacOS/Workcraft"
else
    dist/run.sh --force linux >/dev/null || error "dist/run.sh did not succeed"
    WORKCRAFT="dist/result/linux/workcraft/workcraft"
fi

$WORKCRAFT -exec:"print('$SPECIAL_LINE'); exit();" \
| grep -q "$line" || error "Workcraft (release) did not start up correctly"
