work = load("charge-tm.circuit.work");

testCircuitClearPathBreaker(work);

testCircuitInsertCycleBreakerBuffers(work);

testCircuitInsertPathBreakerScan(work);

write(
    "Cycle absence check: " + checkCircuitCycles(work) + "\n" +
    "Combined check: " + checkCircuitCombined(work) + "\n" +
    statCircuit(work), "charge-tm.circuit.stat");

exit();
