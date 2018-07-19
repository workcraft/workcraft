args="-missing"
for path in ${test_dir}/*.g; do
    [[ -f $path ]] || continue
    g_file="$(basename ${path})"
    name="${g_file%.g}"
    args="${args} "$name
done

./workcraft -nogui -noconfig -dir:${test_dir} -exec:test.js $args >${log_file}

# Clean up
rm -f ${test_dir}/*.work
