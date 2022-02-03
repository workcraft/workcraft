framework.addJavaScriptHelp("setCircuitEnvironment", "work, env",
    "set 'env' STG file or work as environment for Circuit 'work'");

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

    circuitMathModel = circuitWork.getModelEntry().getMathModel();
    envFile = getEnvironmentFile(env);
    circuitMathModel.setEnvironmentFile(envFile);
}


framework.addJavaScriptHelp("getCircuitEnvironment", "work",
    "get environment STG file for Circuit 'work'");

function getCircuitEnvironment(circuitWork) {
    circuitMathModel = circuitWork.getModelEntry().getMathModel();
    return circuitMathModel.getEnvironmentFile();
}
