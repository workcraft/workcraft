# Base config defined via CLI
cp -f ${test_dir}/config-base.xml.min ${test_dir}/config-base.xml
./gradlew run --args="-nogui -config:config-base.xml -dir:${test_dir} -exec:'exit();'" >${log_file}

# Base config and additional config defined via CLI
./gradlew run --args="-nogui -config:config-new.xml -config-add:config-additional.xml -dir:${test_dir} -exec:'exit();'" >${log_file}

# Several additional configs without base config and new resultant config defined via CLI
./gradlew run --args="-nogui -noconfig-load -config:config-new-extra.xml -config-add:config-additional.xml -config-add:config-additional-extra.xml -dir:${test_dir} -exec:'exit();'" >${log_file}

# Base config defined via ENV
cp -f ${test_dir}/config-env-base.xml.min ${test_dir}/config-env-base.xml
export WORKCRAFT_CONFIG=config-env-base.xml
./gradlew run --args="-nogui -dir:${test_dir} -exec:'exit();'" >${log_file}

# Additional config without base config and new resultant config defined via ENV
export WORKCRAFT_CONFIG=config-env-new.xml
export WORKCRAFT_CONFIG_ADD=config-env-additional.xml
./gradlew run --args="-nogui -noconfig-load -dir:${test_dir} -exec:'exit();'" >${log_file}
unset WORKCRAFT_CONFIG
unset WORKCRAFT_CONFIG_ADD
