work = load("charge-tm.circuit.work");

resetCircuitClearForceInit(work);

resetCircuitForceInitInputPorts(work);

resetCircuitForceInitSelfLoops(work);

resetCircuitForceInitSequentialGates(work);

resetCircuitProcessRedundantForceInitPins(work);

resetCircuitInsertActiveLow(work);

write(
    "Initialisation check: " + checkCircuitReset(work) + "\n" +
    "Combined check: " + checkCircuitCombined(work) + "\n" +
    statCircuit(work), "charge-tm.circuit.stat");

exit();
