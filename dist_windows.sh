#!/bin/sh -e

platform="windows"
dist_dir="workcraft-$platform"

./dist.sh $platform
rm -f  ${dist_dir}/workcraft
7z a -r "${dist_dir}.zip" $dist_dir
