work = load("vme-tm.circuit.work");
write(
    "Combined check: " + checkCircuitCombined(work) + "\n" +
    "Deadlock-freeness: " + checkCircuitDeadlockFreeness(work) + "\n" +
    "Conformation: " + checkCircuitConformation(work) + "\n" +
    "Output persistency: " + checkCircuitOutputPersistency(work) + "\n" +
    "Binate function implementation: " + checkCircuitBinateImplementation(work) + "\n" +
    "Strict implementation: " + checkCircuitStrictImplementation(work) + "\n" +
    "Mutual exclusion of 'dsr' and 'dsw' (Signal assertion): " + checkCircuitSignalAssertion(work, "!dsr || !dsw") + "\n" +
    "Mutual exclusion of 'dsr' and 'dsw' (REACH assertion): " + checkCircuitReachAssertion(work, "$S\"dsr\" & $S\"dsw\"") + "\n" +
//    "Mutual exclusion of 'dsr' and 'dsw' (SPOT assertion): " + checkCircuitSpotAssertion(work, "G((!\"dsr\") | (!\"dsw\"))") + "\n" +
    "", "vme-tm.circuit.result");
exit();
