framework.addJavaScriptHelp("exportDfsVerilog", "work, fileName",
    "export the DFS 'work' as a Verilog netlist (*.v) file 'fileName'");

function exportDfsVerilog(work, fileName) {
    framework.exportWork(work, fileName, 'VERILOG');
}
