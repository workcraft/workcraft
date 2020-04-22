framework.addJavaScriptHelp("exportStgG", "work, fileName",
    "export the STG 'work' as a Signal Transition Graph (*.g) file 'fileName'");

function exportStgG(work, fileName) {
    framework.exportWork(work, fileName, 'STG');
}


framework.addJavaScriptHelp("exportStgLpn", "work, fileName",
    "export the STG 'work' as a Labeled Petri Net (*.lpn) file 'fileName'");

function exportStgLpn(work, fileName) {
    framework.exportWork(work, fileName, 'LPN');
}
