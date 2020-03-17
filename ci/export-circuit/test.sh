./gradlew run --args="-nogui -noconfig -dir:${test_dir} -exec:vme-tm.circuit.js" >${log_file}

# Post-processing
result_file=${test_dir}/vme-tm.circuit.txt
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
report_file_header ${test_dir}/vme-tm.circuit.v 144 keep
report_file_header ${test_dir}/vme-tm.circuit.svg 155 remove
report_file_header ${test_dir}/vme-tm.circuit.png 8 remove
report_file_header ${test_dir}/vme-tm.circuit.pdf 9 remove
report_file_header ${test_dir}/vme-tm.circuit.eps 24 remove
report_file_header ${test_dir}/vme-tm.circuit.ps 14 remove
