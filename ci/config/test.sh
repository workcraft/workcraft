# Test local config file
cp -f ${test_dir}/config-local.xml.min ${test_dir}/config-local.xml
./gradlew run --args="-nogui -config:config-local.xml -dir:${test_dir} -exec:'exit();'" >${log_file}
