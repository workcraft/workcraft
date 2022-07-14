setConfigVar("CommonDebugSettings.shortExportHeader", "true");

we = importCircuitVerilog("hier_buck_control.v");

exportCircuitVerilog(we, "hier_buck_control-out.v");

exit();
