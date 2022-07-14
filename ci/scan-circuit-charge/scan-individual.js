work = load("charge-tm.circuit.work");

tagCircuitPathBreakerClearAll(work);

tagCircuitPathBreakerAutoAppend(work);

setConfigVar("CircuitSettings.tbufData", "MUX2 (I0, O)");
setConfigVar("CircuitSettings.tinvData", "NMUX2 (I0, ON)");
setConfigVar("CircuitSettings.testInstancePrefix", "test_");
setConfigVar("CircuitSettings.scanSuffix", "");
setConfigVar("CircuitSettings.scaninData", "scanin / I1");
setConfigVar("CircuitSettings.scanoutData", "scanout / I0");
setConfigVar("CircuitSettings.scanenData", "scanen / S");
setConfigVar("CircuitSettings.scanckData", "");
setConfigVar("CircuitSettings.scantmData", "");
setConfigVar("CircuitSettings.useIndividualScan", "true");
setConfigVar("CircuitSettings.useScanInitialisation", "true");
setConfigVar("CircuitSettings.initialisationInverterInstancePrefix", "test_inv_");
insertCircuitTestableGates(work);

insertCircuitScan(work);

write(
    "Cycle absence check: " + checkCircuitCycles(work) + "\n" +
    "Combined check: " + checkCircuitCombined(work) + "\n" +
    statCircuit(work), "charge-tm-scan-individual.circuit.stat");

exit();
