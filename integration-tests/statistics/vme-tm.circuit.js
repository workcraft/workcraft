setConfigVar('CircuitSettings.gateLibrary', 'dist-template/linux/libraries/workcraft.lib');
we = load('vme-tm.circuit.work');
stat = executeCommand(we, 'CircuitStatisticsCommand');
write(stat, 'vme-tm.circuit.stat');
exit();
