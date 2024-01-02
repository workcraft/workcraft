framework.addJavaScriptHelp("importCircuitVerilog", "vFileName, topModuleName",
    "import a Circuit 'topModuleName' (can be skipped for auto detection) with its dependencies from the given Verilog netlist (*.v) file 'vFileName' and return its work");

function importCircuitVerilog(vFileName, topModuleName) {
    if (!vFileName.endsWith(".v")) {
        throw("Verilog netlist file '" + vFileName + "' has incorrect extension, as '.v' is expected");
    }
    return (topModuleName == undefined) ? framework.importWork(vFileName)
        : framework.importWork(vFileName, topModuleName);
}


framework.addJavaScriptHelp("exportCircuitVerilog", "work, vFileName",
    "export the Circuit 'work' as a Verilog netlist (*.v) file 'vFileName'");

function exportCircuitVerilog(work, vFileName) {
    if (!vFileName.endsWith(".v")) {
        throw("Verilog file '" + vFileName + "' has incorrect extension, as '.v' is expected");
    }
    framework.exportWork(work, vFileName, 'VERILOG');
}


framework.addJavaScriptHelp("exportCircuitVerilogAssigns", "work, vFileName",
    "export the Circuit 'work' as a Verilog assigns (*.v) file 'vFileName'");

function exportCircuitVerilogAssign(work, vFileName) {
    if (!vFileName.endsWith(".v")) {
        throw("Verilog file '" + vFileName + "' has incorrect extension, as '.v' is expected");
    }
    framework.exportWork(work, vFileName, 'VERILOG ASSIGNS');
}
