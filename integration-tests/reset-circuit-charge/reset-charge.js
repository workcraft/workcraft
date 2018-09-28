work = load('charge-tm.circuit.work');

resetCircuitTagForceInitInputPorts(work);

resetCircuitTagForceInitSelfLoops(work);

resetCircuitTagForceInitSequentialGates(work);

resetCircuitClearRedundantForceInitPins(work);

resetCircuitInsertActiveLow(work);

write(
    'Initialisation check: ' + checkCircuitReset(work) + '\n' +
    'Combined check: ' + checkCircuitCombined(work) + '\n' +
    statCircuit(work), 'charge-tm.circuit.stat');

exit();
