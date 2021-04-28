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
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.*;

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

    public static void updateInterface(WorkspaceEntry we, Set<File> changedRefinementFiles) {
        Framework framework = Framework.getInstance();
        VisualCircuit circuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
        for (VisualFunctionComponent component : circuit.getVisualFunctionComponents()) {
            Pair<File, Circuit> refinementFileCircuitPair = getRefinementCircuit(component.getReferencedComponent());
            if (refinementFileCircuitPair != null) {
                File refinementFile = refinementFileCircuitPair.getFirst();
                if (changedRefinementFiles.contains(refinementFile)) {
                    try {
                        //Circuit refinementCircuit = refinementFileCircuitPair.getSecond();
                        WorkspaceEntry refinementWe = framework.loadWork(refinementFile, false);
                        Circuit refinementCircuit = WorkspaceUtils.getAs(refinementWe, Circuit.class);
                        ComponentInterface refinementInterface = getModelInterface(refinementCircuit);
                        updateInterface(circuit, component, refinementInterface);
                    } catch (DeserialisationException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    public static void updateInterface(VisualCircuit circuit, VisualFunctionComponent component,
            ComponentInterface componentInterface) {

        Set<String> inputs = componentInterface.getInputs();
        Set<String> outputs = componentInterface.getOutputs();
        for (VisualFunctionContact contact : component.getVisualFunctionContacts()) {
            String signal = contact.getName();
            boolean matchesRefinementInput = contact.isInput() && inputs.contains(signal);
            boolean matchesRefinementOutput = contact.isOutput() && outputs.contains(signal);
            if (!matchesRefinementInput && !matchesRefinementOutput) {
                component.remove(contact);
            }
        }
        component.getReferencedComponent().setModule(componentInterface.getName());
        for (String signal : inputs) {
            circuit.getOrCreateContact(component, signal, Contact.IOType.INPUT);
        }
        for (String outputSignal : outputs) {
            circuit.getOrCreateContact(component, outputSignal, Contact.IOType.OUTPUT);
        }
    }

    public static List<File> getOrderedCircuitRefinementFiles(ModelEntry me) {
        List<File> result = new ArrayList<>();
        Stack<File> stack = new Stack<>();
        stack.addAll(getRefinementFiles(me));
        Set<File> processed = new HashSet<>();
        while (!stack.empty()) {
            File curFile = stack.pop();
            try {
                ModelEntry curMe = WorkUtils.loadModel(curFile);
                Set<File> refinementFiles = getRefinementFiles(curMe);
                refinementFiles.removeAll(processed);
                if (refinementFiles.isEmpty()) {
                    if (WorkspaceUtils.isApplicable(curMe, Circuit.class)) {
                        result.add(curFile);
                    }
                    processed.add(curFile);
                } else {
                    stack.push(curFile);
                    stack.addAll(refinementFiles);
                }
            } catch (DeserialisationException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static Set<File> getRefinementFiles(ModelEntry me) {
        Set<File> result = new HashSet<>();
        if (WorkspaceUtils.isApplicable(me, Stg.class)) {
            Stg stg = WorkspaceUtils.getAs(me, Stg.class);
            result.addAll(getRefinementFiles(stg));
        }
        if (WorkspaceUtils.isApplicable(me, Circuit.class)) {
            Circuit circuit = WorkspaceUtils.getAs(me, Circuit.class);
            result.addAll(getRefinementFiles(circuit));
        }
        return result;
    }

    public static Set<File> getRefinementFiles(Stg stg) {
        Set<File> result = new HashSet<>();
        File refinementFile = stg.getRefinementFile();
        if (refinementFile != null) {
            result.add(refinementFile);
        }
        return result;
    }

    public static Set<File> getRefinementFiles(Circuit circuit) {
        Set<File> result = new HashSet<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            File refinementFile = component.getRefinementFile();
            if (refinementFile != null) {
                result.add(refinementFile);
            }
        }
        return result;
    }

}
