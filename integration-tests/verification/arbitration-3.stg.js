stgWork = load('arbitration-3.stg.work');
write(
    'Combined check: ' + checkStgCombined(stgWork) + '\n' +
    'Consistency: ' + checkStgConsistency(stgWork) + '\n' +
    'Deadlock-freeness: ' + checkStgDeadlockFreeness(stgWork) + '\n' +
    'Input properness: ' + checkStgInputProperness(stgWork) + '\n' +
    'Output persistency: ' + checkStgOutputPersistency(stgWork) + '\n' +
    'CSC: ' + checkStgCsc(stgWork) + '\n' +
    'USC: ' + checkStgUsc(stgWork) + '\n' +
    'DI interface: ' + checkStgDiInterface(stgWork) + '\n' +
    'Normalcy: ' + checkStgNormalcy(stgWork) + '\n' +
    'Mutex implementability: ' + checkStgMutexImplementability(stgWork) + '\n',
    'arbitration-3.stg.result');
exit();
