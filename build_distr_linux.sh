#!/usr/bin/env bash

src_dir="."
template_dir="../../distr-template-linux"

./build_distr.sh -s $src_dir -t $template_dir
rm -f  ${distr_dir}/workcraft.bat
tar czf "${distr_dir}.tar.gz" $distr_dir
rm -rf $distr_dir
