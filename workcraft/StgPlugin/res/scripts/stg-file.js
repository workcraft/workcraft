framework.addJavaScriptHelp("importStgG", "gFileName",
    "import an STG from the Signal Transition Graph (*.g) file 'gFileName' and return its work");

function importStgG(gFileName) {
    if (!gFileName.endsWith(".g")) {
        throw("Signal Transition Graph file '" + gFileName + "' has incorrect extension, as '.g' is expected");
    }
    return framework.importWork(gFileName);
}


framework.addJavaScriptHelp("exportStgG", "work, gFileName",
    "export the STG 'work' as a Signal Transition Graph (*.g) file 'gFileName'");

function exportStgG(work, gFileName) {
    if (!gFileName.endsWith(".g")) {
        throw("Signal Transition Graph file '" + gFileName + "' has incorrect extension, as '.g' is expected");
    }
    framework.exportWork(work, gFileName, 'STG');
}


framework.addJavaScriptHelp("importStgLpn", "lpnFileName",
    "import an STG from the Labeled Petri Net (*.lpn) file 'lpnFileName' and return its work");

function importStgLpn(lpnFileName) {
    if (!lpnFileName.endsWith(".lpn")) {
        throw("Labeled Petri Net file '" + lpnFileName + "' has incorrect extension, as '.lpn' is expected");
    }
    return framework.importWork(lpnFileName);
}


framework.addJavaScriptHelp("exportStgLpn", "work, lpnFileName",
    "export the STG 'work' as a Labeled Petri Net (*.lpn) file 'lpnFileName'");

function exportStgLpn(work, lpnFileName) {
    if (!lpnFileName.endsWith(".lpn")) {
        throw("Labeled Petri Net file '" + lpnFileName + "' has incorrect extension, as '.lpn' is expected");
    }
    framework.exportWork(work, lpnFileName, 'LPN');
}
