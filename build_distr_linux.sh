#!/usr/bin/env bash

src_dir="."
distr_dir="../../workcraft_3.0.7"
template_dir="../../distr-template-linux"

./build_distr.sh -s $src_dir -d $distr_dir -t $template_dir
rm -f  ${distr_dir}/workcraft.bat
tar czf "${distr_dir}.tar.gz" $distr_dir
rm -rf $distr_dir
