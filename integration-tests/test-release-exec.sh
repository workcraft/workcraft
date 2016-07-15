#!/bin/bash -e

err() { echo >&2 "$@"; exit 1; }

./dist.sh linux >/dev/null || err "dist.sh did not succeed"

cd dist/linux/workcraft

line=" == expected js line"

./workcraft -nogui -exec:<(echo "println('$line'); exit();") \
    | grep -q "$line" || err "Workcraft (release) did not start up correctly"
