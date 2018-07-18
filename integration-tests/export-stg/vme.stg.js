setConfigVar("CommonDebugSettings.shortExportHeader", "true");

we = load('vme.stg.work');

exportStgG(we, 'vme.stg.g');
exportStgLpn(we, 'vme.stg.lpn');

exportSvg(we, 'vme.stg.svg');
exportPng(we, 'vme.stg.png');
exportPdf(we, 'vme.stg.pdf');
exportEps(we, 'vme.stg.eps');
exportPs(we, 'vme.stg.ps');
exportDot(we, 'vme.stg.dot');

exit();
