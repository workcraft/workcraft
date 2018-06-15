package org.workcraft.plugins.circuit.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitUtils;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.tasks.CheckStrictImplementationTask;
import org.workcraft.plugins.mpsat.tasks.MpsatChainOutput;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResultHandler;
import org.workcraft.plugins.mpsat.tasks.MpsatUtils;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.DialogUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

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
        VisualCircuit visualCircuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
        File envFile = visualCircuit.getEnvironmentFile();
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        if (check(circuit, envFile)) {
            Framework framework = Framework.getInstance();
            TaskManager manager = framework.getTaskManager();
            CheckStrictImplementationTask task = new CheckStrictImplementationTask(we);
            String description = MpsatUtils.getToolchainDescription(we.getTitle());
            monitor = new MpsatChainResultHandler(we);
            manager.queue(task, description, monitor);
        }
        return monitor;
    }

    private boolean check(Circuit circuit, File envFile) {
        // Check that circuit is not empty
        if (circuit.getFunctionComponents().isEmpty()) {
            DialogUtils.showError("The circuit must have components.");
            return false;
        }
        // Check that environment STG exists
        Stg envStg = StgUtils.loadStg(envFile);
        if (envStg == null) {
            String message = "Strict implementation cannot be checked without an environment STG.";
            if (envFile != null) {
                message += "\n\nCannot read STG model from the file:\n" + envFile.getAbsolutePath();
            }
            DialogUtils.showError(message);
            return false;
        }
        // Make sure that input signals of the circuit are also inputs in the environment STG
        ArrayList<String> circuitInputSignals = ReferenceHelper.getReferenceList(circuit, circuit.getInputPorts());
        ArrayList<String> circuitOutputSignals = ReferenceHelper.getReferenceList(circuit, circuit.getOutputPorts());
        StgUtils.restoreInterfaceSignals(envStg, circuitInputSignals, circuitOutputSignals);

        // Check that the set of circuit input signals is a subset of STG input signals.
        Set<String> stgInputSignals = envStg.getSignalNames(Signal.Type.INPUT, null);
        if (!stgInputSignals.containsAll(circuitInputSignals)) {
            Set<String> missingInputSignals = new HashSet<>(circuitInputSignals);
            missingInputSignals.removeAll(stgInputSignals);
            String message = "Strict implementation cannot be checked for a circuit whose\n"
                    + "input signals are not specified in its environment STG.";
            message += "\n\nThe following input signals are missing in the environemnt STG:\n"
                    + ReferenceHelper.getReferencesAsString(missingInputSignals, 50);
            DialogUtils.showError(message);
            return false;
        }

        // Check that the set of local signals is the same for the circuit and STG.
        Set<String> stgLocalSignals = new HashSet<>();
        stgLocalSignals.addAll(envStg.getSignalNames(Signal.Type.INTERNAL, null));
        stgLocalSignals.addAll(envStg.getSignalNames(Signal.Type.OUTPUT, null));
        Set<String> circuitLocalSignals = new HashSet<>();
        for (FunctionComponent component: circuit.getFunctionComponents()) {
            Collection<Contact> componentOutputs = component.getOutputs();
            for (Contact contact: componentOutputs) {
                String signalName = CircuitUtils.getSignalName(circuit, contact);
                circuitLocalSignals.add(signalName);
            }
        }
        if (!stgLocalSignals.equals(circuitLocalSignals)) {
            String message = "Strict implementation cannot be checked for a circuit whose\n"
                    + "non-input signals are different from those of its environment STG.";
            Set<String> missingCircuitSignals = new HashSet<>(circuitLocalSignals);
            missingCircuitSignals.removeAll(stgLocalSignals);
            if (!missingCircuitSignals.isEmpty()) {
                message += "\n\nNon-input signals missing in the circuit:\n"
                        + ReferenceHelper.getReferencesAsString(missingCircuitSignals, 50);
            }
            Set<String> missingStgSignals = new HashSet<>(stgLocalSignals);
            missingStgSignals.removeAll(circuitLocalSignals);
            if (!missingStgSignals.isEmpty()) {
                message += "\n\nNon-input signals missing in the environment STG:\n"
                        + ReferenceHelper.getReferencesAsString(missingStgSignals, 50);
            }
            DialogUtils.showError(message);
            return false;
        }
        return true;
    }

}
