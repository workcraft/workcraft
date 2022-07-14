// File operations

framework.addJavaScriptHelp("load", "fileName",
    "load a model from the given work file 'fileName' and return its work");

function load(fileName) {
    return framework.loadWork(fileName);
}


framework.addJavaScriptHelp("save", "work, fileName",
    "save the model 'work' to a file 'fileName'");

function save(work, fileName) {
    framework.saveWork(work, fileName);
}


framework.addJavaScriptHelp("exportSvg", "work, fileName",
    "export the model 'work' as a Scalable Vector Graphics (*.svg) file 'fileName'");

function exportSvg(work, fileName) {
    framework.exportWork(work, fileName, 'SVG');
}


framework.addJavaScriptHelp("exportPng", "work, fileName",
    "export the model 'work' as a Portable Network Graphics (*.png) file 'fileName'");

function exportPng(work, fileName) {
    framework.exportWork(work, fileName, 'PNG');
}


framework.addJavaScriptHelp("exportPdf", "work, fileName",
    "export the model 'work' as a Portable Document Format (*.pdf) file 'fileName'");

function exportPdf(work, fileName) {
    framework.exportWork(work, fileName, 'PDF');
}


framework.addJavaScriptHelp("exportPs", "work, fileName",
    "export the model 'work' as a PostScript (*.ps) file 'fileName'");

function exportPs(work, fileName) {
    framework.exportWork(work, fileName, 'PS');
}


framework.addJavaScriptHelp("exportEps", "work, fileName",
    "export the model 'work' as an Encapsulated PostScript (*.eps) file 'fileName'");

function exportEps(work, fileName) {
    framework.exportWork(work, fileName, 'EPS');
}


framework.addJavaScriptHelp("exportDot", "work, fileName",
    "export the model 'work' as a GraphViz (*.dot) file 'fileName'");

function exportDot(work, fileName) {
    framework.exportWork(work, fileName, 'DOT');
}
