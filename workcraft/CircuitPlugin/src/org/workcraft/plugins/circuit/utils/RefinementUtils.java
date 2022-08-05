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
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.utils.StgUtils;
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
                if (isInconsistentSignalNames(component, refinementCircuit.getSecond())) {
                    result.add(component);
                }
            }
        }
        return result;
    }

    public static ComponentInterface getComponentInterface(CircuitComponent component) {
        return component == null ? null : new ComponentInterface(
                CircuitUtils.getInputPinNames(component), CircuitUtils.getOutputPinNames(component));
    }

    public static ComponentInterface getModelInterface(ModelEntry me) {
        return me == null ? null : getModelInterface(me.getMathModel());
    }

    public static ComponentInterface getModelInterface(MathModel model) {
        return new ComponentInterface(getInputSignals(model), getOutputSignals(model));
    }

    public static Set<String> getInputSignals(MathModel model) {
        if (model instanceof Circuit) {
            return CircuitUtils.getInputPortNames((Circuit) model);
        }
        if (model instanceof Stg) {
            return ((Stg) model).getSignalReferences(Signal.Type.INPUT);
        }
        return null;
    }

    public static Set<String> getOutputSignals(MathModel model) {
        if (model instanceof Circuit) {
            return CircuitUtils.getOutputPortNames((Circuit) model);
        }
        if (model instanceof Stg) {
            return ((Stg) model).getSignalReferences(Signal.Type.OUTPUT);
        }
        return null;
    }

    public static boolean isInconsistentSignalNames(Stg stg, ModelEntry refinementModelEntry) {
        ComponentInterface stgInterface = RefinementUtils.getModelInterface(stg);
        ComponentInterface refinementInterface = RefinementUtils.getModelInterface(refinementModelEntry);
        return isInconsistentSignalNames(stgInterface, refinementInterface);
    }

    public static boolean isInconsistentSignalNames(CircuitComponent component, ModelEntry refinementModelEntry) {
        ComponentInterface componentInterface = RefinementUtils.getComponentInterface(component);
        ComponentInterface refinementInterface = RefinementUtils.getModelInterface(refinementModelEntry);
        return isInconsistentSignalNames(componentInterface, refinementInterface);
    }

    public static boolean isInconsistentSignalNames(CircuitComponent component, Circuit refinementCircuit) {
        ComponentInterface componentInterface = RefinementUtils.getComponentInterface(component);
        ComponentInterface refinementInterface = RefinementUtils.getModelInterface(refinementCircuit);
        return isInconsistentSignalNames(componentInterface, refinementInterface);
    }

    public static boolean isInconsistentSignalNames(ComponentInterface ci1, ComponentInterface ci2) {
        return (ci1 == null) || (ci2 == null)
                || isInconsistentSignalNames(ci1.getInputs(), ci2.getInputs())
                || isInconsistentSignalNames(ci1.getOutputs(), ci2.getOutputs());
    }

    private static boolean isInconsistentSignalNames(Set<String> aSignals, Set<String> bSignals) {
        return (aSignals == null) || !aSignals.equals(bSignals);
    }

    public static Map<String, Boolean> getInterfaceInitialState(ModelEntry me) {
        return me == null ? null : getInterfaceInitialState(me.getMathModel());
    }

    public static Map<String, Boolean> getInterfaceInitialState(MathModel model) {
        Map<String, Boolean> result = new HashMap<>();
        if (model instanceof Stg) {
            result.putAll(getInterfaceInitialState((Stg) model));
        }
        if (model instanceof Circuit) {
            result.putAll(getInterfaceInitialState((Circuit) model));
        }
        return result;
    }

    public static Map<String, Boolean> getInterfaceInitialState(StgModel stg) {
        Map<String, Boolean> result = new HashMap<>();
        Map<String, Boolean> initialState = StgUtils.getInitialState(stg, 1000);
        Set<String> interfaceSignals = new HashSet<>();
        interfaceSignals.addAll(stg.getSignalReferences(Signal.Type.INPUT));
        interfaceSignals.addAll(stg.getSignalReferences(Signal.Type.OUTPUT));
        for (String signal : interfaceSignals) {
            if (initialState.containsKey(signal)) {
                result.put(signal, initialState.get(signal));
            }
        }
        return result;
    }

    public static Map<String, Boolean> getInterfaceInitialState(Circuit circuit) {
        Map<String, Boolean> result = new HashMap<>();
        Set<Contact> ports = new HashSet<>();
        ports.addAll(circuit.getInputPorts());
        ports.addAll(circuit.getOutputPorts());
        for (Contact port : ports) {
            result.put(circuit.getNodeReference(port), port.getInitToOne());
        }
        return result;
    }

    public static Map<String, Boolean> getComponentInterfaceInitialState(Circuit circuit, CircuitComponent component) {
        Map<String, Boolean> result = new HashMap<>();
        Set<Contact> pins = new HashSet<>();
        pins.addAll(component.getInputs());
        pins.addAll(component.getOutputs());
        for (Contact pin : pins) {
            Pair<Contact, Boolean> pair = CircuitUtils.findDriverAndInversionSkipZeroDelay(circuit, pin);
            if (pair != null) {
                Contact driver = pair.getFirst();
                Boolean inversion = pair.getSecond();
                result.put(pin.getName(), driver.getInitToOne() != inversion);
            }
        }
        return result;
    }

    public static boolean isInconsistentInitialStates(Map<String, Boolean> is1, Map<String, Boolean> is2) {
        Set<String> signals = new HashSet<>();
        signals.addAll(is1.keySet());
        signals.retainAll(is2.keySet());
        return !signals.stream().allMatch(signal -> is1.get(signal) == is2.get(signal));
    }

    public static boolean isInconsistentModelTitle(Stg stg, ModelEntry refinementModelEntry) {
        return isInconsistentModelTitle(stg.getTitle(), refinementModelEntry.getModel().getTitle());
    }

    public static boolean isInconsistentModelTitle(CircuitComponent component, ModelEntry refinementModelEntry) {
        return isInconsistentModelTitle(component.getModule(), refinementModelEntry.getModel().getTitle());
    }

    public static boolean isInconsistentModelTitle(String aTitle, String bTitle) {
        return (aTitle == null) || !aTitle.equals(bTitle);
    }

    public static void updateInterface(VisualCircuit circuit, Set<File> changedRefinementFiles) {
        for (VisualFunctionComponent component : circuit.getVisualFunctionComponents()) {
            Pair<File, Circuit> refinementFileCircuitPair = getRefinementCircuit(component.getReferencedComponent());
            if (refinementFileCircuitPair != null) {
                File refinementFile = refinementFileCircuitPair.getFirst();
                if (changedRefinementFiles.contains(refinementFile)) {
                    Circuit refinementCircuit = refinementFileCircuitPair.getSecond();
                    ComponentInterface refinementInterface = getModelInterface(refinementCircuit);
                    updateInterface(circuit, component, refinementInterface);
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
        for (String input : inputs) {
            circuit.getOrCreateContact(component, input, Contact.IOType.INPUT);
        }
        for (String output : outputs) {
            circuit.getOrCreateContact(component, output, Contact.IOType.OUTPUT);
        }
    }

    public static Set<String> getConstrainedPins(VisualFunctionComponent component) {
        Set<String> result = new HashSet<>();
        for (VisualFunctionContact contact : component.getVisualFunctionContacts()) {
            if ((contact.getSetFunction() != null) || (contact.getResetFunction() != null)) {
                result.add(contact.getName());
            }
        }
        return result;
    }

    public static void removeComponentFunctions(VisualFunctionComponent component) {
        for (VisualFunctionContact contact : component.getVisualFunctionContacts()) {
            contact.setSetFunction(null);
            contact.setResetFunction(null);
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
