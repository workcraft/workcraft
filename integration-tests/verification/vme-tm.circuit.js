we = load('vme-tm.circuit.work');
write(
    'Combined check: ' + executeCommand(we, 'CircuitVerificationCommand') + '\n' +
    'Deadlock-freeness: ' + executeCommand(we, 'CircuitDeadlockVerificationCommand') + '\n' +
    'Conformation: ' + executeCommand(we, 'CircuitConformationVerificationCommand') + '\n' +
    'Output persistency: ' + executeCommand(we, 'CircuitPersistencyVerificationCommand') + '\n',
    'vme-tm.circuit.result');
exit();
