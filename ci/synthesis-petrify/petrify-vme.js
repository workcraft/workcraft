stgWork = load("vme.stg.work");

cgCircuitWork = synthComplexGatePetrify(stgWork);
cgStat = statCircuit(cgCircuitWork);
write(cgStat, "petrify-vme-cg.circuit.stat");

gcCircuitWork = synthGeneralisedCelementPetrify(stgWork);
gcStat = statCircuit(gcCircuitWork);
write(gcStat, "petrify-vme-gc.circuit.stat");

stdcCircuitWork = synthStandardCelementPetrify(stgWork);
stdcStat = statCircuit(stdcCircuitWork);
// Note: Petrify produces significantly different stdC implementation
// in Github Actions Linux runner -- skip for now.
//write(stdcStat, "petrify-vme-stdc.circuit.stat");

tmCircuitWork = synthTechnologyMappingPetrify(stgWork);
tmStat = statCircuit(tmCircuitWork);
write(tmStat, "petrify-vme-tm.circuit.stat");

exit();
