setConfigVar("CommonDebugSettings.shortExportHeader", "true");

we = import("hier_buck_control.v");

exportCircuitVerilog(we, "hier_buck_control-out.v");

exit();
