framework.addJavaScriptHelp("importCircuitVerilog", "vFileName",
    "import a Circuit from the given Verilog netlist (*.v) file 'vFileName'and return its work");

function importCircuitVerilog(vFileName) {
    if (!vFileName.endsWith(".v")) {
        throw("Verilog netlist file '" + vFileName + "' has incorrect extension, as '.v' is expected");
    }
    return framework.loadWork(vFileName);
}

framework.addJavaScriptHelp("exportCircuitVerilog", "work, vFileName",
    "export the Circuit 'work' as a Verilog netlist (*.v) file 'vFileName'");

function exportCircuitVerilog(work, vFileName) {
    if (!vFileName.endsWith(".v")) {
        throw("Verilog netlist file '" + vFileName + "' has incorrect extension, as '.v' is expected");
    }
    framework.exportWork(work, vFileName, 'VERILOG');
}
