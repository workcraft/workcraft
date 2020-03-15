work = load("charge-tm.circuit.work");
originalEnvFile = getCircuitEnvironment(work);

setCircuitEnvironment(work, "test.stg.work");
setAsStringEnvFile = getCircuitEnvironment(work);

envWork = load("charge-modified.stg.work");
setCircuitEnvironment(work, envWork);
setAsWorkEnvFile = getCircuitEnvironment(work);

setCircuitEnvironment(work, originalEnvFile);
setAsFileEnvFile = getCircuitEnvironment(work);

write(
    "Original environment: " + originalEnvFile.getName() + "\n" +
    "Modified environment (set as string): " + setAsStringEnvFile.getName() + "\n" +
    "Modified environment (set as work): " + setAsWorkEnvFile.getName() + "\n" +
    "Modified environment (set as file): " + setAsFileEnvFile.getName() + "\n",
    "charge-tm.circuit.txt");

exit();
