// File operations

framework.addJavaScriptHelp("load", "fileName",
    "load a model from the given work file 'fileName' and return its work");

function load(fileName) {
    return framework.loadWork(fileName);
}


framework.addJavaScriptHelp("import", "fileName",
    "import a model from the given file 'fileName' (the model type is determined by its extension) and return its work");

function import(fileName) {
    return framework.loadWork(fileName);
}


framework.addJavaScriptHelp("save", "work, fileName",
    "save the specified 'work' to a file with the given 'fileName'");

function save(work, fileName) {
    framework.saveWork(work, fileName);
}


framework.addJavaScriptHelp("exportSvg", "work, fileName",
    "export the specified 'work' as a Scalable Vector Graphics (*.svg) with the given 'fileName'");

function exportSvg(work, fileName) {
    framework.exportWork(work, fileName, 'SVG');
}


framework.addJavaScriptHelp("exportPng", "work, fileName",
    "export the specified 'work' as a Portable Network Graphics (*.png) with the given 'fileName'");

function exportPng(work, fileName) {
    framework.exportWork(work, fileName, 'PNG');
}


framework.addJavaScriptHelp("exportPdf", "work, fileName",
    "export the specified 'work' as a Portable Document Format (*.pdf) with the given 'fileName'");

function exportPdf(work, fileName) {
    framework.exportWork(work, fileName, 'PDF');
}


framework.addJavaScriptHelp("exportPs", "work, fileName",
    "export the specified 'work' as a PostScript (*.ps) with the given 'fileName'");

function exportPs(work, fileName) {
    framework.exportWork(work, fileName, 'PS');
}


framework.addJavaScriptHelp("exportEps", "work, fileName",
    "export the specified 'work' as an Encapsulated PostScript (*.eps) with the given 'fileName'");

function exportEps(work, fileName) {
    framework.exportWork(work, fileName, 'EPS');
}


framework.addJavaScriptHelp("exportDot", "work, fileName",
    "export the specified 'work' as a GraphViz (*.dot) with the given 'fileName'");

function exportDot(work, fileName) {
    framework.exportWork(work, fileName, 'DOT');
}
