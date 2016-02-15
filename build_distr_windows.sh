#!/usr/bin/env bash

template_dir="../../distr-template-windows"

./build_distr.sh -t $template_dir
rm -f  ${distr_dir}/workcraft
7z a -r "${distr_dir}.zip" $distr_dir
rm -rf $distr_dir
