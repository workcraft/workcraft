work = load("charge-tm.circuit.work");

tagCircuitPathBreakerClearAll(work);

tagCircuitPathBreakerAutoAppend(work);

setConfigVar("CircuitSettings.tbufData", "TBUF (I, O)");
setConfigVar("CircuitSettings.tinvData", "TINV (I, ON)");
setConfigVar("CircuitSettings.scanSuffix", "_scan");
setConfigVar("CircuitSettings.scaninData", "scanin / SI");
setConfigVar("CircuitSettings.scanoutData", "scanout / SO");
setConfigVar("CircuitSettings.scanenData", "scanen / SE");
setConfigVar("CircuitSettings.scanckData", "scanck / CK");
setConfigVar("CircuitSettings.scantmData", "");
setConfigVar("CircuitSettings.individualScan", "false");

insertCircuitTestableGates(work);
insertCircuitScan(work);

write(
    "Cycle absence check: " + checkCircuitCycles(work) + "\n" +
    "Combined check: " + checkCircuitCombined(work) + "\n" +
    statCircuit(work), "charge-tm-scan-chain.circuit.stat");

exit();
