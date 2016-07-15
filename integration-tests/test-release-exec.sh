#!/bin/bash -e

err() { echo >&2 "$@"; exit 1; }

if [[ $OSTYPE == darwin* ]]; then
    ./dist.sh -f osx >/dev/null || err "dist.sh did not succeed"

    cd dist/osx/Workcraft.app/Contents/MacOS

    line=" == expected js line"

    ./Workcraft -nogui -exec:<(echo "println('$line'); exit();") \
        | grep -q "$line" || err "Workcraft (release) did not start up correctly"
else
    ./dist.sh -f linux >/dev/null || err "dist.sh did not succeed"

    cd dist/linux/workcraft

    line=" == expected js line"

    ./workcraft -nogui -exec:<(echo "println('$line'); exit();") \
        | grep -q "$line" || err "Workcraft (release) did not start up correctly"
fi
