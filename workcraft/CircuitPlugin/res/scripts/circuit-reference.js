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

function setCircuitEnvironment(work, env) {
    circuit = work.getModelEntry().getMathModel();
    circuit.setEnvironment(getFileReference(env, work));
}


framework.addJavaScriptHelp("getCircuitEnvironment", "work",
    "get environment STG file for Circuit 'work'");

function getCircuitEnvironment(work) {
    circuit = work.getModelEntry().getMathModel();
    return circuit.getEnvironmentFile();
}

framework.addJavaScriptHelp("setCircuitComponentRefinement", "work, ref, refinement",
    "set 'refinement' as refinement model for component 'ref' in Circuit 'work'");

function setCircuitComponentRefinement(work, ref, refinement) {
    circuit = work.getModelEntry().getMathModel();
    component = circuit.getNodeByReference(ref);
    if (!(component instanceof org.workcraft.plugins.circuit.CircuitComponent)) {
        throw "Circuit component '" + ref + "' not found";
    }
    component.setRefinement(getFileReference(refinement, work));
}


framework.addJavaScriptHelp("getCircuitComponentRefinement", "work, ref",
    "get file path of refinement model for component 'ref' in Circuit 'work'");

function getCircuitComponentRefinement(work, ref) {
    circuit = work.getModelEntry().getMathModel();
    component = circuit.getNodeByReference(ref);
    if (!(component instanceof org.workcraft.plugins.circuit.CircuitComponent)) {
        throw "Circuit component '" + ref + "' not found";
    }
    refinement = component.getRefinement();
    if (!(refinement instanceof org.workcraft.dom.references.FileReference)) {
        return null;
    }
    return refinement.getPath();
}
