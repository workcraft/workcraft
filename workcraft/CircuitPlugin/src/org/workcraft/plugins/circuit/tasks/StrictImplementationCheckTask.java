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
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.punf.tasks.PunfTask;
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
    private static final String REACH_STRICT_IMPLEMENTATION_SIGNAL =
            "/* insert signal name here */";

    private static final String REACH_STRICT_IMPLEMENTATION_EXPR =
            "/* insert complex gate expression here */";

    private static final String REACH_STRICT_IMPLEMENTATION_EXPR_SET =
            "/* insert generalised C-element set function here */";

    private static final String REACH_STRICT_IMPLEMENTATION_EXPR_RESET =
            "/* insert generalised C-element reset function here */";

    private static final String REACH_STRICT_IMPLEMENTATION_COMPLEX_GATE =
            "('S\"" + REACH_STRICT_IMPLEMENTATION_SIGNAL + "\" ^ (" + REACH_STRICT_IMPLEMENTATION_EXPR + "))";

    private static final String REACH_STRICT_IMPLEMENTATION_GENERALISED_CELEMENT =
            "let\n" +
            "    signal=S\"" + REACH_STRICT_IMPLEMENTATION_SIGNAL + "\",\n" +
            "    setExpr=" + REACH_STRICT_IMPLEMENTATION_EXPR_SET + ",\n" +
            "    resetExpr=" + REACH_STRICT_IMPLEMENTATION_EXPR_RESET + " {\n" +
            "    (@signal & ~setExpr & ~$signal) | (setExpr & ~'signal) |\n" +
            "    (@signal & ~resetExpr & $signal) | (resetExpr & 'signal)\n" +
            "}\n";

    private static final String REACH_STRICT_IMPLEMENTATION =
            "// Checks the STG is strictly implemented by a circuit.\n";

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
            ArrayList<String> inputSignalNames = ReferenceHelper.getReferenceList(circuit, circuit.getInputPorts());
            ArrayList<String> outputSignalNames = ReferenceHelper.getReferenceList(circuit, circuit.getOutputPorts());
            StgUtils.restoreInterfaceSignals(envStg, inputSignalNames, outputSignalNames);

            // Write environment STG into a .g file
            File envStgFile = new File(directory, StgUtils.ENVIRONMENT_FILE_PREFIX + stgFileExtension);
            Result<? extends ExportOutput> envExportResult = StgUtils.exportStg(envStg, envStgFile, monitor);
            if (!envExportResult.isSuccess()) {
                if (envExportResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.failure(new VerificationChainOutput(
                        envExportResult, null, null, null, preparationParameters));
            }
            monitor.progressUpdate(0.20);

            // Generate unfolding
            Result<? extends PunfOutput> punfResult = null;
            final TaskManager taskManager = framework.getTaskManager();
            File unfoldingFile = new File(directory, StgUtils.ENVIRONMENT_FILE_PREFIX + PunfTask.PNML_FILE_EXTENSION);
            PunfTask punfTask = new PunfTask(envStgFile, unfoldingFile, directory);
            SubtaskMonitor<Object> punfMonitor = new SubtaskMonitor<>(monitor);
            punfResult = taskManager.execute(punfTask, "Unfolding .g", punfMonitor);

            if (!punfResult.isSuccess()) {
                if (punfResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.failure(new VerificationChainOutput(
                        envExportResult, null, punfResult, null, preparationParameters));
            }
            monitor.progressUpdate(0.50);

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
            MpsatTask mpsatTask = new MpsatTask(unfoldingFile, envStgFile, verificationParameters, directory);
            SubtaskMonitor<Object> mpsatMonitor = new SubtaskMonitor<>(monitor);
            Result<? extends MpsatOutput>  mpsatResult = taskManager.execute(
                    mpsatTask, "Running strict implementation check [MPSat]", mpsatMonitor);

            if (!mpsatResult.isSuccess()) {
                if (mpsatResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.failure(new VerificationChainOutput(
                        envExportResult, null, punfResult, mpsatResult, verificationParameters));
            }
            monitor.progressUpdate(0.80);

            if (mpsatResult.getPayload().hasSolutions()) {
                return Result.success(new VerificationChainOutput(
                        envExportResult, null, punfResult, mpsatResult, verificationParameters,
                        "Circuit does not strictly implement the environment after the following trace(s):"));
            }
            monitor.progressUpdate(1.00);

            // Success
            return Result.success(new VerificationChainOutput(
                    envExportResult, null, punfResult, mpsatResult, verificationParameters,
                    "The circuit strictly implements its environment."));

        } catch (Throwable e) {
            return new Result<>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

    private VerificationParameters getVerificationParameters(Collection<SignalInfo> signalInfos) {
        String reachStrictImplementation = REACH_STRICT_IMPLEMENTATION;
        boolean isFirstSignal = true;
        for (SignalInfo signalInfo: signalInfos) {
            boolean isComplexGate = (signalInfo.resetExpr == null) || signalInfo.resetExpr.isEmpty();
            String s = isComplexGate ? REACH_STRICT_IMPLEMENTATION_COMPLEX_GATE : REACH_STRICT_IMPLEMENTATION_GENERALISED_CELEMENT;
            s = s.replace(REACH_STRICT_IMPLEMENTATION_SIGNAL, signalInfo.name);
            if (isComplexGate) {
                s = s.replace(REACH_STRICT_IMPLEMENTATION_EXPR, signalInfo.setExpr);
            } else {
                s = s.replace(REACH_STRICT_IMPLEMENTATION_EXPR_SET, signalInfo.setExpr);
                s = s.replace(REACH_STRICT_IMPLEMENTATION_EXPR_RESET, signalInfo.resetExpr);
            }
            if (!isFirstSignal) {
                reachStrictImplementation += "\n|\n";
            }
            reachStrictImplementation += s;
            isFirstSignal = false;
        }
        return new VerificationParameters("Strict implementation",
                VerificationMode.STG_REACHABILITY, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                reachStrictImplementation, true);
    }

}
