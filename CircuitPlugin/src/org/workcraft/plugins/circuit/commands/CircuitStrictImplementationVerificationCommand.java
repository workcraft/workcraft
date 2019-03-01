package org.workcraft.plugins.circuit.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.tasks.CircuitStrictImplementationCheckTask;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.utils.EnvironmentUtils;
import org.workcraft.plugins.circuit.utils.VerificationUtils;
import org.workcraft.plugins.mpsat.tasks.MpsatChainOutput;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResultHandler;
import org.workcraft.plugins.mpsat.tasks.MpsatUtils;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.DialogUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CircuitStrictImplementationVerificationCommand extends AbstractVerificationCommand {

    @Override
    public String getDisplayName() {
        return "Strict implementation [MPSat]";
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
        MpsatChainResultHandler monitor = queueVerification(we);
        Result<? extends MpsatChainOutput> result = null;
        if (monitor != null) {
            result = monitor.waitResult();
        }
        return MpsatUtils.getChainOutcome(result);
    }

    private MpsatChainResultHandler queueVerification(WorkspaceEntry we) {
        MpsatChainResultHandler monitor = null;
        if (checkPrerequisites(we)) {
            Framework framework = Framework.getInstance();
            TaskManager manager = framework.getTaskManager();
            CircuitStrictImplementationCheckTask task = new CircuitStrictImplementationCheckTask(we);
            String description = MpsatUtils.getToolchainDescription(we.getTitle());
            monitor = new MpsatChainResultHandler(we);
            manager.queue(task, description, monitor);
        }
        return monitor;
    }

    private boolean checkPrerequisites(WorkspaceEntry we) {
        return isApplicableTo(we)
            && VerificationUtils.checkCircuitHasComponents(we)
            && VerificationUtils.checkInterfaceInitialState(we)
            && checkCircuitHasEnvironmentStrict(we)
            && checkCircuitInterfaceSignals(we)
            && checkCircuitLocalSignals(we);
    }

    private boolean checkCircuitHasEnvironmentStrict(WorkspaceEntry we) {
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        File envFile = EnvironmentUtils.getEnvironmentFile(circuit);
        Stg envStg = StgUtils.loadStg(envFile);
        if (envStg == null) {
            String msg = "Strict implementation cannot be checked without an environment STG.";
            if (envFile != null) {
                msg += "\n\nCannot read an STG model from the file:\n" + envFile.getAbsolutePath();
            }
            DialogUtils.showError(msg);
            return false;
        }
        return true;
    }

    private boolean checkCircuitInterfaceSignals(WorkspaceEntry we) {
        Stg envStg = VerificationUtils.getEnvironmentStg(we);
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);

        // Make sure that input signals of the circuit are also inputs in the environment STG
        ArrayList<String> circuitInputSignals = ReferenceHelper.getReferenceList(circuit, circuit.getInputPorts());
        ArrayList<String> circuitOutputSignals = ReferenceHelper.getReferenceList(circuit, circuit.getOutputPorts());
        StgUtils.restoreInterfaceSignals(envStg, circuitInputSignals, circuitOutputSignals);

        // Check that the set of circuit input signals is a subset of STG input signals.
        Set<String> stgInputs = envStg.getSignalReferences(Signal.Type.INPUT);
        if (!stgInputs.containsAll(circuitInputSignals)) {
            Set<String> missingInputSignals = new HashSet<>(circuitInputSignals);
            missingInputSignals.removeAll(stgInputs);
            String msg = "Strict implementation cannot be checked for a circuit whose\n"
                    + "input signals are not specified in its environment STG.";
            msg += "\n\nThe following input signals are missing in the environemnt STG:\n"
                    + ReferenceHelper.getReferencesAsString(missingInputSignals, 50);
            DialogUtils.showError(msg);
            return false;
        }
        return true;
    }

    private boolean checkCircuitLocalSignals(WorkspaceEntry we) {
        Stg envStg = VerificationUtils.getEnvironmentStg(we);
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);

        // Check that the set of local signals is the same for the circuit and STG.
        Set<String> stgLocalSignals = new HashSet<>();
        stgLocalSignals.addAll(envStg.getSignalNames(Signal.Type.INTERNAL, null));
        stgLocalSignals.addAll(envStg.getSignalNames(Signal.Type.OUTPUT, null));
        Set<String> circuitLocalSignals = new HashSet<>();
        for (FunctionComponent component: circuit.getFunctionComponents()) {
            Collection<Contact> componentOutputs = component.getOutputs();
            for (Contact contact: componentOutputs) {
                String signalName = CircuitUtils.getSignalReference(circuit, contact);
                circuitLocalSignals.add(signalName);
            }
        }
        if (!stgLocalSignals.equals(circuitLocalSignals)) {
            String msg = "Strict implementation cannot be checked for a circuit whose\n"
                    + "non-input signals are different from those of its environment STG.";
            Set<String> missingCircuitSignals = new HashSet<>(circuitLocalSignals);
            missingCircuitSignals.removeAll(stgLocalSignals);
            if (!missingCircuitSignals.isEmpty()) {
                msg += "\n\nNon-input signals missing in the circuit:\n"
                        + ReferenceHelper.getReferencesAsString(missingCircuitSignals, 50);
            }
            Set<String> missingStgSignals = new HashSet<>(stgLocalSignals);
            missingStgSignals.removeAll(circuitLocalSignals);
            if (!missingStgSignals.isEmpty()) {
                msg += "\n\nNon-input signals missing in the environment STG:\n"
                        + ReferenceHelper.getReferencesAsString(missingStgSignals, 50);
            }
            DialogUtils.showError(msg);
            return false;
        }
        return true;
    }

}
