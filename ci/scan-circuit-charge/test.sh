./gradlew run --args="-nogui -noconfig -dir:${test_dir} -exec:scan-individual.js" >${log_file}

./gradlew run --args="-nogui -noconfig -dir:${test_dir} -exec:scan-chain.js" >${log_file}
