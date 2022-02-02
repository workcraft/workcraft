framework.addJavaScriptHelp("setCircuitComponentRefinement", "circuitWork, componentRef, refinementPath",
    "set 'refinementPath' file as refinement for component 'componentRef' in Circuit 'circuitWork'");

function setCircuitComponentRefinement(circuitWork, componentRef, refinementPath) {
    circuitMathModel = circuitWork.getModelEntry().getMathModel();
    component = circuitMathModel.getNodeByReference(componentRef);
    if (!(component instanceof org.workcraft.plugins.circuit.CircuitComponent)) {
        throw "Circuit component '" + componentRef + "' not found";
    }
    refinement = org.workcraft.dom.references.FileReference();
    refinement.setPath(refinementPath);
    component.setRefinement(refinement);
}


framework.addJavaScriptHelp("getCircuitComponentRefinement", "circuitWork, componentRef",
    "get path to refinement file for component 'componentRef' in Circuit 'circuitWork'");

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
