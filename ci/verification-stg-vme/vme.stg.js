work = load("vme.stg.work");
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
    "Redundancy of place '<d+,dtack+>': " + checkStgPlaceRedundancy(work, "<d+,dtack+>") + "\n" +
    "Redundancy of places 'p1 p2': " + checkStgPlaceRedundancy(work, "p1 p2") + "\n" +
    "Mutual exclusion of 'dsr' and 'dsw' (Signal assertion): " + checkStgSignalAssertion(work, "!dsr || !dsw") + "\n" +
    "Mutual exclusion of 'dsr' and 'dsw' (REACH assertion): " + checkStgReachAssertion(work, "$S\"dsr\" & $S\"dsw\"") + "\n" +
    "Mutual exclusion of 'dsr' and 'dsw' (SPOT assertion): " + checkStgSpotAssertion(work, "G((!\"dsr\") | (!\"dsw\"))") + "\n",
    "vme.stg.result");
exit();
