we = load('vme.stg.work');
csc = executeCommand(we, 'PetrifyCscConflictResolutionCommand');

cgCircuit = executeCommand(csc, 'PetrifyComplexGateSynthesisCommand');
cgStat = executeCommand(cgCircuit, 'CircuitStatisticsCommand');
write(cgStat, 'petrify-vme-cg.circuit.stat');

stdcCircuit = executeCommand(csc, 'PetrifyStandardCelementSynthesisCommand');
stdcStat = executeCommand(stdcCircuit, 'CircuitStatisticsCommand');
write(stdcStat, 'petrify-vme-stdc.circuit.stat');

gcCircuit = executeCommand(csc, 'PetrifyGeneralisedCelementSynthesisCommand');
gcStat = executeCommand(gcCircuit, 'CircuitStatisticsCommand');
write(gcStat, 'petrify-vme-gc.circuit.stat');

tmCircuit = executeCommand(csc, 'PetrifyTechnologyMappingSynthesisCommand');
tmStat = executeCommand(tmCircuit, 'CircuitStatisticsCommand');
write(tmStat, 'petrify-vme-tm.circuit.stat');

exit();
