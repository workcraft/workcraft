#!/bin/bash -e

SPECIAL_LINE=" == expected js line"

if [[ $OSTYPE == darwin* ]]; then
    ./dist.sh --force osx >/dev/null || error "dist.sh did not succeed"
    workcraft="dist/osx/Workcraft.app/Contents/MacOS/Workcraft"
else
    ./dist.sh --force linux >/dev/null || error "dist.sh did not succeed"
    workcraft="dist/linux/workcraft/workcraft"
fi

$workcraft -nogui -exec:<(echo "
    print('$SPECIAL_LINE');
    exit();
") \
| grep -q "$line" || error "Workcraft (release) did not start up correctly"
