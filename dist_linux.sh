#!/bin/sh -e

platform="linux"
dist_dir="workcraft-$platform"

./dist.sh $platform
rm -f  ${dist_dir}/workcraft.bat
tar czf "${dist_dir}.tar.gz" $dist_dir
