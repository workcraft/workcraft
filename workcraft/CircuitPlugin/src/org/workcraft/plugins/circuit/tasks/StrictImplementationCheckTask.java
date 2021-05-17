package org.workcraft.plugins.circuit.tasks;

import org.workcraft.Framework;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.visitors.StringGenerator;
import org.workcraft.formula.visitors.StringGenerator.Style;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.plugins.mpsat_verification.presets.VerificationMode;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.tasks.MpsatOutput;
import org.workcraft.plugins.mpsat_verification.tasks.MpsatTask;
import org.workcraft.plugins.mpsat_verification.tasks.VerificationChainOutput;
import org.workcraft.plugins.mpsat_verification.utils.ReachUtils;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class StrictImplementationCheckTask implements Task<VerificationChainOutput> {

    // Reach expression for checking strict implementation
    private static final String STRICT_IMPLEMENTATION_SIGNAL_REPLACEMENT =
            "/* insert signal name here */";

    private static final String STRICT_IMPLEMENTATION_EXPR_REPLACEMENT =
            "/* insert complex gate expression here */";

    private static final String STRICT_IMPLEMENTATION_EXPR_SET_REPLACEMENT =
            "/* insert generalised C-element set function here */";

    private static final String STRICT_IMPLEMENTATION_EXPR_RESET_REPLACEMENT =
            "/* insert generalised C-element reset function here */";

    private static final String STRICT_IMPLEMENTATION_COMPLEX_GATE_REACH =
            "('S\"" + STRICT_IMPLEMENTATION_SIGNAL_REPLACEMENT + "\" ^ (" + STRICT_IMPLEMENTATION_EXPR_REPLACEMENT + "))";

    private static final String STRICT_IMPLEMENTATION_GENERALISED_CELEMENT_REACH =
            "let\n" +
            "    signal=S\"" + STRICT_IMPLEMENTATION_SIGNAL_REPLACEMENT + "\",\n" +
            "    setExpr=" + STRICT_IMPLEMENTATION_EXPR_SET_REPLACEMENT + ",\n" +
            "    resetExpr=" + STRICT_IMPLEMENTATION_EXPR_RESET_REPLACEMENT + " {\n" +
            "    (@signal & ~setExpr & ~$signal) | (setExpr & ~'signal) |\n" +
            "    (@signal & ~resetExpr & $signal) | (resetExpr & 'signal)\n" +
            "}\n";

    private final WorkspaceEntry we;

    private class SignalInfo {
        public final String name;
        public final String setExpr;
        public final String resetExpr;

        SignalInfo(String name, String setExpr, String resetExpr) {
            this.name = name;
            this.setExpr = setExpr;
            this.resetExpr = resetExpr;
        }
    }

    public StrictImplementationCheckTask(WorkspaceEntry we) {
        this.we = we;
    }

    @Override
    public Result<? extends VerificationChainOutput> run(ProgressMonitor<? super VerificationChainOutput> monitor) {
        Framework framework = Framework.getInstance();
        TaskManager taskManager = framework.getTaskManager();
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        String stgFileExtension = StgFormat.getInstance().getExtension();
        VerificationParameters preparationParameters = ReachUtils.getToolchainPreparationParameters();
        try {
            // Common variables
            Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
            File envFile = circuit.getEnvironmentFile();

            // Load environment STG
            Stg envStg = StgUtils.loadStg(envFile);
            // Make sure that input signals of the circuit are also inputs in the environment STG
            Collection<String> inputSignalNames = ReferenceHelper.getReferenceList(circuit, circuit.getInputPorts());
            Collection<String> outputSignalNames = ReferenceHelper.getReferenceList(circuit, circuit.getOutputPorts());
            StgUtils.restoreInterfaceSignals(envStg, inputSignalNames, outputSignalNames);

            // Write environment STG into a .g file
            File envStgFile = new File(directory, StgUtils.ENVIRONMENT_FILE_PREFIX + stgFileExtension);
            Result<? extends ExportOutput> envExportResult = StgUtils.exportStg(envStg, envStgFile, monitor);
            if (!envExportResult.isSuccess()) {
                if (envExportResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.failure(new VerificationChainOutput(
                        envExportResult, null, null, preparationParameters));
            }
            monitor.progressUpdate(0.20);

            // Check for strict implementation
            Collection<SignalInfo> signalInfos = new ArrayList<>();
            for (FunctionComponent component : circuit.getFunctionComponents()) {
                if (!component.getIsZeroDelay()) {
                    for (FunctionContact outputContact : component.getFunctionOutputs()) {
                        BooleanFormula setFormula = CircuitUtils.getDriverFormula(circuit, outputContact.getSetFunction());
                        String setExpr = StringGenerator.toString(setFormula, Style.REACH);
                        BooleanFormula resetFormula = CircuitUtils.getDriverFormula(circuit, outputContact.getResetFunction());
                        String resetExpr = StringGenerator.toString(resetFormula, Style.REACH);
                        String signalName = CircuitUtils.getSignalReference(circuit, outputContact);
                        signalInfos.add(new SignalInfo(signalName, setExpr, resetExpr));
                    }
                }
            }
            VerificationParameters verificationParameters = getVerificationParameters(signalInfos);
            MpsatTask mpsatTask = new MpsatTask(envStgFile, verificationParameters, directory);
            SubtaskMonitor<Object> mpsatMonitor = new SubtaskMonitor<>(monitor);
            Result<? extends MpsatOutput>  mpsatResult = taskManager.execute(
                    mpsatTask, "Running strict implementation check [MPSat]", mpsatMonitor);

            if (!mpsatResult.isSuccess()) {
                if (mpsatResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.failure(new VerificationChainOutput(
                        envExportResult, null, mpsatResult, verificationParameters));
            }
            monitor.progressUpdate(0.80);

            if (mpsatResult.getPayload().hasSolutions()) {
                return Result.success(new VerificationChainOutput(
                        envExportResult, null, mpsatResult, verificationParameters,
                        "Circuit does not strictly implement the environment after the following trace(s):"));
            }
            monitor.progressUpdate(1.00);

            // Success
            return Result.success(new VerificationChainOutput(
                    envExportResult, null, mpsatResult, verificationParameters,
                    "The circuit strictly implements its environment."));

        } catch (Throwable e) {
            return new Result<>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

    private VerificationParameters getVerificationParameters(Collection<SignalInfo> signalInfos) {
        String reach = "// Checks the STG is strictly implemented by a circuit.\n";
        boolean isFirstSignal = true;
        for (SignalInfo signalInfo: signalInfos) {
            boolean isComplexGate = (signalInfo.resetExpr == null) || signalInfo.resetExpr.isEmpty();
            String signalReach = isComplexGate ? STRICT_IMPLEMENTATION_COMPLEX_GATE_REACH : STRICT_IMPLEMENTATION_GENERALISED_CELEMENT_REACH;
            signalReach = signalReach.replace(STRICT_IMPLEMENTATION_SIGNAL_REPLACEMENT, signalInfo.name);
            if (isComplexGate) {
                signalReach = signalReach.replace(STRICT_IMPLEMENTATION_EXPR_REPLACEMENT, signalInfo.setExpr);
            } else {
                signalReach = signalReach.replace(STRICT_IMPLEMENTATION_EXPR_SET_REPLACEMENT, signalInfo.setExpr);
                signalReach = signalReach.replace(STRICT_IMPLEMENTATION_EXPR_RESET_REPLACEMENT, signalInfo.resetExpr);
            }
            if (!isFirstSignal) {
                reach += "\n|\n";
            }
            reach += signalReach;
            isFirstSignal = false;
        }
        return new VerificationParameters("Strict implementation",
                VerificationMode.STG_REACHABILITY, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                reach, true);
    }

}
