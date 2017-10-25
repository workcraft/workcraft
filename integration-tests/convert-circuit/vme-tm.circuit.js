circuitWork = load('vme-tm.circuit.work');

deviceStgWork = convertCircuitToStg(circuitWork);
deviceStgStat = statStg(deviceStgWork);
write(deviceStgStat, 'vme-tm.device.stat');

systemStgWork = convertCircuitToStgWithEnvironment(circuitWork);
systemStgStat = statStg(systemStgWork);
write(systemStgStat, 'vme-tm.system.stat');

exit();
