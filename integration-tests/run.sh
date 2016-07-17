#!/bin/bash -e

dir="integration-tests"

[[ $(basename $PWD) == $dir ]] && cd ..

for f in $dir/test-*; do
    echo $f
    . $f
done
