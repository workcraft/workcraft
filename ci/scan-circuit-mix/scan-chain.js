work = load("scan-bufs_invs_boxes.circuit.work");

setConfigVar("CircuitSettings.tbufData", "TBUF (I, O)");
setConfigVar("CircuitSettings.tinvData", "TINV (I, ON)");
setConfigVar("CircuitSettings.testInstancePrefix", "test_");
setConfigVar("CircuitSettings.scaninData", "scanin / SI");
setConfigVar("CircuitSettings.scanoutData", "scanout / SO");
setConfigVar("CircuitSettings.scanenData", "scanen / SE");
setConfigVar("CircuitSettings.scanckData", "scanck / CK");
setConfigVar("CircuitSettings.scantmData", "");
setConfigVar("CircuitSettings.useIndividualScan", "false");
setConfigVar("CircuitSettings.useScanInitialisation", "false");

insertCircuitTestableGates(work);
insertCircuitScan(work);

write(
    "Cycle absence check: " + checkCircuitCycles(work) + "\n" +
    "Combined check: " + checkCircuitCombined(work) + "\n" +
    statCircuit(work), "scan-bufs_invs_boxes-chain.circuit.stat");

exit();
