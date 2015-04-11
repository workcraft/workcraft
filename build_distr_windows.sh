#!/usr/bin/env bash

src_dir="."
distr_dir="../../workcraft_3.0.4"
template_dir="../../distr-template-windows"

./build_distr.sh -s $src_dir -d $distr_dir -t $template_dir
rm -f  ${distr_dir}/workcraft
7z a -r "${distr_dir}.zip" $distr_dir
rm -rf $distr_dir
