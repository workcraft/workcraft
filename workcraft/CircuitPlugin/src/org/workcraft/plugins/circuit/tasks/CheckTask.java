package org.workcraft.plugins.circuit.tasks;

import org.workcraft.Framework;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.stg.CircuitStgUtils;
import org.workcraft.plugins.circuit.stg.CircuitToStgConverter;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.tasks.VerificationChainOutput;
import org.workcraft.plugins.mpsat.tasks.VerificationOutput;
import org.workcraft.plugins.mpsat.tasks.VerificationOutputParser;
import org.workcraft.plugins.mpsat.tasks.VerificationTask;
import org.workcraft.plugins.mpsat.utils.ReachUtils;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.types.Pair;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
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
            LinkedList<Pair<String, String>> grantPairs = getMutexGrantPairs(we);

            // Load device STG
            CircuitToStgConverter converter = new CircuitToStgConverter(circuit);
            Stg devStg = converter.getStg().getMathModel();
            // Expose mutex grants as outputs in the device STG (store the original signal type to apply in composition STG)
            HashMap<String, Signal.Type> signalOriginalType = new HashMap<>();
            for (Pair<String, String> grantPair: grantPairs) {
                String g1SignalName = grantPair.getFirst();
                signalOriginalType.put(g1SignalName, devStg.getSignalType(g1SignalName));
                devStg.setSignalType(g1SignalName, Signal.Type.OUTPUT);
                String g2SignalName = grantPair.getSecond();
                signalOriginalType.put(g2SignalName, devStg.getSignalType(g2SignalName));
                devStg.setSignalType(g2SignalName, Signal.Type.OUTPUT);
            }

            // Load environment STG
            Stg envStg = StgUtils.loadStg(envFile);
            if (envStg != null) {
                // Make sure that input signals of the device STG are also inputs in the environment STG
                Set<String> inputSignals = devStg.getSignalReferences(Signal.Type.INPUT);
                Set<String> outputSignals = devStg.getSignalReferences(Signal.Type.OUTPUT);
                StgUtils.restoreInterfaceSignals(envStg, inputSignals, outputSignals);
            }

            // Write device STG into a .g file
            String devStgName = (envStg != null ? StgUtils.DEVICE_FILE_PREFIX : StgUtils.SYSTEM_FILE_PREFIX) + stgFileExtension;
            File devStgFile = new File(directory, devStgName);
            Result<? extends ExportOutput> devExportResult = StgUtils.exportStg(devStg, devStgFile, monitor);
            if (devExportResult.getOutcome() != Outcome.SUCCESS) {
                if (devExportResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new VerificationChainOutput(devExportResult, null, null, null, preparationParameters));
            }
            monitor.progressUpdate(0.1);

            // Generating system .g for deadlock freeness and output persistency checks (only if needed)
            File sysStgFile = null;
            File detailFile = null;
            Result<? extends PcompOutput>  pcompResult = null;
            if (checkDeadlock || checkPersistency) {
                if (envStg == null) {
                    sysStgFile = devStgFile;
                } else {
                    File envStgFile = new File(directory, StgUtils.ENVIRONMENT_FILE_PREFIX + stgFileExtension);
                    Result<? extends ExportOutput> envExportResult = StgUtils.exportStg(envStg, envStgFile, monitor);
                    if (envExportResult.getOutcome() != Outcome.SUCCESS) {
                        if (envExportResult.getOutcome() == Outcome.CANCEL) {
                            return new Result<>(Outcome.CANCEL);
                        }
                        return new Result<>(Outcome.FAILURE,
                                new VerificationChainOutput(envExportResult, null, null, null, preparationParameters));
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
                                new VerificationChainOutput(devExportResult, pcompResult, null, null, preparationParameters));
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
                StgUtils.exportStg(sysStg, sysStgFile, monitor);
            }
            monitor.progressUpdate(0.2);

            // Generating system .g for conformation check (only if needed) -- should be without environment internal signals
            File sysModStgFile = null;
            File detailModFile = null;
            Result<? extends PcompOutput>  pcompModResult = null;
            if ((envStg != null) && checkConformation) {
                Set<String> envSignalNames = envStg.getSignalReferences(Signal.Type.INTERNAL);
                if (envSignalNames.isEmpty() && (sysStgFile != null)) {
                    sysModStgFile = sysStgFile;
                    detailModFile = detailFile;
                    pcompModResult = pcompResult;
                } else {
                    String fileSuffix = (sysStgFile == null) ? "" : StgUtils.MODIFIED_FILE_SUFFIX;
                    // Convert internal signals to dummies
                    StgUtils.convertInternalSignalsToDummies(envStg);
                    File envModStgFile = new File(directory, StgUtils.ENVIRONMENT_FILE_PREFIX + fileSuffix + stgFileExtension);
                    Result<? extends ExportOutput> envModExportResult = StgUtils.exportStg(envStg, envModStgFile, monitor);
                    if (envModExportResult.getOutcome() != Outcome.SUCCESS) {
                        if (envModExportResult.getOutcome() == Outcome.CANCEL) {
                            return new Result<>(Outcome.CANCEL);
                        }
                        return new Result<>(Outcome.FAILURE,
                                new VerificationChainOutput(envModExportResult, null, null, null, preparationParameters));
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
                                new VerificationChainOutput(devExportResult, pcompModResult, null, null, preparationParameters));
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
                    StgUtils.exportStg(sysModStg, sysModStgFile, monitor);
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

                if (punfResult.getOutcome() != Outcome.SUCCESS) {
                    if (punfResult.getOutcome() == Outcome.CANCEL) {
                        return new Result<>(Outcome.CANCEL);
                    }
                    return new Result<>(Outcome.FAILURE,
                            new VerificationChainOutput(devExportResult, pcompResult, punfResult, null, preparationParameters));
                }
            }

            // Generate unfolding for conformation checks (if needed)
            File unfoldingModFile = unfoldingFile;
            Result<? extends PunfOutput> punfModResult = punfResult;
            if ((envStg != null) && checkConformation) {
                if ((sysStgFile != sysModStgFile) || (unfoldingModFile == null)) {
                    String fileSuffix = (sysStgFile == null) ? "" : StgUtils.MODIFIED_FILE_SUFFIX;
                    unfoldingModFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + fileSuffix + PunfTask.PNML_FILE_EXTENSION);
                    PunfTask punfModTask = new PunfTask(sysModStgFile, unfoldingModFile, directory);
                    SubtaskMonitor<Object> punfModMonitor = new SubtaskMonitor<>(monitor);
                    punfModResult = manager.execute(punfModTask, "Unfolding .g", punfModMonitor);

                    if (punfModResult.getOutcome() != Outcome.SUCCESS) {
                        if (punfModResult.getOutcome() == Outcome.CANCEL) {
                            return new Result<>(Outcome.CANCEL);
                        }
                        return new Result<>(Outcome.FAILURE,
                                new VerificationChainOutput(devExportResult, pcompModResult, punfModResult, null, preparationParameters));
                    }
                }
            }
            monitor.progressUpdate(0.4);

            // Check for conformation (only if requested and if the environment is specified)
            if ((envStg != null) && checkConformation) {
                CompositionData compositionData = new CompositionData(detailModFile);
                ComponentData devComponentData = compositionData.getComponentData(devStgFile);
                Set<String> devPlaceNames = devComponentData.getDstPlaces();
                VerificationParameters conformationParameters = ReachUtils.getConformationParameters(devPlaceNames);
                VerificationTask mpsatConformationTask = new VerificationTask(unfoldingModFile, sysModStgFile, conformationParameters, directory);
                SubtaskMonitor<Object> mpsatMonitor = new SubtaskMonitor<>(monitor);
                Result<? extends VerificationOutput>  mpsatConformationResult = manager.execute(
                        mpsatConformationTask, "Running conformation check [MPSat]", mpsatMonitor);

                if (mpsatConformationResult.getOutcome() != Outcome.SUCCESS) {
                    if (mpsatConformationResult.getOutcome() == Outcome.CANCEL) {
                        return new Result<>(Outcome.CANCEL);
                    }
                    return new Result<>(Outcome.FAILURE,
                            new VerificationChainOutput(devExportResult, pcompModResult, punfModResult, mpsatConformationResult, conformationParameters));
                }
                monitor.progressUpdate(0.5);

                String mpsatConformationStdout = mpsatConformationResult.getPayload().getStdoutString();
                VerificationOutputParser mpsatConformationParser = new VerificationOutputParser(mpsatConformationStdout);
                if (!mpsatConformationParser.getSolutions().isEmpty()) {
                    return new Result<>(Outcome.SUCCESS,
                            new VerificationChainOutput(devExportResult, pcompModResult, punfModResult, mpsatConformationResult, conformationParameters,
                                    "Circuit does not conform to the environment after the following trace(s):"));
                }
            }
            monitor.progressUpdate(0.6);

            // Check for deadlock (if requested)
            if (checkDeadlock) {
                VerificationParameters deadlockParameters = ReachUtils.getDeadlockParameters();
                VerificationTask mpsatDeadlockTask = new VerificationTask(unfoldingFile, sysStgFile, deadlockParameters, directory);
                SubtaskMonitor<Object> mpsatMonitor = new SubtaskMonitor<>(monitor);
                Result<? extends VerificationOutput> mpsatDeadlockResult = manager.execute(
                        mpsatDeadlockTask, "Running deadlock check [MPSat]", mpsatMonitor);

                if (mpsatDeadlockResult.getOutcome() != Outcome.SUCCESS) {
                    if (mpsatDeadlockResult.getOutcome() == Outcome.CANCEL) {
                        return new Result<>(Outcome.CANCEL);
                    }
                    return new Result<>(Outcome.FAILURE,
                            new VerificationChainOutput(devExportResult, pcompResult, punfResult, mpsatDeadlockResult, deadlockParameters));
                }
                monitor.progressUpdate(0.7);

                String mpsatDeadlockStdout = mpsatDeadlockResult.getPayload().getStdoutString();
                VerificationOutputParser mpsatDeadlockParser = new VerificationOutputParser(mpsatDeadlockStdout);
                if (!mpsatDeadlockParser.getSolutions().isEmpty()) {
                    return new Result<>(Outcome.SUCCESS,
                            new VerificationChainOutput(devExportResult, pcompResult, punfResult, mpsatDeadlockResult, deadlockParameters,
                                    "Circuit has a deadlock after the following trace(s):"));
                }
            }
            monitor.progressUpdate(0.8);

            // Check for persistency (if requested)
            if (checkPersistency) {
                VerificationParameters persistencyParameters = ReachUtils.getOutputPersistencyParameters(grantPairs);
                VerificationTask mpsatPersistencyTask = new VerificationTask(unfoldingFile, sysStgFile, persistencyParameters, directory);
                SubtaskMonitor<Object> mpsatMonitor = new SubtaskMonitor<>(monitor);
                Result<? extends VerificationOutput>  mpsatPersistencyResult = manager.execute(
                        mpsatPersistencyTask, "Running output persistency check [MPSat]", mpsatMonitor);

                if (mpsatPersistencyResult.getOutcome() != Outcome.SUCCESS) {
                    if (mpsatPersistencyResult.getOutcome() == Outcome.CANCEL) {
                        return new Result<>(Outcome.CANCEL);
                    }
                    return new Result<>(Outcome.FAILURE,
                            new VerificationChainOutput(devExportResult, pcompResult, punfResult, mpsatPersistencyResult, persistencyParameters));
                }
                monitor.progressUpdate(0.9);

                String mpsatPersistencyStdout = mpsatPersistencyResult.getPayload().getStdoutString();
                VerificationOutputParser mpsatPersistencyParser = new VerificationOutputParser(mpsatPersistencyStdout);
                if (!mpsatPersistencyParser.getSolutions().isEmpty()) {
                    return new Result<>(Outcome.SUCCESS,
                            new VerificationChainOutput(devExportResult, pcompResult, punfResult, mpsatPersistencyResult, persistencyParameters,
                                    "Circuit is not output-persistent after the following trace(s):"));
                }
            }
            monitor.progressUpdate(1.0);

            // Success
            Result<? extends VerificationOutput>  mpsatResult = new Result<>(Outcome.SUCCESS);
            VerificationParameters completionParameters = ReachUtils.getToolchainCompletionParameters();
            String message = getSuccessMessage(envFile);
            return new Result<>(Outcome.SUCCESS,
                    new VerificationChainOutput(devExportResult, pcompResult, punfResult, mpsatResult, completionParameters, message));

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
