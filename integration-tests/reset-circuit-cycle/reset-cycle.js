work = load('cycle-tm.circuit.work');

resetCircuitCompleteForceInitPins(work);

resetCircuitInsertActiveHigh(work);

write(
    'Initialisation check: ' + checkCircuitReset(work) + '\n' +
    'Combined check: ' + checkCircuitCombined(work) + '\n' +
    statCircuit(work), 'cycle-tm.circuit.stat');

exit();
