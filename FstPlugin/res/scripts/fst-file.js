framework.addJavaScriptHelp("exportFstSg", "work, fileName",
    "export the specified FST 'work' as a State Graph (*.sg) 'fileName'");

function exportFstSg(work, path) {
    framework.exportWork(work, path, 'SG');
}
