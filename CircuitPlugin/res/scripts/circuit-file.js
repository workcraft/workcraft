framework.addJavaScriptHelp("exportCircuitVerilog", "export the specified Circuit 'work' as a Verilog netlist .v 'fileName'");

function exportCircuitVerilog(work, fileName) {
    framework.exportWork(work, fileName, 'VERILOG');
}


// [UNDER CONSTRUCTION] framework.addJavaScriptHelp("exportCircuitSdc", "export the specified Circuit 'work' constraints as a Synopsys Design Constraints .sdc 'fileName'");

function exportCircuitSdc(work, fileName) {
    framework.exportWork(work, fileName, 'SDC');
}
