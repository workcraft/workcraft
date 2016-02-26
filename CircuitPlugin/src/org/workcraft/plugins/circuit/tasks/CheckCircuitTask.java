package org.workcraft.plugins.circuit.tasks;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.Framework;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.stg.CircuitStgUtils;
import org.workcraft.plugins.circuit.stg.CircuitToStgConverter;
import org.workcraft.plugins.mpsat.MpsatMode;
import org.workcraft.plugins.mpsat.MpsatResultParser;
import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.mpsat.MpsatUtilitySettings;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.plugins.mpsat.tasks.MpsatTask;
import org.workcraft.plugins.punf.PunfUtilitySettings;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class CheckCircuitTask extends MpsatChainTask {
    private final MpsatSettings toolchainPreparationSettings = new MpsatSettings("Toolchain preparation of data",
            MpsatMode.UNDEFINED, 0, null, 0);

    private final MpsatSettings toolchainCompletionSettings = new MpsatSettings("Toolchain completion",
            MpsatMode.UNDEFINED, 0, null, 0);

    private final MpsatSettings deadlockSettings = new MpsatSettings("Deadlock freeness",
            MpsatMode.DEADLOCK, 0, MpsatUtilitySettings.getSolutionMode(),
            MpsatUtilitySettings.getSolutionCount());

    private final MpsatSettings hazardSettings = new MpsatSettings("Output persistency",
            MpsatMode.STG_REACHABILITY, 0, MpsatUtilitySettings.getSolutionMode(),
            MpsatUtilitySettings.getSolutionCount(), MpsatSettings.reachSemimodularity, true);

    private final WorkspaceEntry we;
    private final boolean checkConformation;
    private final boolean checkDeadlock;
    private final boolean checkHazard;

    public CheckCircuitTask(WorkspaceEntry we, boolean checkConformation, boolean checkDeadlock, boolean checkHazard) {
        super(we, null);
        this.we = we;
        this.checkConformation = checkConformation;
        this.checkDeadlock = checkDeadlock;
        this.checkHazard = checkHazard;
    }

    @Override
    public Result<? extends MpsatChainResult> run(ProgressMonitor<? super MpsatChainResult> monitor) {
        Framework framework = Framework.getInstance();
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        try {
            // Common variables
            monitor.progressUpdate(0.05);
            VisualCircuit visualCircuit = (VisualCircuit) we.getModelEntry().getVisualModel();
            File envFile = visualCircuit.getEnvironmentFile();
            boolean hasEnvironment = (envFile != null) && envFile.exists();

            CircuitToStgConverter generator = new CircuitToStgConverter(visualCircuit);
            STG devStg = (STG) generator.getStg().getMathModel();
            String devStgName = (hasEnvironment ? StgUtils.DEVICE_FILE_NAME : StgUtils.SYSTEM_FILE_NAME) + StgUtils.ASTG_FILE_EXT;
            File devStgFile =  new File(directory, devStgName);
            Result<? extends Object> devExportResult = CircuitStgUtils.exportStg(devStg, devStgFile, directory, monitor);
            if (devExportResult.getOutcome() != Outcome.FINISHED) {
                if (devExportResult.getOutcome() == Outcome.CANCELLED) {
                    return new Result<MpsatChainResult>(Outcome.CANCELLED);
                }
                return new Result<MpsatChainResult>(Outcome.FAILED,
                        new MpsatChainResult(devExportResult, null, null, null, toolchainPreparationSettings));
            }
            monitor.progressUpdate(0.10);

            // Environment STG
            STG envStg = null;
            if (hasEnvironment) {
                envStg = (STG) framework.loadFile(envFile).getMathModel();
                // Make sure that input signals of the device STG are also inputs in the environment STG
                Set<String> inputSignalNames = devStg.getSignalNames(Type.INPUT, null);
                Set<String> outputSignalNames = devStg.getSignalNames(Type.OUTPUT, null);
                CircuitStgUtils.restoreInterfaceSignals(envStg, inputSignalNames, outputSignalNames);
            }

            // Generating system .g for deadlock and hazard checks (only if needed)
            File sysStgFile = null;
            File placesFile = null;
            Result<? extends ExternalProcessResult>  pcompResult = null;
            if (checkDeadlock || checkHazard) {
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
            if (checkConformation) {
                if (envStg == null) {
                    sysModStgFile = devStgFile;
                } else if (envStg.getSignalNames(Type.INTERNAL, null).isEmpty() && (sysStgFile != null)) {
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
                    sysModStgFile.deleteOnExit();
                    placesModFile = new File(directory, StgUtils.PLACES_FILE_NAME + fileSuffix + StgUtils.LIST_FILE_EXT);
                    placesModFile.deleteOnExit();
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

            // Generate unfolding for deadlock and hazard checks (only if needed)
            File unfoldingFile = null;
            PunfTask punfTask = null;
            Result<? extends ExternalProcessResult> punfResult = null;
            if (checkDeadlock || checkHazard) {
                unfoldingFile = new File(directory, StgUtils.SYSTEM_FILE_NAME + PunfUtilitySettings.getUnfoldingExtension(true));
                punfTask = new PunfTask(sysStgFile.getAbsolutePath(), unfoldingFile.getAbsolutePath());
                SubtaskMonitor<Object> punfMonitor = new SubtaskMonitor<>(monitor);
                punfResult = framework.getTaskManager().execute(punfTask, "Unfolding .g", punfMonitor);

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
            if (hasEnvironment && checkConformation) {
                if ((sysStgFile != sysModStgFile) || (unfoldingModFile == null)) {
                    String fileSuffix = (sysStgFile == null) ? "" : StgUtils.MODIFIED_FILE_SUFFIX;
                    unfoldingModFile = new File(directory, StgUtils.SYSTEM_FILE_NAME + fileSuffix + PunfUtilitySettings.getUnfoldingExtension(true));
                    punfModTask = new PunfTask(sysModStgFile.getAbsolutePath(), unfoldingModFile.getAbsolutePath());
                    SubtaskMonitor<Object> punfModMonitor = new SubtaskMonitor<>(monitor);
                    punfModResult = framework.getTaskManager().execute(punfModTask, "Unfolding .g", punfModMonitor);

                    if (punfModResult.getOutcome() != Outcome.FINISHED) {
                        if (punfModResult.getOutcome() == Outcome.CANCELLED) {
                            return new Result<MpsatChainResult>(Outcome.CANCELLED);
                        }
                        return new Result<MpsatChainResult>(Outcome.FAILED,
                                new MpsatChainResult(devExportResult, pcompModResult, punfModResult, null, toolchainPreparationSettings));
                    }
                    monitor.progressUpdate(0.40);
                }
            }
            monitor.progressUpdate(0.40);

            // Check for deadlock (if requested)
            if (checkDeadlock) {
                MpsatTask mpsatDeadlockTask = new MpsatTask(deadlockSettings.getMpsatArguments(directory),
                        unfoldingFile.getAbsolutePath(), directory, true);
                SubtaskMonitor<Object> mpsatMonitor = new SubtaskMonitor<>(monitor);
                Result<? extends ExternalProcessResult> mpsatDeadlockResult = framework.getTaskManager().execute(
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

            // Check for hazards (if requested)
            if (checkHazard) {
                MpsatTask mpsatHazardTask = new MpsatTask(hazardSettings.getMpsatArguments(directory),
                        unfoldingFile.getAbsolutePath(), directory, true);
                if (MpsatUtilitySettings.getDebugReach()) {
                    System.out.println("\nReach expression for the hazard property:");
                    System.out.println(hazardSettings.getReach());
                }
                SubtaskMonitor<Object> mpsatMonitor = new SubtaskMonitor<>(monitor);
                Result<? extends ExternalProcessResult>  mpsatHazardResult = framework.getTaskManager().execute(
                        mpsatHazardTask, "Running hazard check [MPSat]", mpsatMonitor);

                if (mpsatHazardResult.getOutcome() != Outcome.FINISHED) {
                    if (mpsatHazardResult.getOutcome() == Outcome.CANCELLED) {
                        return new Result<MpsatChainResult>(Outcome.CANCELLED);
                    }
                    return new Result<MpsatChainResult>(Outcome.FAILED,
                            new MpsatChainResult(devExportResult, pcompResult, punfResult, mpsatHazardResult, hazardSettings));
                }
                monitor.progressUpdate(0.70);

                MpsatResultParser mpsatHazardParser = new MpsatResultParser(mpsatHazardResult.getReturnValue());
                if (!mpsatHazardParser.getSolutions().isEmpty()) {
                    return new Result<MpsatChainResult>(Outcome.FINISHED,
                            new MpsatChainResult(devExportResult, pcompResult, punfResult, mpsatHazardResult, hazardSettings,
                                    "Circuit has a hazard after the following trace(s):"));
                }
            }
            monitor.progressUpdate(0.80);

            // Check for interface conformation (only if requested and if the environment is specified)
            if (hasEnvironment && checkConformation) {
                Set<String> devOutputNames = devStg.getSignalFlatNames(Type.OUTPUT);
                byte[] placesList = FileUtils.readAllBytes(placesModFile);
                Set<String> devPlaceNames = parsePlaceNames(placesList, 0);
                String reachConformation = MpsatSettings.genReachConformation(devOutputNames, devPlaceNames);
                if (MpsatUtilitySettings.getDebugReach()) {
                    System.out.println("\nReach expression for the interface conformation property:");
                    System.out.println(reachConformation);
                }
                MpsatSettings conformationSettings = new MpsatSettings("Interface conformance",
                        MpsatMode.STG_REACHABILITY, 0, MpsatUtilitySettings.getSolutionMode(),
                        MpsatUtilitySettings.getSolutionCount(), reachConformation, true);

                MpsatTask mpsatConformationTask = new MpsatTask(conformationSettings.getMpsatArguments(directory),
                        unfoldingModFile.getAbsolutePath(), directory, true);
                SubtaskMonitor<Object> mpsatMonitor = new SubtaskMonitor<>(monitor);
                Result<? extends ExternalProcessResult>  mpsatConformationResult = framework.getTaskManager().execute(
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
        message +=  " the circuit is:\n";
        if (checkConformation) {
            message += "  * conformant\n";
        }
        if (checkDeadlock) {
            message += "  * deadlock-free\n";
        }
        if (checkHazard) {
            message += "  * hazard-free\n";
        }
        return message;
    }

}
