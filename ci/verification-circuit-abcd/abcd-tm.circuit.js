work = load("abcd-bad-tm.circuit.work");
write(
    "Combined check: " + checkCircuitCombined(work) + "\n" +
    "Deadlock-freeness: " + checkCircuitDeadlockFreeness(work) + "\n" +
    "Conformation: " + checkCircuitConformation(work) + "\n" +
    "Output persistency: " + checkCircuitOutputPersistency(work) + "\n" +
    "Binate function implementation: " + checkCircuitBinateImplementation(work) + "\n" +
    "Strict implementation: " + checkCircuitStrictImplementation(work) + "\n" +
    "", "abcd-tm.circuit.result");
exit();
