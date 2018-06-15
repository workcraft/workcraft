package org.workcraft.plugins.circuit.tasks;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.workcraft.Framework;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.CircuitUtils;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.stg.CircuitStgUtils;
import org.workcraft.plugins.circuit.stg.CircuitToStgConverter;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.tasks.MpsatChainOutput;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.plugins.mpsat.tasks.MpsatOutput;
import org.workcraft.plugins.mpsat.tasks.MpsatOutputParser;
import org.workcraft.plugins.mpsat.tasks.MpsatTask;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.shared.tasks.ExportOutput;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.FileUtils;
import org.workcraft.util.Pair;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class CheckCircuitTask extends MpsatChainTask {
    private final MpsatParameters toolchainPreparationSettings = MpsatParameters.getToolchainPreparationSettings();
    private final MpsatParameters toolchainCompletionSettings = MpsatParameters.getToolchainCompletionSettings();

    private final boolean checkConformation;
    private final boolean checkDeadlock;
    private final boolean checkPersistency;

    public CheckCircuitTask(WorkspaceEntry we, boolean checkConformation, boolean checkDeadlock, boolean checkPersistency) {
        super(we, null);
        this.checkConformation = checkConformation;
        this.checkDeadlock = checkDeadlock;
        this.checkPersistency = checkPersistency;
    }

    @Override
    public Result<? extends MpsatChainOutput> run(ProgressMonitor<? super MpsatChainOutput> monitor) {
        Framework framework = Framework.getInstance();
        TaskManager manager = framework.getTaskManager();
        WorkspaceEntry we = getWorkspaceEntry();
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        String stgFileExtension = StgFormat.getInstance().getExtension();
        try {
            // Common variables
            VisualCircuit visualCircuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
            File envFile = visualCircuit.getEnvironmentFile();
            LinkedList<Pair<String, String>> grantPairs = getMutexGrantPairs(we);

            // Load device STG
            CircuitToStgConverter converter = new CircuitToStgConverter(visualCircuit);
            Stg devStg = (Stg) converter.getStg().getMathModel();
            // Convert mutex grants into inputs in device STG, but store the original signal type
            HashMap<String, Signal.Type> signalOriginalType = new HashMap<>();
            for (Pair<String, String> grantPair: grantPairs) {
                String g1SignalName = grantPair.getFirst();
                signalOriginalType.put(g1SignalName, devStg.getSignalType(g1SignalName));
                devStg.setSignalType(g1SignalName, Signal.Type.INPUT);
                String g2SignalName = grantPair.getSecond();
                signalOriginalType.put(g2SignalName, devStg.getSignalType(g2SignalName));
                devStg.setSignalType(g2SignalName, Signal.Type.INPUT);
            }

            // Load environment STG
            Stg envStg = StgUtils.loadStg(envFile);
            if (envStg != null) {
                // Make sure that input signals of the device STG are also inputs in the environment STG
                Set<String> inputSignalNames = devStg.getSignalNames(Signal.Type.INPUT, null);
                Set<String> outputSignalNames = devStg.getSignalNames(Signal.Type.OUTPUT, null);
                StgUtils.restoreInterfaceSignals(envStg, inputSignalNames, outputSignalNames);
                // Convert mutex grants into inputs in environment STG
                for (Pair<String, String> grantPair: grantPairs) {
                    envStg.setSignalType(grantPair.getFirst(), Signal.Type.INPUT);
                    envStg.setSignalType(grantPair.getSecond(), Signal.Type.INPUT);
                }
            }

            // Write device STG into a .g file
            String devStgName = (envStg != null ? StgUtils.DEVICE_FILE_PREFIX : StgUtils.SYSTEM_FILE_PREFIX) + stgFileExtension;
            File devStgFile = new File(directory, devStgName);
            Result<? extends ExportOutput> devExportResult = CircuitStgUtils.exportStg(devStg, devStgFile, directory, monitor);
            if (devExportResult.getOutcome() != Outcome.SUCCESS) {
                if (devExportResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new MpsatChainOutput(devExportResult, null, null, null, toolchainPreparationSettings));
            }
            monitor.progressUpdate(0.10);

            // Generating system .g for deadlock and persistency checks (only if needed)
            File sysStgFile = null;
            File detailFile = null;
            Result<? extends PcompOutput>  pcompResult = null;
            if (checkDeadlock || checkPersistency) {
                if (envStg == null) {
                    sysStgFile = devStgFile;
                } else {
                    File envStgFile = new File(directory, StgUtils.ENVIRONMENT_FILE_PREFIX + stgFileExtension);
                    Result<? extends ExportOutput> envExportResult = CircuitStgUtils.exportStg(envStg, envStgFile, directory, monitor);
                    if (envExportResult.getOutcome() != Outcome.SUCCESS) {
                        if (envExportResult.getOutcome() == Outcome.CANCEL) {
                            return new Result<>(Outcome.CANCEL);
                        }
                        return new Result<>(Outcome.FAILURE,
                                new MpsatChainOutput(envExportResult, null, null, null, toolchainPreparationSettings));
                    }

                    // Generating .g for the whole system (circuit and environment)
                    sysStgFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + stgFileExtension);
                    detailFile = new File(directory, StgUtils.DETAIL_FILE_PREFIX + StgUtils.XML_FILE_EXTENSION);
                    pcompResult = CircuitStgUtils.composeDevWithEnv(devStgFile, envStgFile, sysStgFile, detailFile, directory, monitor);
                    if (pcompResult.getOutcome() != Outcome.SUCCESS) {
                        if (pcompResult.getOutcome() == Outcome.CANCEL) {
                            return new Result<>(Outcome.CANCEL);
                        }
                        return new Result<>(Outcome.FAILURE,
                                new MpsatChainOutput(devExportResult, pcompResult, null, null, toolchainPreparationSettings));
                    }
                }
                // Restore the original types of mutex grant in system STG
                Stg sysStg = StgUtils.loadStg(sysStgFile);
                for (Pair<String, String> grantPair: grantPairs) {
                    String g1SignalName = grantPair.getFirst();
                    sysStg.setSignalType(g1SignalName, signalOriginalType.get(g1SignalName));
                    String g2SignalName = grantPair.getSecond();
                    sysStg.setSignalType(g2SignalName, signalOriginalType.get(g2SignalName));
                }
                sysStgFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + StgUtils.MUTEX_FILE_SUFFIX + stgFileExtension);
                CircuitStgUtils.exportStg(sysStg, sysStgFile, directory, monitor);
            }
            monitor.progressUpdate(0.20);

            // Generating system .g for conformation check (only if needed) -- should be without environment internal signals
            File sysModStgFile = null;
            File detailModFile = null;
            Result<? extends PcompOutput>  pcompModResult = null;
            if ((envStg != null) && checkConformation) {
                Set<String> envSignalNames = envStg.getSignalNames(Signal.Type.INTERNAL, null);
                if (envSignalNames.isEmpty() && (sysStgFile != null)) {
                    sysModStgFile = sysStgFile;
                    detailModFile = detailFile;
                    pcompModResult = pcompResult;
                } else {
                    String fileSuffix = (sysStgFile == null) ? "" : StgUtils.MODIFIED_FILE_SUFFIX;
                    // Convert internal signals to dummies
                    StgUtils.convertInternalSignalsToDummies(envStg);
                    File envModStgFile = new File(directory, StgUtils.ENVIRONMENT_FILE_PREFIX + fileSuffix + stgFileExtension);
                    Result<? extends ExportOutput> envModExportResult = CircuitStgUtils.exportStg(envStg, envModStgFile, directory, monitor);
                    if (envModExportResult.getOutcome() != Outcome.SUCCESS) {
                        if (envModExportResult.getOutcome() == Outcome.CANCEL) {
                            return new Result<>(Outcome.CANCEL);
                        }
                        return new Result<>(Outcome.FAILURE,
                                new MpsatChainOutput(envModExportResult, null, null, null, toolchainPreparationSettings));
                    }

                    // Generating .g for the whole system (circuit and environment) without internal signals
                    sysModStgFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + fileSuffix + stgFileExtension);
                    detailModFile = new File(directory, StgUtils.DETAIL_FILE_PREFIX + fileSuffix + StgUtils.XML_FILE_EXTENSION);
                    pcompModResult = CircuitStgUtils.composeDevWithEnv(devStgFile, envModStgFile, sysModStgFile, detailModFile, directory, monitor);
                    if (pcompModResult.getOutcome() != Outcome.SUCCESS) {
                        if (pcompModResult.getOutcome() == Outcome.CANCEL) {
                            return new Result<>(Outcome.CANCEL);
                        }
                        return new Result<>(Outcome.FAILURE,
                                new MpsatChainOutput(devExportResult, pcompModResult, null, null, toolchainPreparationSettings));
                    }
                    // Restore the original types of mutex grant in modified system STG
                    Stg sysModStg = StgUtils.loadStg(sysModStgFile);
                    for (Pair<String, String> grantPair: grantPairs) {
                        String g1SignalName = grantPair.getFirst();
                        sysModStg.setSignalType(g1SignalName, signalOriginalType.get(g1SignalName));
                        String g2SignalName = grantPair.getSecond();
                        sysModStg.setSignalType(g2SignalName, signalOriginalType.get(g2SignalName));
                    }
                    sysModStgFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + fileSuffix + StgUtils.MUTEX_FILE_SUFFIX + stgFileExtension);
                    CircuitStgUtils.exportStg(sysModStg, sysModStgFile, directory, monitor);
                }
            }
            monitor.progressUpdate(0.30);

            // Generate unfolding for deadlock and output persistency checks (only if needed)
            File unfoldingFile = null;
            PunfTask punfTask = null;
            Result<? extends PunfOutput> punfResult = null;
            if (checkDeadlock || checkPersistency) {
                unfoldingFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + PunfTask.PNML_FILE_EXTENSION);
                punfTask = new PunfTask(sysStgFile.getAbsolutePath(), unfoldingFile.getAbsolutePath());
                SubtaskMonitor<Object> punfMonitor = new SubtaskMonitor<>(monitor);
                punfResult = manager.execute(punfTask, "Unfolding .g", punfMonitor);

                if (punfResult.getOutcome() != Outcome.SUCCESS) {
                    if (punfResult.getOutcome() == Outcome.CANCEL) {
                        return new Result<>(Outcome.CANCEL);
                    }
                    return new Result<>(Outcome.FAILURE,
                            new MpsatChainOutput(devExportResult, pcompResult, punfResult, null, toolchainPreparationSettings));
                }
            }
            // Generate unfolding for conformation checks (if needed)
            File unfoldingModFile = unfoldingFile;
            PunfTask punfModTask = punfTask;
            Result<? extends PunfOutput> punfModResult = punfResult;
            if ((envStg != null) && checkConformation) {
                if ((sysStgFile != sysModStgFile) || (unfoldingModFile == null)) {
                    String fileSuffix = (sysStgFile == null) ? "" : StgUtils.MODIFIED_FILE_SUFFIX;
                    unfoldingModFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + fileSuffix + PunfTask.PNML_FILE_EXTENSION);
                    punfModTask = new PunfTask(sysModStgFile.getAbsolutePath(), unfoldingModFile.getAbsolutePath());
                    SubtaskMonitor<Object> punfModMonitor = new SubtaskMonitor<>(monitor);
                    punfModResult = manager.execute(punfModTask, "Unfolding .g", punfModMonitor);

                    if (punfModResult.getOutcome() != Outcome.SUCCESS) {
                        if (punfModResult.getOutcome() == Outcome.CANCEL) {
                            return new Result<>(Outcome.CANCEL);
                        }
                        return new Result<>(Outcome.FAILURE,
                                new MpsatChainOutput(devExportResult, pcompModResult, punfModResult, null, toolchainPreparationSettings));
                    }
                }
            }
            monitor.progressUpdate(0.40);

            // Check for deadlock (if requested)
            if (checkDeadlock) {
                MpsatParameters deadlockSettings = MpsatParameters.getDeadlockSettings();
                MpsatTask mpsatDeadlockTask = new MpsatTask(deadlockSettings.getMpsatArguments(directory),
                        unfoldingFile, directory);
                SubtaskMonitor<Object> mpsatMonitor = new SubtaskMonitor<>(monitor);
                Result<? extends MpsatOutput> mpsatDeadlockResult = manager.execute(
                        mpsatDeadlockTask, "Running deadlock check [MPSat]", mpsatMonitor);

                if (mpsatDeadlockResult.getOutcome() != Outcome.SUCCESS) {
                    if (mpsatDeadlockResult.getOutcome() == Outcome.CANCEL) {
                        return new Result<>(Outcome.CANCEL);
                    }
                    return new Result<>(Outcome.FAILURE,
                            new MpsatChainOutput(devExportResult, pcompResult, punfResult, mpsatDeadlockResult, deadlockSettings));
                }
                monitor.progressUpdate(0.50);

                MpsatOutputParser mpsatDeadlockParser = new MpsatOutputParser(mpsatDeadlockResult.getPayload());
                if (!mpsatDeadlockParser.getSolutions().isEmpty()) {
                    return new Result<>(Outcome.SUCCESS,
                            new MpsatChainOutput(devExportResult, pcompResult, punfResult, mpsatDeadlockResult, deadlockSettings,
                                    "Circuit has a deadlock after the following trace(s):"));
                }
            }
            monitor.progressUpdate(0.60);

            // Check for persistency (if requested)
            if (checkPersistency) {
                MpsatParameters persistencySettings = MpsatParameters.getOutputPersistencySettings(grantPairs);
                MpsatTask mpsatPersistencyTask = new MpsatTask(persistencySettings.getMpsatArguments(directory),
                        unfoldingFile, directory, sysStgFile);
                SubtaskMonitor<Object> mpsatMonitor = new SubtaskMonitor<>(monitor);
                Result<? extends MpsatOutput>  mpsatPersistencyResult = manager.execute(
                        mpsatPersistencyTask, "Running output persistency check [MPSat]", mpsatMonitor);

                if (mpsatPersistencyResult.getOutcome() != Outcome.SUCCESS) {
                    if (mpsatPersistencyResult.getOutcome() == Outcome.CANCEL) {
                        return new Result<>(Outcome.CANCEL);
                    }
                    return new Result<>(Outcome.FAILURE,
                            new MpsatChainOutput(devExportResult, pcompResult, punfResult, mpsatPersistencyResult, persistencySettings));
                }
                monitor.progressUpdate(0.70);

                MpsatOutputParser mpsatPersistencyParser = new MpsatOutputParser(mpsatPersistencyResult.getPayload());
                if (!mpsatPersistencyParser.getSolutions().isEmpty()) {
                    return new Result<>(Outcome.SUCCESS,
                            new MpsatChainOutput(devExportResult, pcompResult, punfResult, mpsatPersistencyResult, persistencySettings,
                                    "Circuit is not output-persistent after the following trace(s):"));
                }
            }
            monitor.progressUpdate(0.80);

            // Check for conformation (only if requested and if the environment is specified)
            if ((envStg != null) && checkConformation) {
                CompositionData compositionData = new CompositionData(detailModFile);
                ComponentData devComponentData = compositionData.getComponentData(devStgFile);
                Set<String> devPlaceNames = devComponentData.getDstPlaces();
                MpsatParameters conformationSettings = MpsatParameters.getConformationSettings(devPlaceNames);
                MpsatTask mpsatConformationTask = new MpsatTask(conformationSettings.getMpsatArguments(directory),
                        unfoldingModFile, directory, sysModStgFile);
                SubtaskMonitor<Object> mpsatMonitor = new SubtaskMonitor<>(monitor);
                Result<? extends MpsatOutput>  mpsatConformationResult = manager.execute(
                        mpsatConformationTask, "Running conformation check [MPSat]", mpsatMonitor);

                if (mpsatConformationResult.getOutcome() != Outcome.SUCCESS) {
                    if (mpsatConformationResult.getOutcome() == Outcome.CANCEL) {
                        return new Result<>(Outcome.CANCEL);
                    }
                    return new Result<>(Outcome.FAILURE,
                            new MpsatChainOutput(devExportResult, pcompModResult, punfModResult, mpsatConformationResult, conformationSettings));
                }
                monitor.progressUpdate(0.90);

                MpsatOutputParser mpsatConformationParser = new MpsatOutputParser(mpsatConformationResult.getPayload());
                if (!mpsatConformationParser.getSolutions().isEmpty()) {
                    return new Result<>(Outcome.SUCCESS,
                            new MpsatChainOutput(devExportResult, pcompModResult, punfModResult, mpsatConformationResult, conformationSettings,
                                    "Circuit does not conform to the environment after the following trace(s):"));
                }
            }
            monitor.progressUpdate(1.00);

            // Success
            Result<? extends MpsatOutput>  mpsatResult = new Result<>(Outcome.SUCCESS);
            String message = getSuccessMessage(envFile);
            return new Result<>(Outcome.SUCCESS,
                    new MpsatChainOutput(devExportResult, pcompResult, punfResult, mpsatResult, toolchainCompletionSettings, message));

        } catch (Throwable e) {
            return new Result<>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
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
            return "  * " + message + "\n";
        }
        return message;
    }

}
