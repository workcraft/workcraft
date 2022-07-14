framework.addJavaScriptHelp("exportDfsVerilog", "work, vFileName",
    "export the DFS 'work' as a Verilog netlist (*.v) file 'vFileName'");

function exportDfsVerilog(work, vFileName) {
    if (!vFileName.endsWith(".v")) {
        throw("Verilog netlist file '" + vFileName + "' has incorrect extension, as '.v' is expected");
    }
    framework.exportWork(work, vFileName, 'VERILOG');
}
