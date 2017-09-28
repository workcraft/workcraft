we = load('vme.stg.work');
csc = executeCommand(we, 'MpsatCscConflictResolutionCommand');

cgCircuit = executeCommand(csc, 'MpsatComplexGateSynthesisCommand');
cgStat = executeCommand(cgCircuit, 'CircuitStatisticsCommand');
write(cgStat, 'mpsat-vme-cg.circuit.stat');

stdcCircuit = executeCommand(csc, 'MpsatStandardCelementSynthesisCommand');
stdcStat = executeCommand(cgCircuit, 'CircuitStatisticsCommand');
write(stdcStat, 'mpsat-vme-stdc.circuit.stat');

gcCircuit = executeCommand(csc, 'MpsatGeneralisedCelementSynthesisCommand');
gcStat = executeCommand(gcCircuit, 'CircuitStatisticsCommand');
write(gcStat, 'mpsat-vme-gc.circuit.stat');

tmCircuit = executeCommand(csc, 'MpsatTechnologyMappingSynthesisCommand');
tmStat = executeCommand(tmCircuit, 'CircuitStatisticsCommand');
write(tmStat, 'mpsat-vme-tm.circuit.stat');

exit();
