package org.workcraft.plugins.circuit.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.FormulaUtils;
import org.workcraft.formula.bdd.BddManager;
import org.workcraft.formula.visitors.StringGenerator;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.plugins.circuit.tasks.CombinedCheckTask;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.utils.VerificationUtils;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.tasks.CombinedChainOutput;
import org.workcraft.plugins.mpsat.tasks.CombinedChainResultHandler;
import org.workcraft.plugins.mpsat.utils.MpsatUtils;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BinateImplementationVerificationCommand extends AbstractVerificationCommand {

    private class BinateData {
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
        return "Binate functions implementation [MPSat]";
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
        queueVerification(we);
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        CombinedChainResultHandler monitor = queueVerification(we);
        Result<? extends CombinedChainOutput> result = null;
        if (monitor != null) {
            result = monitor.waitResult();
        }
        return MpsatUtils.getCombinedChainOutcome(result);
    }

    private CombinedChainResultHandler queueVerification(WorkspaceEntry we) {
        if (!checkPrerequisites(we)) {
            return null;
        }
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        Collection<BinateData> binateItems = getBinateData(circuit);
        if (binateItems.isEmpty()) {
            DialogUtils.showInfo("Circuit does not implement binate functions.");
            return null;
        }

        List<VerificationParameters> settingsList = new ArrayList<>();
        LogUtils.logInfo("Verifying implementations of binate functions:");
        for (BinateData binateItem : binateItems) {
            String signal = CircuitUtils.getSignalReference(circuit, binateItem.contact);
            settingsList.add(VerificationParameters.getBinateImplementationReachSettings(signal, binateItem.formula, binateItem.variable));
            LogUtils.logMessage("  " + signal
                    + " = " + StringGenerator.toString(binateItem.formula)
                    + "  // (binate in " + binateItem.variable.getLabel() + "): ");
        }

        TaskManager manager = Framework.getInstance().getTaskManager();
        CombinedCheckTask task = new CombinedCheckTask(we, settingsList);
        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        CombinedChainResultHandler monitor = new CombinedChainResultHandler(task, null);
        manager.queue(task, description, monitor);
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

}
