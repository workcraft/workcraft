// File operations

framework.addJavaScriptHelp("load", "load a model from the work file 'fileName' and return its work");

function load(fileName) {
    return framework.loadWork(fileName);
}


framework.addJavaScriptHelp("import", "import a model from a given 'fileName' (the model type is determined by extension) and return its work");

function import(fileName) {
    return framework.loadWork(fileName);
}


framework.addJavaScriptHelp("save", "save the given 'work' to the file 'fileName'");

function save(work, fileName) {
    framework.saveWork(work, fileName);
}


framework.addJavaScriptHelp("exportSvg", "export the specified 'work' as a Scalable Vector Graphics (*.svg) 'fileName'");

function exportSvg(work, fileName) {
    framework.exportWork(work, fileName, 'SVG');
}


framework.addJavaScriptHelp("exportPng", "export the specified 'work' as a Portable Network Graphics (*.png) 'fileName'");

function exportPng(work, fileName) {
    framework.exportWork(work, fileName, 'PNG');
}


framework.addJavaScriptHelp("exportPdf", "export the specified 'work' as a Portable Document Format (*.pdf) 'fileName'");

function exportPdf(work, fileName) {
    framework.exportWork(work, fileName, 'PDF');
}


framework.addJavaScriptHelp("exportPs", "export the specified 'work' as a PostScript (*.ps) 'fileName'");

function exportPs(work, fileName) {
    framework.exportWork(work, fileName, 'PS');
}


framework.addJavaScriptHelp("exportEps", "export the specified 'work' as an Encapsulated PostScript (*.eps) 'fileName'");

function exportEps(work, fileName) {
    framework.exportWork(work, fileName, 'EPS');
}


framework.addJavaScriptHelp("exportDot", "export the specified 'work' as a GraphViz (*.dot) 'fileName'");

function exportDot(work, fileName) {
    framework.exportWork(work, fileName, 'DOT');
}
