framework.addJavaScriptHelp("exportCircuitVerilog", "work, fileName",
    "export the specified Circuit 'work' as a Verilog netlist .v 'fileName'");

function exportCircuitVerilog(work, fileName) {
    framework.exportWork(work, fileName, 'VERILOG');
}
