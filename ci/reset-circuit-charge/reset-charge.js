work = load("charge-tm.circuit.work");

tagCircuitForcedInitClearAll(work);

tagCircuitForcedInitInputPorts(work);

tagCircuitForcedInitProblematicPins(work);

tagCircuitForcedInitAutoDiscard(work);

tagCircuitForcedInitAutoAppend(work);

insertCircuitResetActiveLow(work);

write(
    "Initialisation check: " + checkCircuitReset(work) + "\n" +
    "Combined check: " + checkCircuitCombined(work) + "\n" +
    statCircuit(work), "charge-tm.circuit.stat");

exit();
