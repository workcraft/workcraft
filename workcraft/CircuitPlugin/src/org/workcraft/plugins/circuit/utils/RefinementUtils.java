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
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.WorkUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public final class RefinementUtils {

    private RefinementUtils() {
    }

    public static void openRefinementModel(VisualCircuitComponent component) {
        if (component != null) {
            FileReference refinement = component.getReferencedComponent().getRefinement();
            if (refinement != null) {
                openRefinementFile(refinement.getFile());
            }
        }
    }

    public static boolean hasRefinementStg(VisualFunctionComponent component) {
        return (component != null) && (getRefinementStg(component.getReferencedComponent()) != null);
    }

    public static void openRefinementStg(VisualCircuitComponent component) {
        if (component != null) {
            Pair<File, Stg> refinementStg = getRefinementStg(component.getReferencedComponent());
            if (refinementStg != null) {
                openRefinementFile(refinementStg.getFirst());
            }
        }
    }

    public static boolean hasRefinementCircuit(VisualFunctionComponent component) {
        return (component != null) && (getRefinementCircuit(component.getReferencedComponent()) != null);
    }

    public static void openRefinementCircuit(VisualCircuitComponent component) {
        if (component != null) {
            Pair<File, Circuit> refinementCircuit = getRefinementCircuit(component.getReferencedComponent());
            if (refinementCircuit != null) {
                openRefinementFile(refinementCircuit.getFirst());
            }
        }
    }

    private static void openRefinementFile(File file) {
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        if ((file != null) && (mainWindow != null)) {
            WorkspaceEntry we = mainWindow.openWork(file);
            if (we != null) {
                mainWindow.requestFocus(we);
            }
        }
    }

    public static Pair<File, Stg> getRefinementStg(CircuitComponent component) {
        File file = component.getRefinementFile();
        return file == null ? null : getRefinementStg(file);
    }

    private static Pair<File, Stg> getRefinementStg(File file) {
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
        File file = component.getRefinementFile();
        return file == null ? null : getRefinementCircuit(file);
    }

    private static Pair<File, Circuit> getRefinementCircuit(File file) {
        if (file == null) {
            return null;
        }
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

    public static Set<FunctionComponent> getIncompatibleRefinementCircuitComponents(Circuit circuit) {
        Set<FunctionComponent> result = new HashSet<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            Pair<File, Circuit> refinementCircuit = getRefinementCircuit(component);
            if (refinementCircuit != null) {
                ComponentInterface refinementCircuitInterface = getModelInterface(refinementCircuit.getSecond());
                ComponentInterface componentInterface = getComponentInterface(component);
                if (!isCompatible(componentInterface, refinementCircuitInterface)) {
                    result.add(component);
                }
            }
        }
        return result;
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

    private static boolean isCompatibleName(String aName, String bName) {
        return (aName != null) && aName.equals(bName);
    }

    private static boolean isCompatibleSignals(Set<String> aSignals, Set<String> bSignals) {
        return (aSignals != null)  && aSignals.equals(bSignals);
    }

}
