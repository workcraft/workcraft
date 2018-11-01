work = load("vme-tm.circuit.work");
write(
    "Combined check: " + checkCircuitCombined(work) + "\n" +
    "Deadlock-freeness: " + checkCircuitDeadlockFreeness(work) + "\n" +
    "Conformation: " + checkCircuitConformation(work) + "\n" +
    "Output persistency: " + checkCircuitOutputPersistency(work) + "\n" +
    "Strict implementation: " + checkCircuitStrictImplementation(work) + "\n",
    "vme-tm.circuit.result");
exit();
