#!/bin/bash -e

err() { echo >&2 "$@"; exit 1; }

if [[ $OSTYPE == darwin* ]]; then
    ./dist.sh -f osx >/dev/null || err "dist.sh did not succeed"

    line=" == expected js line"

    dist/osx/Workcraft.app/Contents/MacOS/Workcraft -nogui -exec:<(echo "print('$line'); exit();") \
        | grep -q "$line" || err "Workcraft (release) did not start up correctly"
else
    ./dist.sh -f linux >/dev/null || err "dist.sh did not succeed"

    line=" == expected js line"

    dist/linux/workcraft/workcraft -nogui -exec:<(echo "print('$line'); exit();") \
        | grep -q "$line" || err "Workcraft (release) did not start up correctly"
fi
