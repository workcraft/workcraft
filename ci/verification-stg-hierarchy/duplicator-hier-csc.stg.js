work = load("duplicator-hier-csc.stg.work");
write(
    "Combined check: " + checkStgCombined(work) + "\n" +
    "Consistency: " + checkStgConsistency(work) + "\n" +
    "Output determinacy: " + checkStgOutputDeterminacy(work) + "\n" +
    "Deadlock freeness: " + checkStgDeadlockFreeness(work) + "\n" +
    "Input properness: " + checkStgInputProperness(work) + "\n" +
    "Mutex protocol: " + checkStgMutexProtocol(work) + "\n" +
    "Output persistency: " + checkStgOutputPersistency(work) + "\n" +
    "Absence of local self-triggering: " + checkStgLocalSelfTriggering(work) + "\n" +
    "DI interface: " + checkStgDiInterface(work) + "\n" +
    "CSC: " + checkStgCsc(work) + "\n" +
    "USC: " + checkStgUsc(work) + "\n" +
    "Normalcy: " + checkStgNormalcy(work) + "\n" +
    "", "duplicator-hier-csc.stg.result");
exit();
