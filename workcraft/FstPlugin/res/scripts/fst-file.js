framework.addJavaScriptHelp("exportFstSg", "work, fileName",
    "export the FST 'work' as a State Graph (*.sg) file 'fileName'");

function exportFstSg(work, path) {
    framework.exportWork(work, path, 'SG');
}
