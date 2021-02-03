args=""
for path in ${test_dir}/*.work; do
    [[ -f $path ]] || continue
    work_file="$(basename ${path})"
    name="${work_file%.work}"
    args="${args} "$name
done

./gradlew run --args="-nogui -noconfig -dir:${test_dir} -exec:test.js $args" >${log_file}
