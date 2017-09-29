we = load('bad.stg.work');
write(
    'Combined check: ' + executeCommand(we, 'MpsatCombinedVerificationCommand') + '\n' +
    'Consistency: ' + executeCommand(we, 'MpsatConsistencyVerificationCommand') + '\n' +
    'Deadlock-freeness: ' + executeCommand(we, 'MpsatDeadlockVerificationCommand') + '\n' +
    'Input properness: ' + executeCommand(we, 'MpsatInputPropernessVerificationCommand') + '\n' +
    'Output persistency: ' + executeCommand(we, 'MpsatOutputPersistencyVerificationCommand') + '\n' +
    'CSC: ' + executeCommand(we, 'MpsatCscVerificationCommand') + '\n' +
    'USC: ' + executeCommand(we, 'MpsatUscVerificationCommand') + '\n' +
    'DI interface: ' + executeCommand(we, 'MpsatDiInterfaceVerificationCommand') + '\n' +
    'Normalcy: ' + executeCommand(we, 'MpsatNormalcyVerificationCommand') + '\n' +
    'Mutex implementability: ' + executeCommand(we, 'MpsatMutexImplementabilityVerificationCommand') + '\n',
    'bad.stg.result');
exit();
