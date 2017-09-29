#!/bin/bash -e

# Error out, possibly with a message
function error() {
    echo " ... ERROR"
    echo >&2 "$@"
    exit 1
}

# Get size of a file in bytes (don't use stat as it is not portable)
function size() {
    wc -c < $1
}

DIR="integration-tests"
TEST_DIR_PATTERN="*"
TEST_FILE_PATTERN="*.sh"
REF_FILE_PATTERN="*.ref"

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

    echo -n "* ${test_dir}:"

    for test_file in ${test_dir}/${TEST_FILE_PATTERN}; do
        [[ -f ${test_file} ]] || continue

        echo -n " $(basename ${test_file})"
        . ${test_file}
    done

    for ref_file in ${test_dir}/${REF_FILE_PATTERN}; do
        [[ -f $ref_file ]] || continue

        cur_file="${ref_file%.ref}"
        [[ -f $cur_file ]] || error "Expected output file ${cur_file} not found"

        ref_size=$(size $ref_file)
        cur_size=$(size $cur_file)

        if [[ $ref_size == $cur_size ]]; then
            # Remove temporary files if the test succeeded
            rm -f $cur_file
        else
            # Note the new line before diff output
            error $"Files ${ref_file} (reference) and ${cur_file} (current) differ:
`diff $ref_file $cur_file`"
        fi
    done
    echo "... OK"
done
