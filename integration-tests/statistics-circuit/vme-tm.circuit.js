we = load('vme-tm.circuit.work');
stat = executeCommand(we, 'CircuitStatisticsCommand');
write(stat, 'vme-tm.circuit.stat');
exit();
