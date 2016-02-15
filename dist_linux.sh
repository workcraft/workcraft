#!/bin/sh -e

template_dir="dist-template/linux"
dist_dir="workcraft-linux"

./dist.sh -t $template_dir -d $dist_dir
rm -f  ${dist_dir}/workcraft.bat
tar czf "${dist_dir}.tar.gz" $dist_dir
