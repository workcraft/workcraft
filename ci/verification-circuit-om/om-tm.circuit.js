work = load("om-tm.circuit.work");
write(
    "Combined check: " + checkCircuitCombined(work) + "\n" +
    "Deadlock-freeness: " + checkCircuitDeadlockFreeness(work) + "\n" +
    "Conformation: " + checkCircuitConformation(work) + "\n" +
    "Output persistency: " + checkCircuitOutputPersistency(work) + "\n" +
    "Binate function implementation: " + checkCircuitBinateImplementation(work) + "\n" +
    "Strict implementation: " + checkCircuitStrictImplementation(work) + "\n" +
    "", "om-tm.circuit.result");
exit();
