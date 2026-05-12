var successCount = 0;
for each (arg in args) {
    if (process(arg)) {
        successCount++;
    }
}
write(successCount + " out of " + args.length + " benchmarks passed the test", "test.result");
exit();

function process(name) {
    let logFileName = name + ".result";
    let s = "";
    let stgWork = importStgG(name + ".g");

    s += "Processing STG:\n";
    s += "  - importing: ";
    if (stgWork == null) {
        s += "FAILED\n";
        write(s, logFileName);
        return false;
    }
    s += "OK\n";
    save(stgWork, name + ".stg.work");

    s += "  - verification: ";
    if (checkStgCombined(stgWork) != true) {
        s += "FAILED\n";
        write(s, logFileName);
        return false;
    }
    s += "OK\n";

    s += "  - CSC check: ";
    if (checkStgCsc(stgWork) == true) {
        cscStgWork = stgWork;
        s += "OK\n";
    } else {
        s += "CONFLICT\n";
        s += "  - Conflict resolution: ";
        cscStgWork = resolveCscConflictMpsat(stgWork);
        if (cscStgWork == null) {
            cscStgWork = resolveCscConflictPetrify(stgWork);
            if (cscStgWork == null) {
                s += "FAILED\n";
                write(s, logFileName);
                return false;
            }
        }
        s += "OK\n";
        save(cscStgWork, name + "-csc.stg.work");
    }


    s += "Complex gate:\n";
    s += "  - synthesis: ";
    let cgCircuitWork = synthComplexGateMpsat(cscStgWork);
    if (cgCircuitWork == null) {
        s += "FAILED\n";
        write(s, logFileName);
        return false;
    }
    s += "OK\n";

    s += "  - verification: ";
    if (checkCircuitCombined(cgCircuitWork) != true) {
        s += "FAILED\n";
        write(s, logFileName);
        return false;
    }
    s += "OK\n";


    s += "Generalised C-element:\n";
    s += "  - synthesis: ";
    let gcCircuitWork = synthGeneralisedCelementMpsat(cscStgWork);
    if (gcCircuitWork == null) {
        s += "FAILED\n";
        write(s, logFileName);
        return false;
    }
    s += "OK\n";

    s += "  - verification: ";
    if (checkCircuitCombined(gcCircuitWork) != true) {
        s += "FAILED\n";
        write(s, logFileName);
        return false;
    }
    s += "OK\n";


    s += "Standard C-element:\n";
    s += "  - synthesis: ";
    let stdcCircuitWork = synthStandardCelementMpsat(cscStgWork);
    if (stdcCircuitWork == null) {
        s += "FAILED\n";
        write(s, logFileName);
        return false;
    }
    s += "OK\n";

    s += "  - verification: ";
    if (checkCircuitCombined(stdcCircuitWork) != true) {
        s += "FAILED\n";
        write(s, logFileName);
        return false;
    }
    s += "OK\n";


    s += "Technology mapping:\n";
    s += "  - synthesis: ";
    let tmCircuitWork = synthTechnologyMappingMpsat(cscStgWork);
    if (tmCircuitWork == null) {
        s += "FAILED\n";
        write(s, logFileName);
        return false;
    }
    s += "OK\n";

    s += "  - verification: ";
    if (checkCircuitCombined(tmCircuitWork) != true) {
        s += "FAILED\n";
        write(s, logFileName);
        return false;
    }
    s += "OK\n";

    write(s, logFileName);
    return true;
}
