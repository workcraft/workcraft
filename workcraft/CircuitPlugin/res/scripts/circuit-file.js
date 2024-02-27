framework.addJavaScriptHelp("importCircuitVerilog", "vFileName, topModuleName",
    "import a Circuit 'topModuleName' (can be skipped for auto detection) with its dependencies from the given Verilog netlist (*.v) file 'vFileName' and return its work");

function importCircuitVerilog(vFileName, topModuleName) {
    if (!vFileName.endsWith(".v")) {
        throw("Verilog file '" + vFileName + "' has incorrect extension, as '.v' is expected");
    }
    return (topModuleName == undefined) ? framework.importWork(vFileName)
        : framework.importWork(vFileName, topModuleName);
}

framework.addJavaScriptHelp("importCircuitSystemVerilogAssigns", "svFileName, topModuleName",
    "import a Circuit 'topModuleName' (can be skipped for auto detection) with its dependencies from the given System Verilog (*.sv) file 'svFileName' and return its work");

function importCircuitSystemVerilogAssigns(svFileName, topModuleName) {
    if (!svFileName.endsWith(".sv")) {
        throw("System Verilog file '" + svFileName + "' has incorrect extension, as '.sv' is expected");
    }
    return (topModuleName == undefined) ? framework.importWork(svFileName)
        : framework.importWork(svFileName, topModuleName);
}


framework.addJavaScriptHelp("exportCircuitVerilog", "work, vFileName",
    "export the Circuit 'work' as a Verilog netlist (*.v) file 'vFileName'");

function exportCircuitVerilog(work, vFileName) {
    if (!vFileName.endsWith(".v")) {
        throw("Verilog file '" + vFileName + "' has incorrect extension, as '.v' is expected");
    }
    framework.exportWork(work, vFileName, 'VERILOG');
}


framework.addJavaScriptHelp("exportCircuitSystemVerilogAssigns", "work, svFileName",
    "export the Circuit 'work' as a System Verilog (*.sv) file 'svFileName' with assign statements");

function exportCircuitSystemVerilogAssigns(work, svFileName) {
    if (!svFileName.endsWith(".sv")) {
        throw("System Verilog file '" + svFileName + "' has incorrect extension, as '.sv' is expected");
    }
    framework.exportWork(work, svFileName, 'SYSTEM VERILOG ASSIGNS');
}
