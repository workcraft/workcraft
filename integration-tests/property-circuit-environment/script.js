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

function setCircuitEnvironment(circuitWork, env) {
    // Helper function to convert env to a File object
    function getEnvironmentFile(env) {
        if (env instanceof File) {
            return env;
        }
        if ((env instanceof String) || (typeof env === "string")) {
            return new File(env);
        }
        if (env instanceof org.workcraft.workspace.WorkspaceEntry) {
            return getWorkFile(env);
        }
        throw "Environment must be specified as File, String or WorkspaceEntry";
    }

    circuitMathModel=circuitWork.getModelEntry().getMathModel();
    envFile = getEnvironmentFile(env);
    org.workcraft.plugins.circuit.utils.EnvironmentUtils.setEnvironmentFile(circuitMathModel, envFile);
}

function getCircuitEnvironment(circuitWork) {
    circuitMathModel=circuitWork.getModelEntry().getMathModel();
    return org.workcraft.plugins.circuit.utils.EnvironmentUtils.getEnvironmentFile(circuitMathModel);
}
