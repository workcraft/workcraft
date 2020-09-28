work = load("flat_arbiter.circuit.work");
write(
    "Combined check: " + checkCircuitCombined(work) + "\n" +
    "Deadlock-freeness: " + checkCircuitDeadlockFreeness(work) + "\n" +
    "Conformation: " + checkCircuitConformation(work) + "\n" +
    "Output persistency: " + checkCircuitOutputPersistency(work) + "\n" +
    "Binate function implementation: " + checkCircuitBinateImplementation(work) + "\n" +
    "Strict implementation: " + checkCircuitStrictImplementation(work) + "\n" +
    "Refinement: " + checkCircuitRefinement(work) + "\n" +
    "", "flat_arbiter.circuit.result");

work = load("flat_arbiter-deadlock.circuit.work");
write(
    "Combined check: " + checkCircuitCombined(work) + "\n" +
    "Deadlock-freeness: " + checkCircuitDeadlockFreeness(work) + "\n" +
    "Conformation: " + checkCircuitConformation(work) + "\n" +
    "Output persistency: " + checkCircuitOutputPersistency(work) + "\n" +
    "Binate function implementation: " + checkCircuitBinateImplementation(work) + "\n" +
    "Strict implementation: " + checkCircuitStrictImplementation(work) + "\n" +
    "Refinement: " + checkCircuitRefinement(work) + "\n" +
    "", "flat_arbiter-deadlock.circuit.result");
exit();
