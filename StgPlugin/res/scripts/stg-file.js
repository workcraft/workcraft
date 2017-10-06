framework.addJavaScriptHelp("exportStgG", "work, fileName",
    "export the specified STG 'work' as a Signal Transition Graph (*.g) 'fileName'");

function exportStgG(work, fileName) {
    framework.exportWork(work, fileName, 'STG');
}


framework.addJavaScriptHelp("exportStgLpn", "work, fileName",
    "export the specified STG 'work' as a Labeled Petri Net (*.lpn) 'fileName'");

function exportStgLpn(work, fileName) {
    framework.exportWork(work, fileName, 'LPN');
}
