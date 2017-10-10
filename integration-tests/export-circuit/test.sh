script_file=${test_dir}/vme-tm.circuit.js

[[ -e $script_file ]] || error "Script file ${script_file} is missing"

./workcraft -nogui -dir:${test_dir} -exec:${script_file} >${log_file}

# Post-processing
result_file=${test_dir}/vme-tm.circuit.txt
function report_file_size() {
    if [[ -e $1 ]]; then
        file_size=$(size $1)
        file_kb_size=$(( ($file_size + 512) / 1024 ))
        echo "$(basename $1) ${file_kb_size}KB" >> ${result_file}
        rm -f $1
    fi
}

rm -f ${result_file}
report_file_size ${test_dir}/vme-tm.circuit.svg
report_file_size ${test_dir}/vme-tm.circuit.png
report_file_size ${test_dir}/vme-tm.circuit.pdf
report_file_size ${test_dir}/vme-tm.circuit.eps
report_file_size ${test_dir}/vme-tm.circuit.ps
