package org.workcraft.plugins.circuit.tasks;

import org.workcraft.Framework;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.utils.StringGenerator;
import org.workcraft.formula.utils.StringGenerator.Style;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitSignalInfo;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.tasks.*;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class StrictImplementationCheckTask extends VerificationChainTask {

    public StrictImplementationCheckTask(WorkspaceEntry we) {
        super(we, null);
    }

    @Override
    public Result<? extends VerificationChainOutput> run(ProgressMonitor<? super VerificationChainOutput> monitor) {
        Framework framework = Framework.getInstance();
        WorkspaceEntry we = getWorkspaceEntry();
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        String stgFileExtension = StgFormat.getInstance().getExtension();
        VerificationParameters preparationSettings = VerificationParameters.getToolchainPreparationSettings();
        try {
            // Common variables
            Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
            File envFile = circuit.getEnvironmentFile();

            // Load environment STG
            Stg envStg = StgUtils.loadStg(envFile);
            // Make sure that input signals of the circuit are also inputs in the environment STG
            ArrayList<String> inputSignalNames = ReferenceHelper.getReferenceList(circuit, circuit.getInputPorts());
            ArrayList<String> outputSignalNames = ReferenceHelper.getReferenceList(circuit, circuit.getOutputPorts());
            StgUtils.restoreInterfaceSignals(envStg, inputSignalNames, outputSignalNames);

            // Write environment STG into a .g file
            File envStgFile = new File(directory, StgUtils.ENVIRONMENT_FILE_PREFIX + stgFileExtension);
            Result<? extends ExportOutput> envExportResult = StgUtils.exportStg(envStg, envStgFile, monitor);
            if (envExportResult.getOutcome() != Outcome.SUCCESS) {
                if (envExportResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new VerificationChainOutput(envExportResult, null, null, null, preparationSettings));
            }
            monitor.progressUpdate(0.20);

            // Generate unfolding
            Result<? extends PunfOutput> punfResult = null;
            final TaskManager taskManager = framework.getTaskManager();
            File unfoldingFile = new File(directory, StgUtils.ENVIRONMENT_FILE_PREFIX + PunfTask.PNML_FILE_EXTENSION);
            PunfTask punfTask = new PunfTask(envStgFile, unfoldingFile, directory);
            SubtaskMonitor<Object> punfMonitor = new SubtaskMonitor<>(monitor);
            punfResult = taskManager.execute(punfTask, "Unfolding .g", punfMonitor);

            if (punfResult.getOutcome() != Outcome.SUCCESS) {
                if (punfResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new VerificationChainOutput(envExportResult, null, punfResult, null, preparationSettings));
            }
            monitor.progressUpdate(0.50);

            // Check for strict implementation
            CircuitSignalInfo circuitInfo = new CircuitSignalInfo(circuit, "$S\"", "\"");
            Collection<VerificationParameters.SignalInfo> signalInfos = new ArrayList<>();
            for (FunctionComponent component: circuit.getFunctionComponents()) {
                for (CircuitSignalInfo.SignalInfo signalInfo: circuitInfo.getComponentSignalInfos(component)) {
                    String signalName = circuitInfo.getContactSignal(signalInfo.contact);
                    BooleanFormula setFormula = signalInfo.setFormula;
                    String setExpr = StringGenerator.toString(setFormula, Style.VERILOG);
                    BooleanFormula resetFormula = signalInfo.resetFormula;
                    String resetExpr = StringGenerator.toString(resetFormula, Style.VERILOG);
                    signalInfos.add(new VerificationParameters.SignalInfo(signalName, setExpr, resetExpr));
                }
            }
            VerificationParameters mpsatSettings = VerificationParameters.getStrictImplementationReachSettings(signalInfos);
            VerificationTask verificationTask = new VerificationTask(mpsatSettings.getMpsatArguments(directory),
                    unfoldingFile, directory, envStgFile);
            SubtaskMonitor<Object> mpsatMonitor = new SubtaskMonitor<>(monitor);
            Result<? extends VerificationOutput>  mpsatResult = taskManager.execute(
                    verificationTask, "Running strict implementation check [MPSat]", mpsatMonitor);

            if (mpsatResult.getOutcome() != Outcome.SUCCESS) {
                if (mpsatResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new VerificationChainOutput(envExportResult, null, punfResult, mpsatResult, mpsatSettings));
            }
            monitor.progressUpdate(0.80);

            VerificationOutputParser mpsatParser = new VerificationOutputParser(mpsatResult.getPayload());
            if (!mpsatParser.getSolutions().isEmpty()) {
                return new Result<>(Outcome.SUCCESS,
                        new VerificationChainOutput(envExportResult, null, punfResult, mpsatResult, mpsatSettings,
                                "Circuit does not strictly implement the environment after the following trace(s):"));
            }
            monitor.progressUpdate(1.00);

            // Success
            return new Result<>(Outcome.SUCCESS,
                    new VerificationChainOutput(envExportResult, null, punfResult, mpsatResult, mpsatSettings,
                            "The circuit strictly implements its environment."));

        } catch (Throwable e) {
            return new Result<>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

}
