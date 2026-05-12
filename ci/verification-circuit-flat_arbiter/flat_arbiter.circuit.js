let work = load("flat_arbiter.circuit.work");
write(
    "Combined check: " + checkCircuitCombined(work) + "\n" +
    "Deadlock-freeness: " + checkCircuitDeadlockFreeness(work) + "\n" +
    "Conformation: " + checkCircuitConformation(work) + "\n" +
    "Output persistency: " + checkCircuitOutputPersistency(work) + "\n" +
    "Binate function implementation: " + checkCircuitBinateImplementation(work) + "\n" +
    "Strict implementation: " + checkCircuitStrictImplementation(work) + "\n" +
    "Refinement: " + checkCircuitRefinement(work) + "\n" +
    "", "flat_arbiter.circuit.result");

let deadlockWork = load("flat_arbiter-deadlock.circuit.work");
write(
    "Combined check: " + checkCircuitCombined(deadlockWork) + "\n" +
    "Deadlock-freeness: " + checkCircuitDeadlockFreeness(deadlockWork) + "\n" +
    "Conformation: " + checkCircuitConformation(deadlockWork) + "\n" +
    "Output persistency: " + checkCircuitOutputPersistency(deadlockWork) + "\n" +
    "Binate function implementation: " + checkCircuitBinateImplementation(deadlockWork) + "\n" +
    "Strict implementation: " + checkCircuitStrictImplementation(deadlockWork) + "\n" +
    "Refinement: " + checkCircuitRefinement(deadlockWork) + "\n" +
    "", "flat_arbiter-deadlock.circuit.result");

exit();
