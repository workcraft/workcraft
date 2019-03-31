work = load("cycle-tm.circuit.work");

modifyCircuitForceInitClearAll(work);

modifyCircuitForceInitTagNecessary(work);

insertCircuitResetActiveHigh(work);

write(
    "Initialisation check: " + checkCircuitReset(work) + "\n" +
    "Combined check: " + checkCircuitCombined(work) + "\n" +
    statCircuit(work), "cycle-tm.circuit.stat");

exit();
