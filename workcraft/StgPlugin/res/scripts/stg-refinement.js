framework.addJavaScriptHelp("setStgRefinement", "stgWork, refinementPath",
    "set 'refinementPath' file as refinement for STG 'stgWork'");

function setStgRefinement(stgWork, refinementPath) {
    stgMathModel = stgWork.getModelEntry().getMathModel();
    refinement = org.workcraft.dom.references.FileReference();
    refinement.setPath(refinementPath);
    stgMathModel.setRefinement(refinement);
}


framework.addJavaScriptHelp("getStgRefinement", "stgWork",
    "get path to refinement file for STG 'stgWork'");

function getStgRefinement(stgWork) {
    stgMathModel = stgWork.getModelEntry().getMathModel();
    refinement = stgMathModel.getRefinement();
    if (!(refinement instanceof org.workcraft.dom.references.FileReference)) {
        return null;
    }
    return refinement.getPath();
}
