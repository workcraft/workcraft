framework.addJavaScriptHelp("importFstSg", "sgFileName",
    "import an FST from the State Graph (*.sg) file 'sgFileName' and return its work");

function importFstSg(sgFileName) {
    if (!sgFileName.endsWith(".sg")) {
        throw("State Graph file '" + sgFileName + "' has incorrect extension, as '.sg' is expected");
    }
    return framework.importWork(sgFileName);
}


framework.addJavaScriptHelp("exportFstSg", "work, sgFileName",
    "export the FST 'work' as a State Graph (*.sg) file 'sgFileName'");

function exportFstSg(work, sgFileName) {
    if (!sgFileName.endsWith(".sg")) {
        throw("State Graph file '" + sgFileName + "' has incorrect extension, as '.sg' is expected");
    }
    framework.exportWork(work, sgFileName, 'SG');
}
