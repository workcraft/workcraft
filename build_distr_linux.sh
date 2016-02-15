#!/usr/bin/bash -e

template_dir="../../distr-template-linux"

./build_distr.sh -t $template_dir
rm -f  ${distr_dir}/workcraft.bat
tar czf "${distr_dir}.tar.gz" $distr_dir
rm -rf $distr_dir
