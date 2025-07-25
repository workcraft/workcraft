for each (arg in args) {
    process(arg)
}
exit();

function process(name) {
    result = name + ".result";
    s = "";
    work = load(name + ".work");

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
        "", name + ".result");
}
