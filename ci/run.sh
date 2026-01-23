#!/bin/bash -e

# Error out, possibly with a message
function error() {
    echo "ERROR"
    echo >&2 "$@"
    if [[ -f $log_file ]]; then
        echo >&2 "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
        echo >&2 "`tail -n50 $log_file`"
    fi
    exit 1
}

# Get size of a file in bytes (don't use stat as it is not portable)
function size() {
    wc -c < $1
}

# Check equality to the reference file
function check_file_equality() {
    ref_file=$1
    cur_file="${ref_file%.ref}"
    [[ -f $cur_file ]] || error "Expected output file ${cur_file} not found"

    check_result=`diff <(sed 's/\\r$//g' $ref_file) <(sed 's/\\r$//g' $cur_file) | cat`

    if [ "$check_result" == "" ]; then
        rm -f $cur_file
    else
        error "
Files ${ref_file} (reference) and ${cur_file} (current) differ:
$check_result"
    fi
}

# Check inclusion of the reference file
function check_file_inclusion() {
    min_file=$1
    cur_file="${min_file%.min}"
    [[ -f $cur_file ]] || error "Expected output file ${cur_file} not found"
    check_result=`diff --unchanged-line-format= --old-line-format='%L' --new-line-format= <(sed 's/\\r$//g' $min_file) <(sed 's/\\r$//g' $cur_file) | cat`

    if [ "$check_result" == "" ]; then
        rm -f $cur_file
    else
        error "
Lines of ${min_file} (minimal) are not included in ${cur_file} (current):
$check_result"
    fi
}

function process_test() {
    if [[ $1 == ${DIR}/* ]]; then
        test_dir=$1
    else
        test_dir=${DIR}/$1
    fi

    if [[ -d ${test_dir} ]]; then
        script_file=${test_dir}/${SCRIPT_FILE}
        if [[ -f ${script_file} ]]; then
            log_file=${test_dir}/${LOG_FILE}

            echo -n "* $(basename ${test_dir}) ... "
            . ${script_file}

            for ref_file in ${test_dir}/${REF_FILE_PATTERN}; do
                [[ -f $ref_file ]] || continue
                check_file_equality "$ref_file"
            done

            for min_file in ${test_dir}/${MIN_FILE_PATTERN}; do
                [[ -f ${min_file} ]] || continue
                check_file_inclusion "$min_file"
            done
            rm -f ${log_file}
            echo "OK"
        fi
    fi
}

DIR="ci"
REF_FILE_PATTERN="*.ref"
MIN_FILE_PATTERN="*.min"
SCRIPT_FILE="test.sh"
LOG_FILE="workcraft.log"

# Change to Workcraft root directory
cd "$(dirname "$0")/.."

# Prepare directories for gate libraries and backend tools
[[ -e libraries ]] || ln -s dist/template/common/libraries
case $OSTYPE in
    darwin*)
        [[ -e tools ]] || ln -s dist/template/osx/Contents/Resources/tools
        ;;
    linux*)
        [[ -e tools ]] || ln -s dist/template/linux/tools
        ;;
    msys*)
        [[ -e tools ]] || cp -r dist/template/windows/tools .
        ;;
    *)
        error "Unsupported OS type $OSTYPE"
        ;;
esac

if [ $# -eq 0 ]; then
    test_dirs=${DIR}/*
else
    test_dirs=$@
fi

echo "Running integration tests:"
for test_dir in $test_dirs; do
    process_test ${test_dir}
done
echo "Success!"
