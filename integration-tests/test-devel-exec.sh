#!/bin/bash -e

err() { echo >&2 "$@"; exit 1; }

line=" == expected js line"

./workcraft -nogui -exec:<(echo "println('$line'); exit();") \
    | grep -q "$line" || err "Workcraft (dev) did not start up correctly"

