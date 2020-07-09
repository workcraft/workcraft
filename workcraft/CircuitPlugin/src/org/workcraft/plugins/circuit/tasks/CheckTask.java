package org.workcraft.plugins.circuit.tasks;

import org.workcraft.Framework;
import org.workcraft.gui.properties.PropertyHelper;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.stg.CircuitToStgConverter;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.plugins.mpsat_verification.presets.VerificationMode;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.tasks.MpsatOutput;
import org.workcraft.plugins.mpsat_verification.tasks.MpsatTask;
import org.workcraft.plugins.mpsat_verification.tasks.VerificationChainOutput;
import org.workcraft.plugins.mpsat_verification.utils.ReachUtils;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.pcomp.utils.PcompUtils;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.types.Pair;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class CheckTask implements Task<VerificationChainOutput> {

    private static final String DEV_PLACES_REPLACEMENT =
            "/* insert device place names here */"; // For example: "p0", "<a-,b+>"

    private static final String CIRCUIT_CONFORMATION_REACH =
            "// Check a device STG for conformation to its environment STG.\n" +
            "// LIMITATIONS (could be checked before parallel composition):\n" +
            "// - The set of device STG place names is non-empty (this limitation can be easily removed).\n" +
            "// - Each transition in the device STG must have some arcs, i.e. its preset or postset is non-empty.\n" +
            "// - The device STG must have no dummies.\n" +
            "let\n" +
            "     // PDEV_NAMES is the set of names of places in the composed STG which originated from the device STG.\n" +
            "     // This set may in fact contain places from the environment STG, e.g. when PCOMP removes duplicate\n" +
            "     // places from the composed STG, it substitutes them with equivalent places that remain.\n" +
            "     // LIMITATION: syntax error if any of these sets is empty.\n" +
            "    PDEV_NAMES = {" + DEV_PLACES_REPLACEMENT + "\"\"} \\ {\"\"},\n" +
            "    // PDEV is the set of places with the names in PDEV_NAMES.\n" +
            "    // XML-based PUNF / MPSAT are needed here to process dead places correctly.\n" +
            "    PDEV = gather nm in PDEV_NAMES { P nm },\n" +
            "    // PDEV_EXT includes PDEV and places with the names of the form p@num, where p is a place in PDEV.\n" +
            "    // Such places appeared during optimisation of the unfolding prefix due to splitting places\n" +
            "    // incident with multiple read arcs (-r option of punf).\n" +
            "    // Note that such a place must have the same preset and postset (ignoring context) as p.\n" +
            "    PDEV_EXT = PDEV + gather p in PP \".*@[0-9]+\" s.t.\n" +
            "    let name_p=name p, pre_p=pre p, post_p=post p, s_pre_p=pre_p \\ post_p, s_post_p=post_p \\ pre_p {\n" +
            "        exists q in PDEV {\n" +
            "            let name_q=name q, pre_q=pre q, post_q=post q {\n" +
            "                name_p[..len name_q] = name_q + \"@\" &\n" +
            "                pre_q \\ post_q=s_pre_p & post_q \\ pre_q=s_post_p\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    { p },\n" +
            "    // TDEV is the set of device transitions.\n" +
            "    // XML-based PUNF / MPSAT are needed here to process dead transitions correctly.\n" +
            "    // LIMITATION: each transition in the device must have some arcs, i.e. its preset or postset is non-empty.\n" +
            "    TDEV = tran sig (pre PDEV + post PDEV)\n" +
            "{\n" +
            "     // The device STG must have no dummies.\n" +
            "    card (sig TDEV * DUMMY) != 0 ? fail \"Conformation can currently be checked only for device STGs without dummies\" :\n" +
            "    exists t in TDEV s.t. is_output t {\n" +
            "         // Check if t is enabled in the device STG.\n" +
            "         // LIMITATION: The device STG must have no dummies (this limitation is checked above.)\n" +
            "        forall p in pre t s.t. p in PDEV_EXT { $p }\n" +
            "        &\n" +
            "         // Check if t is enabled in the composed STG (and thus in the environment STG).\n" +
            "        ~@ sig t\n" +
            "    }\n" +
            "}\n";

    // REACH expression for checking if these two pairs of signals can be implemented by a mutex

    private final WorkspaceEntry we;
    private final boolean checkConformation;
    private final boolean checkDeadlock;
    private final boolean checkPersistency;

    public CheckTask(WorkspaceEntry we, boolean checkConformation, boolean checkDeadlock, boolean checkPersistency) {
        this.we = we;
        this.checkConformation = checkConformation;
        this.checkDeadlock = checkDeadlock;
        this.checkPersistency = checkPersistency;
    }

    @Override
    public Result<? extends VerificationChainOutput> run(ProgressMonitor<? super VerificationChainOutput> monitor) {
        Framework framework = Framework.getInstance();
        TaskManager manager = framework.getTaskManager();
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        String stgFileExtension = StgFormat.getInstance().getExtension();
        VerificationParameters preparationParameters = ReachUtils.getToolchainPreparationParameters();
        try {
            // Common variables
            VisualCircuit circuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
            File envFile = circuit.getMathModel().getEnvironmentFile();
            LinkedList<Pair<String, String>> grantPairs = getMutexGrantPairs(we);

            // Load device STG
            CircuitToStgConverter converter = new CircuitToStgConverter(circuit);
            Stg devStg = converter.getStg().getMathModel();
            // Expose mutex grants as outputs in the device STG (store the original signal type to apply in composition STG)
            Map<String, Signal.Type> originalMutexGrantTypes = new HashMap<>();
            exposeMutexGrants(devStg, grantPairs, originalMutexGrantTypes);

            // Load environment STG
            Stg envStg = StgUtils.loadStg(envFile);
            if (envStg != null) {
                // Make sure that signal types of the environment STG match those of the device STG
                StgUtils.restoreInterfaceSignals(envStg,
                        devStg.getSignalReferences(Signal.Type.INPUT),
                        devStg.getSignalReferences(Signal.Type.OUTPUT));
            }

            // Write device STG into a .g file
            String devStgName = (envStg == null ? StgUtils.SYSTEM_FILE_PREFIX : StgUtils.DEVICE_FILE_PREFIX) + stgFileExtension;
            File devStgFile = new File(directory, devStgName);
            Result<? extends ExportOutput> devExportResult = StgUtils.exportStg(devStg, devStgFile, monitor);
            if (!devExportResult.isSuccess()) {
                if (devExportResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.failure(new VerificationChainOutput(
                        devExportResult, null, null, null, preparationParameters));
            }
            monitor.progressUpdate(0.1);

            // Generating system .g for deadlock freeness and output persistency checks (only if needed)
            Result<? extends PcompOutput>  pcompResult = null;
            File sysStgFile = null;
            File detailFile = null;
            if (checkDeadlock || checkPersistency) {
                if (envStg == null) {
                    sysStgFile = devStgFile;
                } else {
                    File envStgFile = new File(directory, StgUtils.ENVIRONMENT_FILE_PREFIX + stgFileExtension);
                    Result<? extends ExportOutput> envExportResult = StgUtils.exportStg(envStg, envStgFile, monitor);
                    if (!envExportResult.isSuccess()) {
                        if (envExportResult.isCancel()) {
                            return Result.cancel();
                        }
                        return Result.failure(new VerificationChainOutput(
                                envExportResult, null, null, null, preparationParameters));
                    }

                    // Generating .g for the whole system (circuit and environment)
                    pcompResult = PcompUtils.composeDevWithEnv(devStgFile, envStgFile, directory, monitor);
                    if (!pcompResult.isSuccess()) {
                        if (pcompResult.isCancel()) {
                            return Result.cancel();
                        }
                        return Result.failure(new VerificationChainOutput(
                                devExportResult, pcompResult, null, null, preparationParameters));
                    }
                    sysStgFile = pcompResult.getPayload().getOutputFile();
                    detailFile = pcompResult.getPayload().getDetailFile();
                }
                // Restore the original types of mutex grant in system STG (if needed)
                if (!originalMutexGrantTypes.isEmpty()) {
                    Stg sysStg = StgUtils.loadStg(sysStgFile);
                    restoreMutexGrants(sysStg, grantPairs, originalMutexGrantTypes);
                    sysStgFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + StgUtils.MUTEX_FILE_SUFFIX + stgFileExtension);
                    StgUtils.exportStg(sysStg, sysStgFile, monitor);
                }
            }
            monitor.progressUpdate(0.2);

            // Generating system .g for conformation check (only if needed)
            Result<? extends PcompOutput>  modPcompResult = null;
            File modSysStgFile = null;
            File modDetailFile = null;
            if ((envStg != null) && checkConformation) {
                if ((sysStgFile != null) && envStg.getSignalReferences(Signal.Type.INTERNAL).isEmpty()) {
                    modPcompResult = pcompResult;
                    modSysStgFile = sysStgFile;
                    modDetailFile = detailFile;
                } else {
                    // Convert internal signals of the environment STG to dummies
                    StgUtils.convertInternalSignalsToDummies(envStg);
                    File modEnvStgFile = new File(directory, StgUtils.ENVIRONMENT_FILE_PREFIX + StgUtils.MODIFIED_FILE_SUFFIX + stgFileExtension);
                    Result<? extends ExportOutput> modEnvExportResult = StgUtils.exportStg(envStg, modEnvStgFile, monitor);
                    if (!modEnvExportResult.isSuccess()) {
                        if (modEnvExportResult.isCancel()) {
                            return Result.cancel();
                        }
                        return Result.failure(new VerificationChainOutput(
                                modEnvExportResult, null, null, null, preparationParameters));
                    }

                    // Generating .g for the whole system (circuit and environment) without internal signals
                    modPcompResult = PcompUtils.composeDevWithEnv(devStgFile, modEnvStgFile, directory, monitor,
                            StgUtils.SYSTEM_FILE_PREFIX + StgUtils.MODIFIED_FILE_SUFFIX + stgFileExtension,
                            PcompTask.DETAIL_FILE_PREFIX + StgUtils.MODIFIED_FILE_SUFFIX + PcompTask.DETAIL_FILE_EXTENSION);

                    if (!modPcompResult.isSuccess()) {
                        if (modPcompResult.isCancel()) {
                            return Result.cancel();
                        }
                        return Result.failure(new VerificationChainOutput(
                                devExportResult, modPcompResult, null, null, preparationParameters));
                    }
                    modSysStgFile = modPcompResult.getPayload().getOutputFile();
                    modDetailFile = modPcompResult.getPayload().getDetailFile();

                    // Restore the original types of mutex grant in modified system STG (if needed)
                    if (!originalMutexGrantTypes.isEmpty()) {
                        Stg sysModStg = StgUtils.loadStg(modSysStgFile);
                        restoreMutexGrants(sysModStg, grantPairs, originalMutexGrantTypes);
                        modSysStgFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + StgUtils.MODIFIED_FILE_SUFFIX + StgUtils.MUTEX_FILE_SUFFIX + stgFileExtension);
                        StgUtils.exportStg(sysModStg, modSysStgFile, monitor);
                    }
                }
            }
            monitor.progressUpdate(0.3);

            // Generate unfolding for deadlock freeness and output persistency checks (only if needed)
            File unfoldingFile = null;
            Result<? extends PunfOutput> punfResult = null;
            if (checkDeadlock || checkPersistency) {
                unfoldingFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + PunfTask.PNML_FILE_EXTENSION);
                PunfTask punfTask = new PunfTask(sysStgFile, unfoldingFile, directory);
                SubtaskMonitor<Object> punfMonitor = new SubtaskMonitor<>(monitor);
                punfResult = manager.execute(punfTask, "Unfolding .g", punfMonitor);

                if (!punfResult.isSuccess()) {
                    if (punfResult.isCancel()) {
                        return Result.cancel();
                    }
                    return Result.failure(new VerificationChainOutput(
                            devExportResult, pcompResult, punfResult, null, preparationParameters));
                }
            }

            // Generate unfolding for conformation checks (if needed)
            File modUnfoldingFile = unfoldingFile;
            Result<? extends PunfOutput> modPunfResult = punfResult;
            if ((envStg != null) && checkConformation) {
                if ((sysStgFile != modSysStgFile) || (modUnfoldingFile == null)) {
                    modUnfoldingFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + StgUtils.MODIFIED_FILE_SUFFIX + PunfTask.PNML_FILE_EXTENSION);
                    PunfTask punfModTask = new PunfTask(modSysStgFile, modUnfoldingFile, directory);
                    SubtaskMonitor<Object> punfModMonitor = new SubtaskMonitor<>(monitor);
                    modPunfResult = manager.execute(punfModTask, "Unfolding .g", punfModMonitor);

                    if (!modPunfResult.isSuccess()) {
                        if (modPunfResult.isCancel()) {
                            return Result.cancel();
                        }
                        return Result.failure(new VerificationChainOutput(
                                devExportResult, modPcompResult, modPunfResult, null, preparationParameters));
                    }
                }
            }
            monitor.progressUpdate(0.4);

            // Check for conformation (only if requested and if the environment is specified)
            if ((envStg != null) && checkConformation) {
                CompositionData compositionData = new CompositionData(modDetailFile);
                ComponentData devComponentData = compositionData.getComponentData(devStgFile);
                Set<String> devPlaceNames = devComponentData.getDstPlaces();
                VerificationParameters conformationParameters = getCircuitConformationParameters(devPlaceNames);
                MpsatTask conformationMpsatTask = new MpsatTask(modUnfoldingFile, modSysStgFile, conformationParameters, directory);
                SubtaskMonitor<Object> mpsatMonitor = new SubtaskMonitor<>(monitor);
                Result<? extends MpsatOutput>  conformationMpsatResult = manager.execute(
                        conformationMpsatTask, "Running conformation check [MPSat]", mpsatMonitor);

                if (!conformationMpsatResult.isSuccess()) {
                    if (conformationMpsatResult.isCancel()) {
                        return Result.cancel();
                    }
                    return Result.failure(new VerificationChainOutput(
                            devExportResult, modPcompResult, modPunfResult, conformationMpsatResult, conformationParameters));
                }
                monitor.progressUpdate(0.5);

                if (conformationMpsatResult.getPayload().hasSolutions()) {
                    return Result.success(new VerificationChainOutput(
                            devExportResult, modPcompResult, modPunfResult, conformationMpsatResult, conformationParameters,
                            "Circuit does not conform to the environment after the following trace(s):"));
                }
            }
            monitor.progressUpdate(0.6);

            // Check for deadlock (if requested)
            if (checkDeadlock) {
                VerificationParameters deadlockParameters = ReachUtils.getDeadlockParameters();
                MpsatTask deadlockMpsatTask = new MpsatTask(unfoldingFile, sysStgFile, deadlockParameters, directory);
                SubtaskMonitor<Object> mpsatMonitor = new SubtaskMonitor<>(monitor);
                Result<? extends MpsatOutput> deadlockMpsatResult = manager.execute(
                        deadlockMpsatTask, "Running deadlock check [MPSat]", mpsatMonitor);

                if (!deadlockMpsatResult.isSuccess()) {
                    if (deadlockMpsatResult.isCancel()) {
                        return Result.cancel();
                    }
                    return Result.failure(new VerificationChainOutput(
                            devExportResult, pcompResult, punfResult, deadlockMpsatResult, deadlockParameters));
                }
                monitor.progressUpdate(0.7);

                if (deadlockMpsatResult.getPayload().hasSolutions()) {
                    return Result.success(new VerificationChainOutput(
                            devExportResult, pcompResult, punfResult, deadlockMpsatResult, deadlockParameters,
                            "Circuit has a deadlock after the following trace(s):"));
                }
            }
            monitor.progressUpdate(0.8);

            // Check for persistency (if requested)
            if (checkPersistency) {
                VerificationParameters persistencyParameters = ReachUtils.getOutputPersistencyParameters(grantPairs);
                MpsatTask persistencyMpsatTask = new MpsatTask(unfoldingFile, sysStgFile, persistencyParameters, directory);
                SubtaskMonitor<Object> mpsatMonitor = new SubtaskMonitor<>(monitor);
                Result<? extends MpsatOutput>  persistencyMpsatResult = manager.execute(
                        persistencyMpsatTask, "Running output persistency check [MPSat]", mpsatMonitor);

                if (!persistencyMpsatResult.isSuccess()) {
                    if (persistencyMpsatResult.isCancel()) {
                        return Result.cancel();
                    }
                    return Result.failure(new VerificationChainOutput(
                            devExportResult, pcompResult, punfResult, persistencyMpsatResult, persistencyParameters));
                }
                monitor.progressUpdate(0.9);

                if (persistencyMpsatResult.getPayload().hasSolutions()) {
                    return Result.success(new VerificationChainOutput(
                            devExportResult, pcompResult, punfResult, persistencyMpsatResult, persistencyParameters,
                            "Circuit is not output-persistent after the following trace(s):"));
                }
            }
            monitor.progressUpdate(1.0);

            // Success
            Result<? extends MpsatOutput>  mpsatResult = Result.success();
            VerificationParameters completionParameters = ReachUtils.getToolchainCompletionParameters();
            String message = getSuccessMessage(envFile);
            return Result.success(new VerificationChainOutput(
                    devExportResult, pcompResult, punfResult, mpsatResult, completionParameters, message));

        } catch (Throwable e) {
            return new Result<>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

    private void restoreMutexGrants(Stg stg, LinkedList<Pair<String, String>> grantPairs,
            Map<String, Signal.Type> grantTypes) {

        for (Pair<String, String> grantPair : grantPairs) {
            String g1SignalName = grantPair.getFirst();
            stg.setSignalType(g1SignalName, grantTypes.get(g1SignalName));
            String g2SignalName = grantPair.getSecond();
            stg.setSignalType(g2SignalName, grantTypes.get(g2SignalName));
        }
    }

    private void exposeMutexGrants(Stg stg, LinkedList<Pair<String, String>> grantPairs,
            Map<String, Signal.Type> grantTypes) {

        for (Pair<String, String> grantPair: grantPairs) {
            String g1SignalName = grantPair.getFirst();
            Signal.Type g1SignalType = stg.getSignalType(g1SignalName);
            if (g1SignalType != Signal.Type.OUTPUT) {
                grantTypes.put(g1SignalName, g1SignalType);
                stg.setSignalType(g1SignalName, Signal.Type.OUTPUT);
            }
            String g2SignalName = grantPair.getSecond();
            Signal.Type g2SignalType = stg.getSignalType(g2SignalName);
            if (g2SignalType != Signal.Type.OUTPUT) {
                grantTypes.put(g2SignalName, g2SignalType);
                stg.setSignalType(g2SignalName, Signal.Type.OUTPUT);
            }
        }
    }

    private LinkedList<Pair<String, String>> getMutexGrantPairs(WorkspaceEntry we) {
        LinkedList<Pair<String, String>> grantPairs = new LinkedList<>();
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        Mutex mutex = CircuitSettings.parseMutexData();
        if ((mutex != null) && (mutex.name != null)) {
            for (FunctionComponent component: circuit.getFunctionComponents()) {
                if (mutex.name.equals(component.getModule())) {
                    Collection<Contact> outputs = component.getOutputs();
                    if (outputs.size() == 2) {
                        Iterator<Contact> iterator = outputs.iterator();
                        Contact contact1 = iterator.next();
                        Contact signal1 = CircuitUtils.findSignal(circuit, contact1, true);
                        String name1 = circuit.getNodeReference(signal1);
                        Contact contact2 = iterator.next();
                        Contact signal2 = CircuitUtils.findSignal(circuit, contact2, true);
                        String name2 = circuit.getNodeReference(signal2);
                        Pair<String, String> grantPair = Pair.of(name1, name2);
                        grantPairs.add(grantPair);
                    }
                }
            }
        }
        return grantPairs;
    }

    private String getSuccessMessage(File environmentFile) {
        int checkCount = 0;
        if (checkConformation) {
            checkCount++;
        }
        if (checkDeadlock) {
            checkCount++;
        }
        if (checkPersistency) {
            checkCount++;
        }
        String message = "";
        boolean hasEnvironment = (environmentFile != null) && environmentFile.exists();
        if (hasEnvironment) {
            message = "Under the given environment (" + environmentFile.getName() + ")";
        } else {
            message = "Without environment restrictions";
        }
        message += " the circuit is";
        message += checkCount > 1 ? ":\n" : " ";
        if (checkConformation) {
            message += getPropertyMessage("conformant", checkCount > 1);
        }
        if (checkDeadlock) {
            message += getPropertyMessage("deadlock-free", checkCount > 1);
        }
        if (checkPersistency) {
            message += getPropertyMessage("output-persistent", checkCount > 1);
        }
        return message;
    }

    private String getPropertyMessage(String message, boolean multiline) {
        if (multiline) {
            return PropertyHelper.BULLET_PREFIX + message + "\n";
        }
        return message;
    }

    public static VerificationParameters getCircuitConformationParameters(Collection<String> devPlaceRefs) {
        String str = devPlaceRefs.stream().map(ref -> "\"" + ref + "\", ").collect(Collectors.joining());
        String reach = CIRCUIT_CONFORMATION_REACH.replace(DEV_PLACES_REPLACEMENT, str);
        return new VerificationParameters("Conformation",
                VerificationMode.STG_REACHABILITY_CONFORMATION, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                reach, true);
    }

}
