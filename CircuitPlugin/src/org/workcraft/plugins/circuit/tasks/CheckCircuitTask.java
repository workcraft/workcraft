package org.workcraft.plugins.circuit.tasks;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
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
import org.workcraft.plugins.mpsat.MpsatResultParser;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.plugins.mpsat.tasks.MpsatTask;
import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgUtils;
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
    public Result<? extends MpsatChainResult> run(ProgressMonitor<? super MpsatChainResult> monitor) {
        Framework framework = Framework.getInstance();
        WorkspaceEntry we = getWorkspaceEntry();
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        try {
            // Common variables
            VisualCircuit visualCircuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
            File envFile = visualCircuit.getEnvironmentFile();
            LinkedList<Pair<String, String>> grantPairs = getMutexGrantPairs(we);

            // Load device STG
            CircuitToStgConverter converter = new CircuitToStgConverter(visualCircuit);
            Stg devStg = (Stg) converter.getStg().getMathModel();

            // Load environment STG
            Stg envStg = StgUtils.loadStg(envFile);
            if (envStg != null) {
                // Make sure that input signals of the device STG are also inputs in the environment STG
                Set<String> inputSignalNames = devStg.getSignalNames(Type.INPUT, null);
                Set<String> outputSignalNames = devStg.getSignalNames(Type.OUTPUT, null);
                CircuitStgUtils.restoreInterfaceSignals(envStg, inputSignalNames, outputSignalNames);
            }

            // Convert mutex grants into inputs bith in device and environemnt STGs
            for (Pair<String, String> grantPair: grantPairs) {
                devStg.setSignalType(grantPair.getFirst(), Type.INPUT);
                devStg.setSignalType(grantPair.getSecond(), Type.INPUT);
                envStg.setSignalType(grantPair.getFirst(), Type.INPUT);
                envStg.setSignalType(grantPair.getSecond(), Type.INPUT);
            }

            // Write device STG into a .g file
            String devStgName = (envStg != null ? StgUtils.DEVICE_FILE_NAME : StgUtils.SYSTEM_FILE_NAME) + StgUtils.ASTG_FILE_EXT;
            File devStgFile = new File(directory, devStgName);
            Result<? extends Object> devExportResult = CircuitStgUtils.exportStg(devStg, devStgFile, directory, monitor);
            if (devExportResult.getOutcome() != Outcome.FINISHED) {
                if (devExportResult.getOutcome() == Outcome.CANCELLED) {
                    return new Result<MpsatChainResult>(Outcome.CANCELLED);
                }
                return new Result<MpsatChainResult>(Outcome.FAILED,
                        new MpsatChainResult(devExportResult, null, null, null, toolchainPreparationSettings));
            }
            monitor.progressUpdate(0.10);

            // Generating system .g for deadlock and persistency checks (only if needed)
            File sysStgFile = null;
            File placesFile = null;
            Result<? extends ExternalProcessResult>  pcompResult = null;
            if (checkDeadlock || checkPersistency) {
                if (envStg == null) {
                    sysStgFile = devStgFile;
                } else {
                    File envStgFile = new File(directory, StgUtils.ENVIRONMENT_FILE_NAME + StgUtils.ASTG_FILE_EXT);
                    Result<? extends Object> envExportResult = CircuitStgUtils.exportStg(envStg, envStgFile, directory, monitor);
                    if (envExportResult.getOutcome() != Outcome.FINISHED) {
                        if (envExportResult.getOutcome() == Outcome.CANCELLED) {
                            return new Result<MpsatChainResult>(Outcome.CANCELLED);
                        }
                        return new Result<MpsatChainResult>(Outcome.FAILED,
                                new MpsatChainResult(envExportResult, null, null, null, toolchainPreparationSettings));
                    }

                    // Generating .g for the whole system (circuit and environment)
                    sysStgFile = new File(directory, StgUtils.SYSTEM_FILE_NAME + StgUtils.ASTG_FILE_EXT);
                    placesFile = new File(directory, StgUtils.PLACES_FILE_NAME + StgUtils.LIST_FILE_EXT);
                    pcompResult = CircuitStgUtils.composeDevWithEnv(devStgFile, envStgFile, sysStgFile, placesFile, directory, monitor);
                    if (pcompResult.getOutcome() != Outcome.FINISHED) {
                        if (pcompResult.getOutcome() == Outcome.CANCELLED) {
                            return new Result<MpsatChainResult>(Outcome.CANCELLED);
                        }
                        return new Result<MpsatChainResult>(Outcome.FAILED,
                                new MpsatChainResult(devExportResult, pcompResult, null, null, toolchainPreparationSettings));
                    }
                }
            }
            monitor.progressUpdate(0.20);

            // Generating system .g for conformation check (only if needed) -- should be without environment internal signals
            File sysModStgFile = null;
            File placesModFile = null;
            Result<? extends ExternalProcessResult>  pcompModResult = null;
            if ((envStg != null) && checkConformation) {
                Set<String> envSignalNames = envStg.getSignalNames(Type.INTERNAL, null);
                if (envSignalNames.isEmpty() && (sysStgFile != null)) {
                    sysModStgFile = sysStgFile;
                    placesModFile = placesFile;
                    pcompModResult = pcompResult;
                } else {
                    String fileSuffix = (sysStgFile == null) ? "" : StgUtils.MODIFIED_FILE_SUFFIX;
                    // Convert internal signals to dummies
                    CircuitStgUtils.convertInternalSignalsToDummies(envStg);
                    File envModStgFile = new File(directory, StgUtils.ENVIRONMENT_FILE_NAME + fileSuffix + StgUtils.ASTG_FILE_EXT);
                    Result<? extends Object> envModExportResult = CircuitStgUtils.exportStg(envStg, envModStgFile, directory, monitor);
                    if (envModExportResult.getOutcome() != Outcome.FINISHED) {
                        if (envModExportResult.getOutcome() == Outcome.CANCELLED) {
                            return new Result<MpsatChainResult>(Outcome.CANCELLED);
                        }
                        return new Result<MpsatChainResult>(Outcome.FAILED,
                                new MpsatChainResult(envModExportResult, null, null, null, toolchainPreparationSettings));
                    }

                    // Generating .g for the whole system (circuit and environment) without internal signals
                    sysModStgFile = new File(directory, StgUtils.SYSTEM_FILE_NAME + fileSuffix + StgUtils.ASTG_FILE_EXT);
                    placesModFile = new File(directory, StgUtils.PLACES_FILE_NAME + fileSuffix + StgUtils.LIST_FILE_EXT);
                    pcompModResult = CircuitStgUtils.composeDevWithEnv(devStgFile, envModStgFile, sysModStgFile, placesModFile, directory, monitor);
                    if (pcompModResult.getOutcome() != Outcome.FINISHED) {
                        if (pcompModResult.getOutcome() == Outcome.CANCELLED) {
                            return new Result<MpsatChainResult>(Outcome.CANCELLED);
                        }
                        return new Result<MpsatChainResult>(Outcome.FAILED,
                                new MpsatChainResult(devExportResult, pcompModResult, null, null, toolchainPreparationSettings));
                    }
                }
            }
            monitor.progressUpdate(0.30);

            // Generate unfolding for deadlock and output persistency checks (only if needed)
            File unfoldingFile = null;
            PunfTask punfTask = null;
            Result<? extends ExternalProcessResult> punfResult = null;
            final TaskManager taskManager = framework.getTaskManager();
            if (checkDeadlock || checkPersistency) {
                unfoldingFile = new File(directory, StgUtils.SYSTEM_FILE_NAME + PunfSettings.getUnfoldingExtension(true));
                punfTask = new PunfTask(sysStgFile.getAbsolutePath(), unfoldingFile.getAbsolutePath());
                SubtaskMonitor<Object> punfMonitor = new SubtaskMonitor<>(monitor);
                punfResult = taskManager.execute(punfTask, "Unfolding .g", punfMonitor);

                if (punfResult.getOutcome() != Outcome.FINISHED) {
                    if (punfResult.getOutcome() == Outcome.CANCELLED) {
                        return new Result<MpsatChainResult>(Outcome.CANCELLED);
                    }
                    return new Result<MpsatChainResult>(Outcome.FAILED,
                            new MpsatChainResult(devExportResult, pcompResult, punfResult, null, toolchainPreparationSettings));
                }
            }
            // Generate unfolding for conformation checks (if needed)
            File unfoldingModFile = unfoldingFile;
            PunfTask punfModTask = punfTask;
            Result<? extends ExternalProcessResult> punfModResult = punfResult;
            if ((envStg != null) && checkConformation) {
                if ((sysStgFile != sysModStgFile) || (unfoldingModFile == null)) {
                    String fileSuffix = (sysStgFile == null) ? "" : StgUtils.MODIFIED_FILE_SUFFIX;
                    unfoldingModFile = new File(directory, StgUtils.SYSTEM_FILE_NAME + fileSuffix + PunfSettings.getUnfoldingExtension(true));
                    punfModTask = new PunfTask(sysModStgFile.getAbsolutePath(), unfoldingModFile.getAbsolutePath());
                    SubtaskMonitor<Object> punfModMonitor = new SubtaskMonitor<>(monitor);
                    punfModResult = taskManager.execute(punfModTask, "Unfolding .g", punfModMonitor);

                    if (punfModResult.getOutcome() != Outcome.FINISHED) {
                        if (punfModResult.getOutcome() == Outcome.CANCELLED) {
                            return new Result<MpsatChainResult>(Outcome.CANCELLED);
                        }
                        return new Result<MpsatChainResult>(Outcome.FAILED,
                                new MpsatChainResult(devExportResult, pcompModResult, punfModResult, null, toolchainPreparationSettings));
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
                Result<? extends ExternalProcessResult> mpsatDeadlockResult = taskManager.execute(
                        mpsatDeadlockTask, "Running deadlock check [MPSat]", mpsatMonitor);

                if (mpsatDeadlockResult.getOutcome() != Outcome.FINISHED) {
                    if (mpsatDeadlockResult.getOutcome() == Outcome.CANCELLED) {
                        return new Result<MpsatChainResult>(Outcome.CANCELLED);
                    }
                    return new Result<MpsatChainResult>(Outcome.FAILED,
                            new MpsatChainResult(devExportResult, pcompResult, punfResult, mpsatDeadlockResult, deadlockSettings));
                }
                monitor.progressUpdate(0.50);

                MpsatResultParser mpsatDeadlockParser = new MpsatResultParser(mpsatDeadlockResult.getReturnValue());
                if (!mpsatDeadlockParser.getSolutions().isEmpty()) {
                    return new Result<MpsatChainResult>(Outcome.FINISHED,
                            new MpsatChainResult(devExportResult, pcompResult, punfResult, mpsatDeadlockResult, deadlockSettings,
                                    "Circuit has a deadlock after the following trace(s):"));
                }
            }
            monitor.progressUpdate(0.60);

            // Check for persistency (if requested)
            if (checkPersistency) {
                MpsatParameters persistencySettings = MpsatParameters.getOutputPersistencySettings(grantPairs);
                MpsatTask mpsatPersistencyTask = new MpsatTask(persistencySettings.getMpsatArguments(directory),
                        unfoldingFile, directory, true, sysStgFile);
                SubtaskMonitor<Object> mpsatMonitor = new SubtaskMonitor<>(monitor);
                Result<? extends ExternalProcessResult>  mpsatPersistencyResult = taskManager.execute(
                        mpsatPersistencyTask, "Running output persistency check [MPSat]", mpsatMonitor);

                if (mpsatPersistencyResult.getOutcome() != Outcome.FINISHED) {
                    if (mpsatPersistencyResult.getOutcome() == Outcome.CANCELLED) {
                        return new Result<MpsatChainResult>(Outcome.CANCELLED);
                    }
                    return new Result<MpsatChainResult>(Outcome.FAILED,
                            new MpsatChainResult(devExportResult, pcompResult, punfResult, mpsatPersistencyResult, persistencySettings));
                }
                monitor.progressUpdate(0.70);

                MpsatResultParser mpsatPersistencyParser = new MpsatResultParser(mpsatPersistencyResult.getReturnValue());
                if (!mpsatPersistencyParser.getSolutions().isEmpty()) {
                    return new Result<MpsatChainResult>(Outcome.FINISHED,
                            new MpsatChainResult(devExportResult, pcompResult, punfResult, mpsatPersistencyResult, persistencySettings,
                                    "Circuit is not output persistent after the following trace(s):"));
                }
            }
            monitor.progressUpdate(0.80);

            // Check for interface conformation (only if requested and if the environment is specified)
            if ((envStg != null) && checkConformation) {
                byte[] placesList = FileUtils.readAllBytes(placesModFile);
                Set<String> devPlaceNames = parsePlaceNames(placesList, 0);
                MpsatParameters conformationSettings = MpsatParameters.getConformationSettings(devPlaceNames);
                MpsatTask mpsatConformationTask = new MpsatTask(conformationSettings.getMpsatArguments(directory),
                        unfoldingModFile, directory, true, sysModStgFile, placesModFile);
                SubtaskMonitor<Object> mpsatMonitor = new SubtaskMonitor<>(monitor);
                Result<? extends ExternalProcessResult>  mpsatConformationResult = taskManager.execute(
                        mpsatConformationTask, "Running conformation check [MPSat]", mpsatMonitor);

                if (mpsatConformationResult.getOutcome() != Outcome.FINISHED) {
                    if (mpsatConformationResult.getOutcome() == Outcome.CANCELLED) {
                        return new Result<MpsatChainResult>(Outcome.CANCELLED);
                    }
                    return new Result<MpsatChainResult>(Outcome.FAILED,
                            new MpsatChainResult(devExportResult, pcompModResult, punfModResult, mpsatConformationResult, conformationSettings));
                }
                monitor.progressUpdate(0.90);

                MpsatResultParser mpsatConformationParser = new MpsatResultParser(mpsatConformationResult.getReturnValue());
                if (!mpsatConformationParser.getSolutions().isEmpty()) {
                    return new Result<MpsatChainResult>(Outcome.FINISHED,
                            new MpsatChainResult(devExportResult, pcompModResult, punfModResult, mpsatConformationResult, conformationSettings,
                                    "Circuit does not conform to the environment after the following trace(s):"));
                }
            }
            monitor.progressUpdate(1.00);

            // Success
            String message = getSuccessMessage(envFile);
            return new Result<MpsatChainResult>(Outcome.FINISHED,
                    new MpsatChainResult(devExportResult, pcompResult, punfResult, null, toolchainCompletionSettings, message));

        } catch (Throwable e) {
            return new Result<MpsatChainResult>(e);
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

    private HashSet<String> parsePlaceNames(byte[] bufferedInput, int lineIndex) {
        HashSet<String> result = new HashSet<>();
        InputStream is = new ByteArrayInputStream(bufferedInput);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        try {
            String line = null;
            while ((lineIndex >= 0) && ((line = br.readLine()) != null)) {
                lineIndex--;
            }
            if (line != null) {
                for (String name: line.trim().split("\\s")) {
                    result.add(name);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String getSuccessMessage(File environmentFile) {
        String message = "";
        boolean hasEnvironment = (environmentFile != null) && environmentFile.exists();
        if (hasEnvironment) {
            message = "Under the given environment (" + environmentFile.getName() + ")";
        } else {
            message = "Without environment restrictions";
        }
        message += " the circuit is:\n";
        if (checkConformation) {
            message += "  * conformant\n";
        }
        if (checkDeadlock) {
            message += "  * deadlock-free\n";
        }
        if (checkPersistency) {
            message += "  * output persistent\n";
        }
        return message;
    }

}
