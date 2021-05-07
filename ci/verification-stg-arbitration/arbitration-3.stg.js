work = load("arbitration-3.stg.work");

setConfigVar("StgSettings.mutexProtocol", "STRICT");
write(
    "Combined check: " + checkStgCombined(work) + "\n" +
    "Consistency: " + checkStgConsistency(work) + "\n" +
    "Deadlock freeness: " + checkStgDeadlockFreeness(work) + "\n" +
    "Input properness: " + checkStgInputProperness(work) + "\n" +
    "Output persistency: " + checkStgOutputPersistency(work) + "\n" +
    "Output determinacy: " + checkStgOutputDeterminacy(work) + "\n" +
    "CSC: " + checkStgCsc(work) + "\n" +
    "USC: " + checkStgUsc(work) + "\n" +
    "DI interface: " + checkStgDiInterface(work) + "\n" +
    "Normalcy: " + checkStgNormalcy(work) + "\n" +
    "Mutex implementability: " + checkStgMutexImplementability(work) + "\n" +
    "", "arbitration-3.stg.result");
exit();
