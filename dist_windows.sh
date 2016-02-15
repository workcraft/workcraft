#!/usr/bin/bash -e

template_dir="dist-template/windows"
dist_dir="dist_windows"

./dist.sh -t $template_dir -d $dist_dir
rm -f  ${dist_dir}/workcraft
7z a -r "${dist_dir}.zip" $dist_dir
