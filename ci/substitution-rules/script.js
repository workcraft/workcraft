setConfigVar("CommonDebugSettings.shortExportHeader", "true");

setConfigVar("CircuitSettings.invertImportSubstitutionRules", "true");
setConfigVar("CircuitSettings.invertExportSubstitutionRules", "false");

// Workcraft library Verilog
setConfigVar("CircuitSettings.importSubstitutionLibrary", "");
we = importCircuitVerilog("vme-tm.workcraft.v");

setConfigVar("CircuitSettings.exportSubstitutionLibrary", "");
exportCircuitVerilog(we, "vme-tm.workcraft-workcraft.v");

setConfigVar("CircuitSettings.exportSubstitutionLibrary", "libraries/workcraft-tsmc_ghp.cnv");
exportCircuitVerilog(we, "vme-tm.workcraft-tsmc_ghp.v");

setConfigVar("CircuitSettings.exportSubstitutionLibrary", "libraries/workcraft-tsmc_bcd.cnv");
exportCircuitVerilog(we, "vme-tm.workcraft-tsmc_bcd.v");

// TSMC GHP library Verilog
setConfigVar("CircuitSettings.importSubstitutionLibrary", "libraries/workcraft-tsmc_ghp.cnv");
we = importCircuitVerilog("vme-tm.tsmc_ghp.v");

setConfigVar("CircuitSettings.exportSubstitutionLibrary", "");
exportCircuitVerilog(we, "vme-tm.tsmc_ghp-workcraft.v");

setConfigVar("CircuitSettings.exportSubstitutionLibrary", "libraries/workcraft-tsmc_ghp.cnv");
exportCircuitVerilog(we, "vme-tm.tsmc_ghp-tsmc_ghp.v");

setConfigVar("CircuitSettings.exportSubstitutionLibrary", "libraries/workcraft-tsmc_bcd.cnv");
exportCircuitVerilog(we, "vme-tm.tsmc_ghp-tsmc_bcd.v");

// TSMC BCD library Verilog
setConfigVar("CircuitSettings.importSubstitutionLibrary", "libraries/workcraft-tsmc_bcd.cnv");
we = importCircuitVerilog("vme-tm.tsmc_bcd.v");

setConfigVar("CircuitSettings.exportSubstitutionLibrary", "");
exportCircuitVerilog(we, "vme-tm.tsmc_bcd-workcraft.v");

setConfigVar("CircuitSettings.exportSubstitutionLibrary", "libraries/workcraft-tsmc_ghp.cnv");
exportCircuitVerilog(we, "vme-tm.tsmc_bcd-tsmc_ghp.v");

setConfigVar("CircuitSettings.exportSubstitutionLibrary", "libraries/workcraft-tsmc_bcd.cnv");
exportCircuitVerilog(we, "vme-tm.tsmc_bcd-tsmc_bcd.v");

exit();
