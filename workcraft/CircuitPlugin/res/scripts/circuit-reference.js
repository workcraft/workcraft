// Get file FileReference object for a given file path and base WorkspaceEntry
function getFileReference(value, baseWork) {

    // Get a file path for a givent String, File or WorkspaceEntry
    function getFilePath(value) {
        if ((value instanceof String) || (typeof value === "string")) {
            return value;
        }
        if (value instanceof File) {
            return value.getPath();
        } 
        if (value instanceof org.workcraft.workspace.WorkspaceEntry) {
            return framework.getWorkspace().getFile(value).getPath();
        }
        throw "File path must be specified as String, File or WorkspaceEntry";
    }

    result = org.workcraft.dom.references.FileReference();
    baseFile = framework.getWorkspace().getFile(baseWork);
    basePath = org.workcraft.utils.FileUtils.getBasePath(baseFile);
    result.setBase(basePath);
    result.setPath(getFilePath(value));
    return result;
}

framework.addJavaScriptHelp("setCircuitEnvironment", "work, env",
    "set 'env' STG file or work as environment for Circuit 'work'");

function setCircuitEnvironment(circuitWork, env) {
    circuitMathModel = circuitWork.getModelEntry().getMathModel();
    circuitMathModel.setEnvironment(getFileReference(env, circuitWork));
}


framework.addJavaScriptHelp("getCircuitEnvironment", "work",
    "get environment STG file for Circuit 'work'");

function getCircuitEnvironment(circuitWork) {
    circuitMathModel = circuitWork.getModelEntry().getMathModel();
    return circuitMathModel.getEnvironmentFile();
}

framework.addJavaScriptHelp("setCircuitComponentRefinement", "circuitWork, componentRef, refinement",
    "set 'refinement' as refinement model for component 'componentRef' in Circuit 'circuitWork'");

function setCircuitComponentRefinement(circuitWork, componentRef, refinement) {
    circuitMathModel = circuitWork.getModelEntry().getMathModel();
    component = circuitMathModel.getNodeByReference(componentRef);
    if (!(component instanceof org.workcraft.plugins.circuit.CircuitComponent)) {
        throw "Circuit component '" + componentRef + "' not found";
    }
    component.setRefinement(getFileReference(refinement, circuitWork));
}


framework.addJavaScriptHelp("getCircuitComponentRefinement", "circuitWork, componentRef",
    "get file path of refinement model for component 'componentRef' in Circuit 'circuitWork'");

function getCircuitComponentRefinement(circuitWork, componentRef) {
    circuitMathModel=circuitWork.getModelEntry().getMathModel();
    component = circuitMathModel.getNodeByReference(componentRef);
    if (!(component instanceof org.workcraft.plugins.circuit.CircuitComponent)) {
        throw "Circuit component '" + componentRef + "' not found";
    }
    refinement = component.getRefinement();
    if (!(refinement instanceof org.workcraft.dom.references.FileReference)) {
        return null;
    }
    return refinement.getPath();
}
