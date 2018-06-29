package org.workcraft.plugins.circuit.commands;

import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class CircuitVerificationUtils {

    public static Stg getEnvironmentStg(WorkspaceEntry we) {
        VisualCircuit visualCircuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
        File envFile = visualCircuit.getEnvironmentFile();
        return StgUtils.loadStg(envFile);
    }

    public static boolean checkCircuitHasComponents(WorkspaceEntry we) {
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        if (circuit.getFunctionComponents().isEmpty()) {
            DialogUtils.showError("The circuit must have components.");
            return false;
        }
        return true;
    }

    public static boolean checkCircuitHasValidEnvironment(WorkspaceEntry we) {
        VisualCircuit visualCircuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
        File envFile = visualCircuit.getEnvironmentFile();
        Stg envStg = StgUtils.loadStg(envFile);
        if (envStg == null) {
            String msg;
            if (envFile == null) {
                msg = "Environment STG is missing; the circuit will be verified without it.";
            } else {
                msg = "Cannot read an STG model from the file:\n" + envFile.getAbsolutePath()
                        + "\n\nThe circuit will be verified without environment STG.";
            }
            DialogUtils.showWarning(msg);
        }
        return true;
    }

    public static boolean checkInterfaceInitialState(WorkspaceEntry we) {
        VisualCircuit visualCircuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
        File envFile = visualCircuit.getEnvironmentFile();
        Stg envStg = StgUtils.loadStg(envFile);
        // Check initial state conformance of interface signals between the circuit and its environment STG (if present)
        if (envStg != null) {
            Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
            Set<String> conflictSignals = getConflictingInterfaceSignals(circuit, envStg);
            if (!conflictSignals.isEmpty()) {
                String msg = "The circuit and its environment have different initial state of interface signal";
                DialogUtils.showError(LogUtils.getTextWithRefs(msg, conflictSignals));
                return false;
            }
        }
        return true;
    }

    private static Set<String> getConflictingInterfaceSignals(Circuit circuit, Stg envStg) {
        Set<String> inconsistentSignals = new HashSet<>();
        HashMap<String, Boolean> envSignalStates = StgUtils.getInitialState(envStg, 2000);
        for (Contact port : circuit.getPorts()) {
            String portRef = circuit.getNodeReference(port);
            Boolean envSignalState = envSignalStates.get(portRef);
            if ((envSignalState != null) && (port.getInitToOne() != envSignalState)) {
                inconsistentSignals.add(portRef);
            }
        }
        return inconsistentSignals;
    }

}
