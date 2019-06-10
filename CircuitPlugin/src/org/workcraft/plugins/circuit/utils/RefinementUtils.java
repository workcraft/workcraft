package org.workcraft.plugins.circuit.utils;

import org.workcraft.Framework;
import org.workcraft.dom.math.MathModel;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitComponent;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.types.Pair;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.ModelEntry;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class RefinementUtils {

    public static Pair<File, Circuit> getRefinementCircuit(CircuitComponent component) {
        if (component.hasRefinement()) {
            return getRefinementCircuit(component.getRefinement().getFile());
        }
        return null;
    }

    public static Pair<File, Circuit> getRefinementCircuit(File file) {
        Framework framework = Framework.getInstance();
        try {
            ModelEntry me = framework.loadModel(file);
            MathModel model = me.getMathModel();
            if (model instanceof Circuit) {
                return Pair.of(file, (Circuit) model);
            }
            if (model instanceof Stg) {
                Stg stg = (Stg) model;
                if (stg.hasRefinement()) {
                    return getRefinementCircuit(stg.getRefinement().getFile());
                }
            }
        } catch (DeserialisationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean checkRefinementCircuits(Circuit circuit) {
        Set<String> refs = new HashSet<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            if (!checkRefinementCircuit(circuit, component, false)) {
                String ref = circuit.getNodeReference(component);
                refs.add(ref);
            }
        }
        if (refs.isEmpty()) {
            return true;
        } else {
            String msg = LogUtils.getTextWithRefs("Problematic refinement for component", refs);
            DialogUtils.showError(msg);
            return false;
        }
    }

    public static boolean checkRefinementCircuit(Circuit circuit, FunctionComponent component, boolean showDialog) {
        Pair<File, Circuit> refinement = getRefinementCircuit(component);
        String msg = "";
        if (refinement != null) {
            if (!checkRefinementCircuitTitle(component, refinement.getSecond())) {
                msg += "\n  * module name does not match the refinement title";
            }
            if (!checkRefinementCircuitPorts(circuit, component, refinement.getSecond())) {
                msg += "\n  * component pins do not match the refinement circuit ports";
            }
        }
        if (msg.isEmpty()) {
            return true;
        } else {
            if (showDialog) {
                String ref = circuit.getNodeReference(component);
                DialogUtils.showError("Refinement circuit issues for component '" + ref + "':" + msg);
            }
            return false;
        }
    }

    public static boolean checkRefinementCircuitTitle(FunctionComponent component, Circuit refinementCircuit) {
        return component.getModule().equals(refinementCircuit.getTitle());
    }

    public static boolean checkRefinementCircuitPorts(Circuit circuit, FunctionComponent component, Circuit refinementCircuit) {
        return CircuitUtils.getInputPortNames(refinementCircuit).equals(CircuitUtils.getInputPinNames(circuit, component))
                && CircuitUtils.getOutputPortNames(refinementCircuit).equals(CircuitUtils.getOutputPinNames(circuit, component));
    }

}
