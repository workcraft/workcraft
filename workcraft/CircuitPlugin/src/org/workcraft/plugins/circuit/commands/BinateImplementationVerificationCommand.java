package org.workcraft.plugins.circuit.commands;

import org.workcraft.Framework;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.formula.*;
import org.workcraft.formula.bdd.BddManager;
import org.workcraft.formula.visitors.StringGenerator;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.plugins.circuit.tasks.CombinedCheckTask;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.utils.VerificationUtils;
import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.plugins.mpsat_verification.presets.VerificationMode;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.tasks.CombinedChainResultHandlingMonitor;
import org.workcraft.plugins.mpsat_verification.utils.MpsatUtils;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BinateImplementationVerificationCommand extends org.workcraft.commands.AbstractVerificationCommand
        implements ScriptableCommand<Boolean> {

    private static class BinateData {
        public final FunctionContact contact;
        public final BooleanFormula formula;
        public final BooleanVariable variable;

        BinateData(FunctionContact contact, BooleanFormula formula, BooleanVariable variable) {
            this.contact = contact;
            this.formula = formula;
            this.variable = variable;
        }
    }

    @Override
    public String getDisplayName() {
        return "Binate consensus [MPSat]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Circuit.class);
    }

    @Override
    public Position getPosition() {
        return Position.MIDDLE;
    }

    @Override
    public void run(WorkspaceEntry we) {
        queueTask(we);
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        CombinedChainResultHandlingMonitor monitor = queueTask(we);
        monitor.setInteractive(false);
        return monitor.waitForHandledResult();
    }

    private CombinedChainResultHandlingMonitor queueTask(WorkspaceEntry we) {
        CombinedChainResultHandlingMonitor monitor = new CombinedChainResultHandlingMonitor(we);
        if (!checkPrerequisites(we)) {
            monitor.isFinished(Result.cancel());
        } else {
            Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
            Collection<BinateData> binateItems = getBinateData(circuit);
            List<VerificationParameters> verificationParametersList = new ArrayList<>();
            boolean isFirstItem = true;
            for (BinateData binateItem : binateItems) {
                if (isFirstItem) {
                    LogUtils.logInfo("Verifying binate consensus for functions:");
                }
                isFirstItem = false;
                String signal = CircuitUtils.getSignalReference(circuit, binateItem.contact);
                VerificationParameters verificationParameters = getBinateImplementationReachSettings(
                        signal, binateItem.formula, binateItem.variable);

                verificationParametersList.add(verificationParameters);
                String formulaStr = StringGenerator.toString(binateItem.formula);
                String variableLabel = binateItem.variable.getLabel();
                LogUtils.logMessage("  " + signal + " = " + formulaStr + "   [binate in " + variableLabel + "]");
            }

            TaskManager manager = Framework.getInstance().getTaskManager();
            CombinedCheckTask task = new CombinedCheckTask(we, verificationParametersList,
                    "Binate consensus vacuously holds");

            String description = MpsatUtils.getToolchainDescription(we.getTitle());
            manager.queue(task, description, monitor);
        }
        return monitor;
    }

    private boolean checkPrerequisites(WorkspaceEntry we) {
        return isApplicableTo(we)
                && VerificationUtils.checkCircuitHasComponents(we)
                && VerificationUtils.checkInterfaceInitialState(we)
                && VerificationUtils.checkInterfaceConstrains(we, true);
    }

    private Collection<BinateData> getBinateData(Circuit circuit) {
        List<BinateData> result = new ArrayList<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            for (FunctionContact outputContact : component.getFunctionOutputs()) {
                if (outputContact.isSequential()) continue;
                BooleanFormula formula = CircuitUtils.getDriverFormula(circuit, outputContact.getSetFunction());
                for (BooleanVariable variable : FormulaUtils.extractOrderedVariables(formula)) {
                    if (new BddManager().isBinate(formula, variable)) {
                        result.add(new BinateData(outputContact, formula, variable));
                    }
                }
            }
        }
        return result;
    }

    private VerificationParameters getBinateImplementationReachSettings(String signal, BooleanFormula formula,
            BooleanVariable variable) {

        BooleanFormula insensitivityFormula = new Iff(
                FormulaUtils.replaceOne(formula, variable),
                FormulaUtils.replaceZero(formula, variable));

        String varName = variable.getLabel();
        FreeVariable positiveVar = new FreeVariable("@" + varName);
        BooleanFormula splitVarFormula = FormulaUtils.replaceBinateVariable(formula, variable, positiveVar);
        BooleanFormula derivativeFormula = FormulaUtils.derive(splitVarFormula, positiveVar);

        String reach = "@S\"" + varName + "\" &\n"
                + "(" + StringGenerator.toString(insensitivityFormula, StringGenerator.Style.REACH) + ") &\n"
                + "(" + StringGenerator.toString(derivativeFormula, StringGenerator.Style.REACH) + ")";

        return new VerificationParameters("Binate consensus for " + signal + " in " + varName,
                VerificationMode.STG_REACHABILITY, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                reach, true);
    }

}
