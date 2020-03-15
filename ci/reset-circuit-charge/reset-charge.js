work = load("charge-tm.circuit.work");

tagCircuitForceInitClearAll(work);

tagCircuitForceInitInputPorts(work);

tagCircuitForceInitProblematicPins(work);

tagCircuitForceInitAutoDiscard(work);

tagCircuitForceInitAutoAppend(work);

insertCircuitResetActiveLow(work);

write(
    "Initialisation check: " + checkCircuitReset(work) + "\n" +
    "Combined check: " + checkCircuitCombined(work) + "\n" +
    statCircuit(work), "charge-tm.circuit.stat");

exit();
