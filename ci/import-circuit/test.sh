# Remove work files.
# This has to be done both before and after the scipt
# (hierarhical Verilog import fails if there is a file name clash)
function removeGeneratedWorkFiles() {
    rm -f ${test_dir}/CYCLE_CTRL.work
    rm -f ${test_dir}/CYCLE.work
    rm -f ${test_dir}/CHARGE_CTRL.work
    rm -f ${test_dir}/CHARGE.work
    rm -f ${test_dir}/WAIT2.work
}

removeGeneratedWorkFiles

./gradlew run --args="-nogui -noconfig -dir:${test_dir} -exec:script.js" >${log_file}

removeGeneratedWorkFiles
