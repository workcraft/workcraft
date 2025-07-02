package org.workcraft.plugins.circuit.tasks;

import org.workcraft.Framework;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.stg.CircuitToStgConverter;
import org.workcraft.plugins.circuit.utils.ArbitrationUtils;
import org.workcraft.plugins.circuit.utils.ScanUtils;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.tasks.*;
import org.workcraft.plugins.mpsat_verification.utils.CompositionUtils;
import org.workcraft.plugins.mpsat_verification.utils.ReachUtils;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.pcomp.utils.PcompUtils;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.types.Pair;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.SortUtils;
import org.workcraft.utils.TextUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class CheckTask implements Task<VerificationChainOutput> {

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
            LinkedList<Pair<String, String>> exceptionPairs = new LinkedList<>();
            exceptionPairs.addAll(ArbitrationUtils.getMutexGrantPersistencyExceptions(circuit.getMathModel()));
            exceptionPairs.addAll(ArbitrationUtils.getWaitPersistencyExceptions(circuit.getMathModel(), false));

            // Load device STG
            CircuitToStgConverter converter = new CircuitToStgConverter(circuit);
            Stg devStg = converter.getStg().getMathModel();
            // Expose mutex grants as outputs in the device STG (store the original signal type to apply in composition STG)
            Map<String, Signal.Type> originalMutexGrantTypes = new HashMap<>();
            exposeMutexGrants(devStg, exceptionPairs, originalMutexGrantTypes);

            // Load environment STG
            File envStgFile = null;
            Stg envStg = StgUtils.loadOrImportStg(envFile);
            if (envStg != null) {
                // Make sure that signal types of the environment STG match those of the device STG
                StgUtils.restoreInterfaceSignals(envStg,
                        devStg.getSignalReferences(Signal.Type.INPUT),
                        devStg.getSignalReferences(Signal.Type.OUTPUT));

                envStgFile = new File(directory, StgUtils.ENVIRONMENT_FILE_PREFIX + stgFileExtension);
                Result<? extends ExportOutput> envExportResult = StgUtils.exportStg(envStg, envStgFile, monitor);
                if (!envExportResult.isSuccess()) {
                    if (envExportResult.isCancel()) {
                        return Result.cancel();
                    }
                    return Result.failure(new VerificationChainOutput(
                            envExportResult, null, null, preparationParameters));
                }
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
                        devExportResult, null, null, preparationParameters));
            }
            monitor.progressUpdate(0.1);

            // Generating system .g for conformation check (only if needed)
            if ((envStg != null) && checkConformation) {
                // Export environment STG (convert internal signals to dummies and keep track of renaming)
                Map<String, String> envSubstitutions = StgUtils.convertInternalSignalsToDummies(envStg);
                File modEnvStgFile = new File(directory, StgUtils.ENVIRONMENT_FILE_PREFIX + StgUtils.MODIFIED_FILE_SUFFIX + stgFileExtension);
                Result<? extends ExportOutput> modEnvExportResult = StgUtils.exportStg(envStg, modEnvStgFile, monitor);
                if (!modEnvExportResult.isSuccess()) {
                    if (modEnvExportResult.isCancel()) {
                        return Result.cancel();
                    }
                    return Result.failure(new VerificationChainOutput(
                            modEnvExportResult, null, null, preparationParameters));
                }
                monitor.progressUpdate(0.2);

                // Generating .g for the whole system (circuit and environment) without internal signals
                Result<? extends PcompOutput> modPcompResult = PcompUtils.composeDevWithEnv(devStgFile, modEnvStgFile, directory, monitor,
                        StgUtils.SYSTEM_FILE_PREFIX + StgUtils.MODIFIED_FILE_SUFFIX + stgFileExtension,
                        PcompTask.DETAIL_FILE_PREFIX + StgUtils.MODIFIED_FILE_SUFFIX + PcompTask.DETAIL_FILE_EXTENSION);

                if (!modPcompResult.isSuccess()) {
                    if (modPcompResult.isCancel()) {
                        return Result.cancel();
                    }
                    return Result.failure(new VerificationChainOutput(
                            devExportResult, modPcompResult, null, preparationParameters));
                }
                monitor.progressUpdate(0.3);

                File modSysStgFile = modPcompResult.getPayload().getOutputFile();
                Stg modSysStg = StgUtils.loadOrImportStg(modSysStgFile);
                // Restore the original types of mutex grant in modified system STG (if needed)
                if (!originalMutexGrantTypes.isEmpty()) {
                    restoreMutexGrants(modSysStg, exceptionPairs, originalMutexGrantTypes);
                    modSysStgFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + StgUtils.MODIFIED_FILE_SUFFIX + StgUtils.MUTEX_FILE_SUFFIX + stgFileExtension);
                    StgUtils.exportStg(modSysStg, modSysStgFile, monitor);
                }

                CompositionData compositionData;
                try {
                    compositionData = new CompositionData(modPcompResult.getPayload().getDetailFile());
                } catch (FileNotFoundException e) {
                    return Result.exception(e);
                }

                // Apply substitutions to the composition data of the environment STG component
                CompositionUtils.applyComponentSubstitutions(compositionData, modEnvStgFile, envSubstitutions);

                // Insert shadow transitions into the composition STG for device outputs and internal signals
                CompositionTransformer transformer = new CompositionTransformer(modSysStg, compositionData);
                Collection<String> devOutputSignals = devStg.getSignalReferences(Signal.Type.OUTPUT);
                Collection<SignalTransition> shadowTransitions = transformer.insetShadowTransitions(devOutputSignals, devStgFile);
                // Insert a marked choice place shared by all shadow transitions (to prevent inconsistency)
                transformer.insertShadowEnablerPlace(shadowTransitions);

                // Fill verification parameters with the inserted shadow transitions
                Collection<String> shadowTransitionRefs = ReferenceHelper.getReferenceList(modSysStg, shadowTransitions);
                VerificationParameters conformationParameters = ReachUtils.getConformationParameters(shadowTransitionRefs);

                modSysStgFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + StgUtils.MODIFIED_FILE_SUFFIX + "-shadow" + stgFileExtension);
                StgUtils.exportStg(modSysStg, modSysStgFile, monitor);

                MpsatTask conformationMpsatTask = new MpsatTask(modSysStgFile, conformationParameters, directory);
                Result<? extends MpsatOutput>  conformationMpsatResult = manager.execute(
                        conformationMpsatTask, "Running conformation check [MPSat]", new SubtaskMonitor<>(monitor));

                Result<CompositionExportOutput> compositionExportResult = Result.success(new CompositionExportOutput(modSysStgFile, compositionData));

                if (!conformationMpsatResult.isSuccess()) {
                    if (conformationMpsatResult.isCancel()) {
                        return Result.cancel();
                    }
                    return Result.failure(new VerificationChainOutput(
                            compositionExportResult, modPcompResult, conformationMpsatResult, conformationParameters));
                }
                monitor.progressUpdate(0.5);

                if (conformationMpsatResult.getPayload().hasSolutions()) {
                    return Result.success(new VerificationChainOutput(
                            compositionExportResult, modPcompResult, conformationMpsatResult, conformationParameters,
                            "Circuit does not conform to the environment after the following trace(s):"));
                }
            }
            monitor.progressUpdate(0.6);

            // Generating system .g for deadlock freeness and output persistency checks (only if needed)
            Result<? extends PcompOutput>  pcompResult = null;
            if (checkDeadlock || checkPersistency) {
                File sysStgFile;
                if (envStg == null) {
                    sysStgFile = devStgFile;
                } else {
                    // Generating .g for the whole system (circuit and environment)
                    pcompResult = PcompUtils.composeDevWithEnv(devStgFile, envStgFile, directory, monitor);
                    if (!pcompResult.isSuccess()) {
                        if (pcompResult.isCancel()) {
                            return Result.cancel();
                        }
                        return Result.failure(new VerificationChainOutput(
                                devExportResult, pcompResult, null, preparationParameters));
                    }
                    sysStgFile = pcompResult.getPayload().getOutputFile();
                }
                // Restore the original types of mutex grant in system STG (if needed)
                if (!originalMutexGrantTypes.isEmpty()) {
                    Stg sysStg = StgUtils.loadOrImportStg(sysStgFile);
                    restoreMutexGrants(sysStg, exceptionPairs, originalMutexGrantTypes);
                    sysStgFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + StgUtils.MUTEX_FILE_SUFFIX + stgFileExtension);
                    StgUtils.exportStg(sysStg, sysStgFile, monitor);
                }
                monitor.progressUpdate(0.2);

                File unfoldingFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + MpsatUnfoldingTask.PNML_FILE_EXTENSION);
                MpsatUnfoldingTask unfoldingTask = new MpsatUnfoldingTask(sysStgFile, unfoldingFile, directory);
                SubtaskMonitor<Object> unfoldingMonitor = new SubtaskMonitor<>(monitor);
                Result<? extends MpsatOutput> unfoldingResult = manager.execute(unfoldingTask, "Unfolding .g", unfoldingMonitor);

                if (!unfoldingResult.isSuccess()) {
                    if (unfoldingResult.isCancel()) {
                        return Result.cancel();
                    }
                    return Result.failure(new VerificationChainOutput(
                            devExportResult, pcompResult, unfoldingResult, preparationParameters));
                }

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
                                devExportResult, pcompResult, deadlockMpsatResult, deadlockParameters));
                    }
                    monitor.progressUpdate(0.7);

                    if (deadlockMpsatResult.getPayload().hasSolutions()) {
                        return Result.success(new VerificationChainOutput(
                                devExportResult, pcompResult, deadlockMpsatResult, deadlockParameters,
                                "Circuit has a deadlock after the following trace(s):"));
                    }
                }
                monitor.progressUpdate(0.8);

                // Check for persistency (if requested)
                if (checkPersistency) {
                    Set<String> skipSignals = ScanUtils.getScanoutAndAuxiliarySignals(circuit.getMathModel());
                    List<String> orderedSkipSignals = SortUtils.getSortedNatural(skipSignals);

                    VerificationParameters persistencyParameters = ReachUtils.getOutputPersistencyParameters(
                            exceptionPairs, orderedSkipSignals);

                    MpsatTask persistencyMpsatTask = new MpsatTask(unfoldingFile, sysStgFile, persistencyParameters, directory);
                    SubtaskMonitor<Object> mpsatMonitor = new SubtaskMonitor<>(monitor);
                    Result<? extends MpsatOutput> persistencyMpsatResult = manager.execute(
                            persistencyMpsatTask, "Running output persistency check [MPSat]", mpsatMonitor);

                    if (!persistencyMpsatResult.isSuccess()) {
                        if (persistencyMpsatResult.isCancel()) {
                            return Result.cancel();
                        }
                        return Result.failure(new VerificationChainOutput(
                                devExportResult, pcompResult, persistencyMpsatResult, persistencyParameters));
                    }
                    monitor.progressUpdate(0.9);

                    if (persistencyMpsatResult.getPayload().hasSolutions()) {
                        return Result.success(new VerificationChainOutput(
                                devExportResult, pcompResult, persistencyMpsatResult, persistencyParameters,
                                "Circuit is not output-persistent after the following trace(s):"));
                    }
                }
                monitor.progressUpdate(1.0);
            }

            // Success
            Result<? extends MpsatOutput>  mpsatResult = Result.success();
            VerificationParameters completionParameters = ReachUtils.getToolchainCompletionParameters();
            String message = getSuccessMessage(envFile);
            return Result.success(new VerificationChainOutput(
                    devExportResult, pcompResult, mpsatResult, completionParameters, message));

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
            Signal.Type g1SignalType = grantTypes.get(g1SignalName);
            if (g1SignalType != null) {
                stg.setSignalType(g1SignalName, g1SignalType);
            }
            String g2SignalName = grantPair.getSecond();
            Signal.Type g2SignalType = grantTypes.get(g2SignalName);
            if (g2SignalType != null) {
                stg.setSignalType(g2SignalName, g2SignalType);
            }
        }
    }

    private void exposeMutexGrants(Stg stg, LinkedList<Pair<String, String>> grantPairs,
            Map<String, Signal.Type> grantTypes) {

        for (Pair<String, String> grantPair : grantPairs) {
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
        String message;
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
        return TextUtils.wrapText(message);
    }

    private String getPropertyMessage(String message, boolean multiline) {
        if (multiline) {
            return TextUtils.getBullet(message + '\n');
        }
        return message;
    }

}
