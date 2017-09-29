// File operations

function load(path) {
    return framework.loadWork(path);
}

function import(path) {
    return framework.loadWork(path);
}

function save(work, path) {
    framework.saveWork(work, path);
}

function exportSvg(work, path) {
    framework.exportWork(work, path, 'SVG');
}

function exportPng(work, path) {
    framework.exportWork(work, path, 'PNG');
}

function exportPdf(work, path) {
    framework.exportWork(work, path, 'PDF');
}

function exportPs(work, path) {
    framework.exportWork(work, path, 'PS');
}

function exportEps(work, path) {
    framework.exportWork(work, path, 'EPS');
}

function exportDot(work, path) {
    framework.exportWork(work, path, 'DOT');
}
