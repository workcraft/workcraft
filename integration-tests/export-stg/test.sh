./workcraft -nogui -dir:${test_dir} -exec:vme.stg.js >${log_file}

# Post-processing
result_file=${test_dir}/vme.stg.txt
function report_file_header() {
    file_name=$1
    header_size=$2
    options=$3
    if [[ -e $file_name ]]; then
        header=$(head -c $header_size $file_name)
    fi
    echo "$(basename $file_name):" >> ${result_file}
    echo "${header}" >> ${result_file}
    echo >> ${result_file}
    if [[ "$options" == "remove" ]]; then
        rm -f $file_name
    fi
}

rm -f ${result_file}
report_file_header ${test_dir}/vme.stg.g 128 keep
report_file_header ${test_dir}/vme.stg.lpn 127 keep
report_file_header ${test_dir}/vme.stg.svg 155 remove
report_file_header ${test_dir}/vme.stg.png 8 remove
report_file_header ${test_dir}/vme.stg.pdf 9 remove
report_file_header ${test_dir}/vme.stg.eps 24 remove
report_file_header ${test_dir}/vme.stg.ps 14 remove
report_file_header ${test_dir}/vme.stg.dot 86 remove
