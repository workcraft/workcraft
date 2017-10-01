framework.addJavaScriptHelp("exportFstSg", "export the specified FST 'work' as a State Graph (*.sg) 'fileName'");

function exportFstSg(work, path) {
    framework.exportWork(work, path, 'SG');
}
