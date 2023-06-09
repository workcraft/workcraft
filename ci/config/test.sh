# Base config defined via CLI
cp -f ${test_dir}/config-base-cli.xml.min ${test_dir}/config-base-cli.xml
./gradlew run --args="-nogui -config:config-base-cli.xml -dir:${test_dir} -exec:'exit();'" >${log_file}

# Additional config and new resultant config defined via CLI
cp -f ${test_dir}/config-new-cli.xml.min ${test_dir}/config-additional-cli.xml
./gradlew run --args="-nogui -noconfig-load -config:config-new-cli.xml -config-add:config-additional-cli.xml -dir:${test_dir} -exec:'exit();'" >${log_file}
rm ${test_dir}/config-additional-cli.xml

# Base config defined via ENV
cp -f ${test_dir}/config-base-env.xml.min ${test_dir}/config-base-env.xml
export WORKCRAFT_CONFIG=config-base-env.xml
./gradlew run --args="-nogui -dir:${test_dir} -exec:'exit();'" >${log_file}

# Additional config and new resultant config defined via ENV
cp -f ${test_dir}/config-new-env.xml.min ${test_dir}/config-additional-env.xml
export WORKCRAFT_CONFIG=config-new-env.xml
export WORKCRAFT_CONFIG_ADD=config-additional-env.xml
./gradlew run --args="-nogui -noconfig-load -dir:${test_dir} -exec:'exit();'" >${log_file}
unset WORKCRAFT_CONFIG
unset WORKCRAFT_CONFIG_ADD
rm ${test_dir}/config-additional-env.xml
