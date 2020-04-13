framework.addJavaScriptHelp("exportCircuitVerilog", "work, fileName",
    "export the Circuit 'work' as a Verilog netlist (*.v) file 'fileName'");

function exportCircuitVerilog(work, fileName) {
    framework.exportWork(work, fileName, 'VERILOG');
}
