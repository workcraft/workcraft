work = load("charge-tm.circuit.work");

modifyCircuitForceInitClearAll(work);

modifyCircuitForceInitTagInputPorts(work);

modifyCircuitForceInitTagSelfLoops(work);

modifyCircuitForceInitTagSequentialGates(work);

modifyCircuitForceInitClearRedundant(work);

insertCircuitResetActiveLow(work);

write(
    "Initialisation check: " + checkCircuitReset(work) + "\n" +
    "Combined check: " + checkCircuitCombined(work) + "\n" +
    statCircuit(work), "charge-tm.circuit.stat");

exit();
