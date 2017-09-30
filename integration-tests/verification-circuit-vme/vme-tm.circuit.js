circuitWork = load('vme-tm.circuit.work');
write(
    'Combined check: ' + checkCircuitCombined(circuitWork) + '\n' +
    'Deadlock-freeness: ' + checkCircuitDeadlockFreeness(circuitWork) + '\n' +
    'Conformation: ' + checkCircuitConformation(circuitWork) + '\n' +
    'Output persistency: ' + checkCircuitOutputPersistency(circuitWork) + '\n' +
    'Strict implementation: ' + checkCircuitStrictImplementation(circuitWork) + '\n',
    'vme-tm.circuit.result');
exit();
