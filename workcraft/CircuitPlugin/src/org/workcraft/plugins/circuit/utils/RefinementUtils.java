package org.workcraft.plugins.circuit.utils;

import org.workcraft.Framework;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.references.FileReference;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.refinement.ComponentInterface;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.types.Pair;
import org.workcraft.utils.*;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public final class RefinementUtils {

    private RefinementUtils() {
    }

    public static boolean hasRefinementModel(VisualFunctionComponent component) {
        return (component != null) && hasRefinementModel(component.getReferencedComponent());
    }

    private static boolean hasRefinementModel(FunctionComponent component) {
        return (component != null) && component.hasRefinement();
    }

    public static boolean openRefinementModel(VisualCircuitComponent component) {
        return (component == null) ? false : openRefinementModel(component.getReferencedComponent());
    }

    public static boolean openRefinementModel(CircuitComponent component) {
        if (component != null) {
            FileReference refinement = component.getRefinement();
            if (refinement != null) {
                return openRefinementFile(refinement.getFile());
            }
        }
        return false;
    }

    public static boolean hasRefinementStg(VisualFunctionComponent component) {
        return (component != null) && hasRefinementStg(component.getReferencedComponent());
    }

    private static boolean hasRefinementStg(FunctionComponent component) {
        return getRefinementStg(component) != null;
    }

    public static boolean openRefinementStg(VisualCircuitComponent component) {
        return (component == null) ? false : openRefinementStg(component.getReferencedComponent());
    }

    public static boolean hasRefinementCircuit(VisualFunctionComponent component) {
        return (component != null) && hasRefinementCircuit(component.getReferencedComponent());
    }

    private static boolean hasRefinementCircuit(FunctionComponent component) {
        return getRefinementCircuit(component) != null;
    }

    public static boolean openRefinementCircuit(VisualCircuitComponent component) {
        return (component == null) ? false : openRefinementCircuit(component.getReferencedComponent());
    }

    public static boolean openRefinementStg(CircuitComponent component) {
        if (component != null) {
            Pair<File, Stg> refinementStg = getRefinementStg(component);
            if (refinementStg != null) {
                return openRefinementFile(refinementStg.getFirst());
            }
        }
        return false;
    }

    public static boolean openRefinementCircuit(CircuitComponent component) {
        if (component != null) {
            Pair<File, Circuit> refinementCircuit = getRefinementCircuit(component);
            if (refinementCircuit != null) {
                return openRefinementFile(refinementCircuit.getFirst());
            }
        }
        return false;
    }

    public static boolean openRefinementFile(File file) {
        if (file != null) {
            MainWindow mainWindow = Framework.getInstance().getMainWindow();
            WorkspaceEntry we = mainWindow.openWork(file);
            if (we != null) {
                mainWindow.requestFocus(we);
                return true;
            }
        }
        return false;
    }

    public static Pair<File, Stg> getRefinementStg(CircuitComponent component) {
        if (component.hasRefinement()) {
            return getRefinementStg(component.getRefinement().getFile());
        }
        return null;
    }

    public static Pair<File, Stg> getRefinementStg(File file) {
        try {
            ModelEntry me = WorkUtils.loadModel(file);
            MathModel model = me.getMathModel();
            if (model instanceof Stg) {
                return Pair.of(file, (Stg) model);
            }
        } catch (DeserialisationException e) {
            String filePath = FileUtils.getFullPath(file);
            LogUtils.logError("Cannot read model from file '" + filePath + "':\n" + e.getMessage());
        }
        return null;
    }

    public static Pair<File, Circuit> getRefinementCircuit(CircuitComponent component) {
        if (component.hasRefinement()) {
            return getRefinementCircuit(component.getRefinement().getFile());
        }
        return null;
    }

    public static Pair<File, Circuit> getRefinementCircuit(File file) {
        try {
            ModelEntry me = WorkUtils.loadModel(file);
            if (me != null) {
                MathModel model = me.getMathModel();
                if (model instanceof Circuit) {
                    return Pair.of(file, (Circuit) model);
                }
                if (model instanceof Stg) {
                    Stg stg = (Stg) model;
                    return getRefinementCircuit(stg.getRefinementFile());
                }
            }
        } catch (DeserialisationException e) {
            String filePath = FileUtils.getFullPath(file);
            LogUtils.logError("Cannot read model from file '" + filePath + "':\n" + e.getMessage());
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
            String msg = TextUtils.wrapMessageWithItems("Incompatible refinement interface for component", refs);
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
            if (!checkRefinementCircuitPorts(component, refinement.getSecond())) {
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

    public static boolean checkRefinementCircuitPorts(CircuitComponent component, Circuit refinementCircuit) {
        return CircuitUtils.getInputPortNames(refinementCircuit).equals(CircuitUtils.getInputPinNames(component))
                && CircuitUtils.getOutputPortNames(refinementCircuit).equals(CircuitUtils.getOutputPinNames(component));
    }

    public static ComponentInterface getComponentInterface(CircuitComponent component) {
        return component == null ? null : new ComponentInterface(component.getModule(),
                CircuitUtils.getInputPinNames(component), CircuitUtils.getOutputPinNames(component));
    }

    public static ComponentInterface getModelInterface(MathModel model) {
        return new ComponentInterface(model.getTitle(), getInputSignals(model), getOutputSignals(model));
    }

    private static Set<String> getInputSignals(MathModel model) {
        if (model instanceof Circuit) {
            return CircuitUtils.getInputPortNames((Circuit) model);
        }
        if (model instanceof Stg) {
            return ((Stg) model).getSignalReferences(Signal.Type.INPUT);
        }
        return null;
    }

    private static Set<String> getOutputSignals(MathModel model) {
        if (model instanceof Circuit) {
            return CircuitUtils.getOutputPortNames((Circuit) model);
        }
        if (model instanceof Stg) {
            return ((Stg) model).getSignalReferences(Signal.Type.OUTPUT);
        }
        return null;
    }

    public static boolean isCompatible(ComponentInterface ci1, ComponentInterface ci2) {
        return (ci1 != null) && (ci2 != null)
                && isCompatibleName(ci1.getName(), ci2.getName())
                && isCompatibleSignals(ci1.getInputs(), ci2.getInputs())
                && isCompatibleSignals(ci1.getOutputs(), ci2.getOutputs());
    }

    public static boolean isCompatibleName(String aName, String bName) {
        return (aName != null) && aName.equals(bName);
    }

    public static boolean isCompatibleSignals(Set<String> aSignals, Set<String> bSignals) {
        return (aSignals != null)  && aSignals.equals(bSignals);
    }

}
