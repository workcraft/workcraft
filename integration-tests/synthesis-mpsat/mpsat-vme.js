stgWork = load('vme.stg.work');

if (checkStgCsc(stgWork) == true) {
    cscStgWork = stgWork;
} else {
    cscStgWork = resolveCscConflictPetrify(stgWork);
    save(cscStgWork, 'vme-csc.stg.work');
}

cgCircuitWork = synthComplexGateMpsat(cscStgWork);
cgStat = statCircuit(cgCircuitWork);
write(cgStat, 'mpsat-vme-cg.circuit.stat');

gcCircuitWork = synthGeneralisedCelementMpsat(cscStgWork);
gcStat = statCircuit(gcCircuitWork);
write(gcStat, 'mpsat-vme-gc.circuit.stat');

stdcCircuitWork = synthGeneralisedCelementMpsat(cscStgWork);
stdcStat = statCircuit(cgCircuitWork);
write(stdcStat, 'mpsat-vme-stdc.circuit.stat');

setConfigVar("CircuitSettings.gateLibrary", "libraries/workcraft.lib");
setConfigVar("CircuitSettings.substitutionLibrary", "");
tmCircuitWork = synthTechnologyMappingMpsat(cscStgWork);
tmStat = statCircuit(tmCircuitWork);
write(tmStat, 'mpsat-vme-tm.circuit.stat');

exit();
