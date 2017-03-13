package org.workcraft.plugins.circuit.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.workcraft.Framework;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.utils.FormulaToString;
import org.workcraft.formula.utils.FormulaToString.Style;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitUtils;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.stg.CircuitStgUtils;
import org.workcraft.plugins.circuit.stg.CircuitToStgConverter;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.MpsatResultParser;
import org.workcraft.plugins.mpsat.SignalInfo;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.plugins.mpsat.tasks.MpsatTask;
import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class CheckStrictImplementationTask extends MpsatChainTask {
    private final MpsatParameters toolchainPreparationSettings = MpsatParameters.getToolchainPreparationSettings();

    public CheckStrictImplementationTask(WorkspaceEntry we) {
        super(we, null);
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
            Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
            File envFile = visualCircuit.getEnvironmentFile();

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

            // Write environment STG into a .g file
            File envStgFile = new File(directory, StgUtils.ENVIRONMENT_FILE_NAME + StgUtils.ASTG_FILE_EXT);
            Result<? extends Object> envExportResult = CircuitStgUtils.exportStg(envStg, envStgFile, directory, monitor);
            if (envExportResult.getOutcome() != Outcome.FINISHED) {
                if (envExportResult.getOutcome() == Outcome.CANCELLED) {
                    return new Result<MpsatChainResult>(Outcome.CANCELLED);
                }
                return new Result<MpsatChainResult>(Outcome.FAILED,
                        new MpsatChainResult(envExportResult, null, null, null, toolchainPreparationSettings));
            }
            monitor.progressUpdate(0.20);

            // Generate unfolding
            Result<? extends ExternalProcessResult> punfResult = null;
            final TaskManager taskManager = framework.getTaskManager();
            File unfoldingFile = new File(directory, StgUtils.SYSTEM_FILE_NAME + PunfSettings.getUnfoldingExtension(true));
            PunfTask punfTask = new PunfTask(envStgFile.getAbsolutePath(), unfoldingFile.getAbsolutePath());
            SubtaskMonitor<Object> punfMonitor = new SubtaskMonitor<>(monitor);
            punfResult = taskManager.execute(punfTask, "Unfolding .g", punfMonitor);

            if (punfResult.getOutcome() != Outcome.FINISHED) {
                if (punfResult.getOutcome() == Outcome.CANCELLED) {
                    return new Result<MpsatChainResult>(Outcome.CANCELLED);
                }
                return new Result<MpsatChainResult>(Outcome.FAILED,
                        new MpsatChainResult(envExportResult, null, punfResult, null, toolchainPreparationSettings));
            }
            monitor.progressUpdate(0.40);

            // Check for strict implementation
            Collection<SignalInfo> signalInfos = new ArrayList<>();
            for (FunctionComponent component: circuit.getFunctionComponents()) {
                Collection<FunctionContact> componentOutputs = component.getFunctionContacts();
                for (FunctionContact contact: componentOutputs) {
                    String signalName = CircuitUtils.getSignalName(circuit, contact);
                    BooleanFormula setFunction = contact.getSetFunction();
                    String setFormula = FormulaToString.toString(setFunction, Style.VERILOG);
                    BooleanFormula resetFunction = contact.getResetFunction();
                    String resetFormula = FormulaToString.toString(resetFunction, Style.VERILOG);
                    SignalInfo signalInfo = new SignalInfo(signalName, setFormula, resetFormula);
                    signalInfos.add(signalInfo);
                }
            }
            MpsatParameters mpsatSettings = MpsatParameters.getStrictImplementationReachSettings(signalInfos);
            MpsatTask mpsatTask = new MpsatTask(mpsatSettings.getMpsatArguments(directory),
                    unfoldingFile, directory, true, envStgFile);
            SubtaskMonitor<Object> mpsatMonitor = new SubtaskMonitor<>(monitor);
            Result<? extends ExternalProcessResult>  mpsatResult = taskManager.execute(
                    mpsatTask, "Running strict implementation check [MPSat]", mpsatMonitor);

            if (mpsatResult.getOutcome() != Outcome.FINISHED) {
                if (mpsatResult.getOutcome() == Outcome.CANCELLED) {
                    return new Result<MpsatChainResult>(Outcome.CANCELLED);
                }
                return new Result<MpsatChainResult>(Outcome.FAILED,
                        new MpsatChainResult(envExportResult, null, punfResult, mpsatResult, mpsatSettings));
            }
            monitor.progressUpdate(0.90);

            MpsatResultParser mpsatConformationParser = new MpsatResultParser(mpsatResult.getReturnValue());
            if (!mpsatConformationParser.getSolutions().isEmpty()) {
                return new Result<MpsatChainResult>(Outcome.FINISHED,
                        new MpsatChainResult(envExportResult, null, punfResult, mpsatResult, mpsatSettings,
                                "Circuit does not strictly implement the environment after the following trace(s):"));
            }
            monitor.progressUpdate(1.00);

            // Success
            return new Result<MpsatChainResult>(Outcome.FINISHED,
                    new MpsatChainResult(envExportResult, null, punfResult, null, mpsatSettings,
                            "The circuit strictly implements its environment."));

        } catch (Throwable e) {
            return new Result<MpsatChainResult>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

}
