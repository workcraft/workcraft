package org.workcraft.plugins.circuit.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.Framework;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.utils.FormulaToString;
import org.workcraft.formula.utils.FormulaToString.Style;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitSignalInfo;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.stg.CircuitStgUtils;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.MpsatResultParser;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.plugins.mpsat.tasks.MpsatTask;
import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
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

            // Load environment STG
            Stg envStg = StgUtils.loadStg(envFile);
            // Make sure that input signals of the circuit are also inputs in the environment STG
            ArrayList<String> inputSignalNames = ReferenceHelper.getReferenceList(circuit, (Collection) circuit.getInputPorts());
            ArrayList<String> outputSignalNames = ReferenceHelper.getReferenceList(circuit, (Collection) circuit.getOutputPorts());
            StgUtils.restoreInterfaceSignals(envStg, inputSignalNames, outputSignalNames);

            // Write environment STG into a .g file
            File envStgFile = new File(directory, StgUtils.ENVIRONMENT_FILE_NAME + StgUtils.ASTG_FILE_EXT);
            Result<? extends Object> envExportResult = CircuitStgUtils.exportStg(envStg, envStgFile, directory, monitor);
            if (envExportResult.getOutcome() != Outcome.SUCCESS) {
                if (envExportResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<MpsatChainResult>(Outcome.CANCEL);
                }
                return new Result<MpsatChainResult>(Outcome.FAILURE,
                        new MpsatChainResult(envExportResult, null, null, null, toolchainPreparationSettings));
            }
            monitor.progressUpdate(0.20);

            // Generate unfolding
            Result<? extends ExternalProcessResult> punfResult = null;
            final TaskManager taskManager = framework.getTaskManager();
            File unfoldingFile = new File(directory, StgUtils.ENVIRONMENT_FILE_NAME + PunfSettings.getUnfoldingExtension(true));
            PunfTask punfTask = new PunfTask(envStgFile.getAbsolutePath(), unfoldingFile.getAbsolutePath());
            SubtaskMonitor<Object> punfMonitor = new SubtaskMonitor<>(monitor);
            punfResult = taskManager.execute(punfTask, "Unfolding .g", punfMonitor);

            if (punfResult.getOutcome() != Outcome.SUCCESS) {
                if (punfResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<MpsatChainResult>(Outcome.CANCEL);
                }
                return new Result<MpsatChainResult>(Outcome.FAILURE,
                        new MpsatChainResult(envExportResult, null, punfResult, null, toolchainPreparationSettings));
            }
            monitor.progressUpdate(0.50);

            // Check for strict implementation
            CircuitSignalInfo circuitInfo = new CircuitSignalInfo(circuit, "$S\"", "\"");
            Collection<MpsatParameters.SignalInfo> signalInfos = new ArrayList<>();
            for (FunctionComponent component: circuit.getFunctionComponents()) {
                for (CircuitSignalInfo.SignalInfo signalInfo: circuitInfo.getComponentSignalInfos(component)) {
                    String signalName = circuitInfo.getContactSignal(signalInfo.contact);
                    BooleanFormula setFormula = signalInfo.setFormula;
                    String setExpr = FormulaToString.toString(setFormula, Style.VERILOG);
                    BooleanFormula resetFormula = signalInfo.resetFormula;
                    String resetExpr = FormulaToString.toString(resetFormula, Style.VERILOG);
                    signalInfos.add(new MpsatParameters.SignalInfo(signalName, setExpr, resetExpr));
                }
            }
            MpsatParameters mpsatSettings = MpsatParameters.getStrictImplementationReachSettings(signalInfos);
            MpsatTask mpsatTask = new MpsatTask(mpsatSettings.getMpsatArguments(directory),
                    unfoldingFile, directory, true, envStgFile);
            SubtaskMonitor<Object> mpsatMonitor = new SubtaskMonitor<>(monitor);
            Result<? extends ExternalProcessResult>  mpsatResult = taskManager.execute(
                    mpsatTask, "Running strict implementation check [MPSat]", mpsatMonitor);

            if (mpsatResult.getOutcome() != Outcome.SUCCESS) {
                if (mpsatResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<MpsatChainResult>(Outcome.CANCEL);
                }
                return new Result<MpsatChainResult>(Outcome.FAILURE,
                        new MpsatChainResult(envExportResult, null, punfResult, mpsatResult, mpsatSettings));
            }
            monitor.progressUpdate(0.80);

            MpsatResultParser mpsatParser = new MpsatResultParser(mpsatResult.getReturnValue());
            if (!mpsatParser.getSolutions().isEmpty()) {
                return new Result<MpsatChainResult>(Outcome.SUCCESS,
                        new MpsatChainResult(envExportResult, null, punfResult, mpsatResult, mpsatSettings,
                                "Circuit does not strictly implement the environment after the following trace(s):"));
            }
            monitor.progressUpdate(1.00);

            // Success
            return new Result<MpsatChainResult>(Outcome.SUCCESS,
                    new MpsatChainResult(envExportResult, null, punfResult, mpsatResult, mpsatSettings,
                            "The circuit strictly implements its environment."));

        } catch (Throwable e) {
            return new Result<MpsatChainResult>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

}
