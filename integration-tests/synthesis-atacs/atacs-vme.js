stgWork = load('vme.stg.work');

if (checkStgCsc(stgWork) == true) {
    cscStgWork = stgWork;
} else {
    cscStgWork = resolveCscConflictPetrify(stgWork);
    save(cscStgWork, 'vme-csc.stg.work');
}

cgCircuitWork = synthComplexGateAtacs(cscStgWork);
cgStat = statCircuit(cgCircuitWork);
write(cgStat, 'atacs-vme-cg.circuit.stat');

gcCircuitWork = synthGeneralisedCelementAtacs(cscStgWork);
gcStat = statCircuit(gcCircuitWork);
write(gcStat, 'atacs-vme-gc.circuit.stat');

stdcCircuitWork = synthStandardCelementAtacs(cscStgWork);
stdcStat = statCircuit(stdcCircuitWork);
write(stdcStat, 'atacs-vme-stdc.circuit.stat');

exit();
