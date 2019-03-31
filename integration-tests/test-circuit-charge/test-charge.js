work = load("charge-tm.circuit.work");

modifyCircuitPathBreakerClearAll(work);

modifyCircuitPathBreakerTagNecessary(work);

insertCircuitScan(work);

write(
    "Cycle absence check: " + checkCircuitCycles(work) + "\n" +
    "Combined check: " + checkCircuitCombined(work) + "\n" +
    statCircuit(work), "charge-tm.circuit.stat");

exit();
