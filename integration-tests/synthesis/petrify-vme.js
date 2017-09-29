stgWork = load('vme.stg.work');
cscStgWork = resolveCscConflictPetrify(stgWork);

cgCircuitWork = synthComplexGatePetrify(cscStgWork);
cgStat = statCircuit(cgCircuitWork);
write(cgStat, 'petrify-vme-cg.circuit.stat');

gcCircuitWork = synthGeneralisedCelementPetrify(cscStgWork);
gcStat = statCircuit(gcCircuitWork);
write(gcStat, 'petrify-vme-gc.circuit.stat');

stdcCircuitWork = synthStandardCelementPetrify(cscStgWork);
stdcStat = statCircuit(stdcCircuitWork);
write(stdcStat, 'petrify-vme-stdc.circuit.stat');

tmCircuitWork = synthTechnologyMappingPetrify(cscStgWork);
tmStat = statCircuit(tmCircuitWork);
write(tmStat, 'petrify-vme-tm.circuit.stat');

exit();
