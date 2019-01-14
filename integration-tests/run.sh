#!/bin/bash -e

# Error out, possibly with a message
function error() {
    echo "ERROR"
    echo >&2 "$@"
    exit 1
}

# Get size of a file in bytes (don't use stat as it is not portable)
function size() {
    wc -c < $1
}

if [ -z "$@" ]; then
    TEST_DIR_PATTERN="*"
else
    TEST_DIR_PATTERN="$@"
fi
DIR="integration-tests"
REF_FILE_PATTERN="*.ref"
SCRIPT_FILE="test.sh"
LOG_FILE="workcraft.log"

[[ $(basename $PWD) == $DIR ]] && cd ..

# Symbolic links to libraries and tools directories
if [[ $OSTYPE == darwin* ]]; then
   [[ -e libraries ]] || ln -s dist-template/osx/Contents/Resources/libraries
   [[ -e tools ]] || ln -s dist-template/osx/Contents/Resources/tools
else
   [[ -e libraries ]] || ln -s dist-template/linux/libraries
   [[ -e tools ]] || ln -s dist-template/linux/tools
fi

echo "Running integration tests:"
for test_dir in ${DIR}/${TEST_DIR_PATTERN}; do
    [[ -d $test_dir ]] || continue

    script_file=${test_dir}/${SCRIPT_FILE}
    [[ -f ${script_file} ]] || continue

    log_file=${test_dir}/${LOG_FILE}

    echo -n "* $(basename ${test_dir}) ... "
    . ${script_file}

    for ref_file in ${test_dir}/${REF_FILE_PATTERN}; do
        [[ -f $ref_file ]] || continue

        cur_file="${ref_file%.ref}"
        [[ -f $cur_file ]] || error "Expected output file ${cur_file} not found"

        if diff -q $ref_file $cur_file &> /dev/null; then
            # Remove temporary files if the test succeeded
            rm -f $cur_file
        else
            # Note the new line before diff output
            error "
Files ${ref_file} (reference) and ${cur_file} (current) differ:
`diff $ref_file $cur_file`
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
`tail -n50 $log_file`"
        fi
    done
    rm -f ${log_file}
    echo "OK"
done
echo "Success!"
