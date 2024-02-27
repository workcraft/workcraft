setConfigVar("CommonEditorSettings.exportHeaderStyle", "BRIEF");

we = load("vme-tm.circuit.work");

exportCircuitVerilog(we, "vme-tm.circuit.v");
exportCircuitSystemVerilogAssigns(we, "vme-assign.circuit.sv");

exportSvg(we, "vme-tm.circuit.svg");
exportPng(we, "vme-tm.circuit.png");
exportPdf(we, "vme-tm.circuit.pdf");
exportEps(we, "vme-tm.circuit.eps");
exportPs(we, "vme-tm.circuit.ps");

exit();
