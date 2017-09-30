we = load('vme.stg.work');
stat = executeCommand(we, 'StgStatisticsCommand');
write(stat, 'vme.stg.stat');
exit();
