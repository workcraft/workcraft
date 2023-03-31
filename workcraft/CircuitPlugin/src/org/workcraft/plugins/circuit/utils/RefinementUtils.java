package org.workcraft.plugins.circuit.utils;

import org.workcraft.Framework;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.references.FileReference;
import org.workcraft.dom.references.Identifier;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.refinement.ComponentInterface;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.utils.*;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

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
        return (component != null) && (getRefinementStgFile(component.getReferencedComponent()) != null);
    }

    public static void openRefinementStg(VisualCircuitComponent component) {
        if (component != null) {
            File refinementStgFile = getRefinementStgFile(component.getReferencedComponent());
            if (refinementStgFile != null) {
                openRefinementFile(refinementStgFile);
            }
        }
    }

    public static boolean hasRefinementCircuit(VisualFunctionComponent component) {
        return (component != null) && (getRefinementCircuitFile(component.getReferencedComponent()) != null);
    }

    public static void openRefinementCircuit(VisualCircuitComponent component) {
        if (component != null) {
            File refinementCircuitFile = getRefinementCircuitFile(component.getReferencedComponent());
            if (refinementCircuitFile != null) {
                openRefinementFile(refinementCircuitFile);
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

    public static File getRefinementStgFile(CircuitComponent component) {
        File file = component.getRefinementFile();
        if (file != null) {
            try {
                ModelDescriptor modelDescriptor = WorkUtils.extractModelDescriptor(file);
                if (modelDescriptor instanceof StgDescriptor) {
                    return file;
                }
            } catch (DeserialisationException e) {
                LogUtils.logError("Cannot read model from file '" + FileUtils.getFullPath(file) + "':\n" + e.getMessage());
            }
        }
        return null;
    }

    public static File getRefinementCircuitFile(CircuitComponent component) {
        File file = component.getRefinementFile();
        Set<File> visited = new HashSet<>();
        while (file != null) {
            if (!FileUtils.checkFileReadability(file, false)) {
                return null;
            }
            if (visited.contains(file)) {
                LogUtils.logError("Cyclic dependency on file '" + FileUtils.getFullPath(file) + "'");
                return null;
            }
            visited.add(file);
            try {
                ModelDescriptor modelDescriptor = WorkUtils.extractModelDescriptor(file);
                if (modelDescriptor instanceof CircuitDescriptor) {
                    return file;
                }
                if (modelDescriptor instanceof StgDescriptor) {
                    ModelEntry me = WorkUtils.loadModel(file);
                    Stg stg = WorkspaceUtils.getAs(me, Stg.class);
                    file = stg.getRefinementFile();
                } else {
                    LogUtils.logError("Unexpected model type in file '" + FileUtils.getFullPath(file) + "'");
                    return null;
                }
            } catch (DeserialisationException e) {
                LogUtils.logError("Cannot read model from file '" + FileUtils.getFullPath(file) + "':\n" + e.getMessage());
                return null;
            }
        }
        return null;
    }

    public static Set<FunctionComponent> getIncompatibleRefinementCircuitComponents(Circuit circuit) {
        Set<FunctionComponent> result = new HashSet<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            File refinementCircuitFile = getRefinementCircuitFile(component);
            if (refinementCircuitFile != null) {
                try {
                    ModelEntry me = WorkUtils.loadModel(refinementCircuitFile);
                    Circuit refinementCircuit = WorkspaceUtils.getAs(me, Circuit.class);
                    if (hasInconsistentSignalNames(component, refinementCircuit)) {
                        result.add(component);
                    }
                } catch (DeserialisationException e) {
                    String filePath = FileUtils.getFullPath(refinementCircuitFile);
                    LogUtils.logError("Cannot read model from file '" + filePath + "':\n" + e.getMessage());
                }
            }
        }
        return result;
    }

    public static ComponentInterface getComponentInterface(CircuitComponent component) {
        return component == null ? null : new ComponentInterface(
                CircuitUtils.getInputPinNames(component), CircuitUtils.getOutputPinNames(component));
    }

    public static ComponentInterface getModelInterface(MathModel model) {
        return model == null ? null : new ComponentInterface(getInputSignals(model), getOutputSignals(model));
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

    public static boolean hasInconsistentSignalNames(Stg stg, Stg refinementStg) {
        ComponentInterface stgInterface = RefinementUtils.getModelInterface(stg);
        ComponentInterface refinementInterface = RefinementUtils.getModelInterface(refinementStg);

        String messageIntro = getStgMessageIntro(stg);
        if ((stgInterface == null) || (refinementInterface == null)) {
            LogUtils.logError(messageIntro + " cannot be checked against its refinement STG");
            return true;
        }

        // Abstract STG and its refinement STG:
        // * Inputs must be the same in both STGs.
        //   (If there is mismatch, then issue an error.)
        // * Outputs must be the same in both STGs.
        //   (If there is mismatch, then issue an error.)
        Set<String> missingInputSignals = stgInterface.getMissingInputSignals(refinementInterface);
        Set<String> extraInputSignals = stgInterface.getExtraInputSignals(refinementInterface);
        Set<String> missingOutputSignals = stgInterface.getMissingOutputSignals(refinementInterface);
        Set<String> extraOutputSignals = stgInterface.getExtraOutputSignals(refinementInterface);
        boolean result = !missingInputSignals.isEmpty() || !extraInputSignals.isEmpty()
                || !missingOutputSignals.isEmpty() || !extraOutputSignals.isEmpty();

        if (result) {
            String message = messageIntro + " has a mismatch of signals with its refinement STG"
                    + TextUtils.getBulletpointPair("Missing input signal", missingInputSignals)
                    + TextUtils.getBulletpointPair("Unexpected input signal", extraInputSignals)
                    + TextUtils.getBulletpointPair("Missing output signal", missingOutputSignals)
                    + TextUtils.getBulletpointPair("Unexpected output signal", extraOutputSignals);

            LogUtils.logError(message);
        }
        return result;
    }

    public static boolean hasInconsistentSignalNames(Stg stg, Circuit refinementCircuit) {
        ComponentInterface stgInterface = RefinementUtils.getModelInterface(stg);
        ComponentInterface refinementInterface = RefinementUtils.getModelInterface(refinementCircuit);
        String messageIntro = getStgMessageIntro(stg);
        if ((stgInterface == null) || (refinementInterface == null)) {
            LogUtils.logError(messageIntro + " cannot be checked against its Circuit implementation");
            return true;
        }

        // Specification STG and its Circuit implementation:
        // * All inputs must be the same, except the Circuit implementation may have extra reset and scan inputs.
        //   (If there are other inputs, then issue an error.)
        // * All outputs must be the same, except the Circuit implementation may have extra scan outputs.
        //   (If there are other outputs, then issue an error.)
        Set<String> extraInputSignals = stgInterface.getExtraInputSignals(refinementInterface);
        Set<String> missingInputSignals = stgInterface.getMissingInputSignals(refinementInterface).stream()
                .filter(name -> !ScanUtils.isScanInputPortName(name) && !ResetUtils.isResetInputPortName(name))
                .collect(Collectors.toSet());

        Set<String> extraOutputSignals = stgInterface.getExtraOutputSignals(refinementInterface);
        Set<String> missingOutputSignals = stgInterface.getMissingOutputSignals(refinementInterface).stream()
                .filter(name -> !ScanUtils.isScanOutputPortName(name))
                .collect(Collectors.toSet());

        boolean result = !extraInputSignals.isEmpty() || !missingInputSignals.isEmpty()
                || !missingOutputSignals.isEmpty() || !extraOutputSignals.isEmpty();

        if (result) {
            String message = messageIntro + " has a mismatch of signals with its Circuit implementation"
                    + TextUtils.getBulletpointPair("Missing input signal", missingInputSignals)
                    + TextUtils.getBulletpointPair("Unexpected input signal", extraInputSignals)
                    + TextUtils.getBulletpointPair("Missing output signal", missingOutputSignals)
                    + TextUtils.getBulletpointPair("Unexpected output signal", extraOutputSignals);

            LogUtils.logError(message);
        }
        return result;
    }

    public static void warnAboutReservedInterfaceSignals(Stg stg) {
        ComponentInterface stgInterface = RefinementUtils.getModelInterface(stg);

        Set<String> reservedInterfaceSignals = stgInterface.getSignals().stream()
                .filter(name -> ResetUtils.isResetInputPortName(name)
                        || ScanUtils.isScanInputPortName(name) || ScanUtils.isScanOutputPortName(name))
                .collect(Collectors.toSet());

        if (!reservedInterfaceSignals.isEmpty()) {
            String message = getStgMessageIntro(stg) + " uses reserved interface signal";
            LogUtils.logWarning(TextUtils.wrapMessageWithItems(message, reservedInterfaceSignals));
        }
    }

    public static boolean hasInconsistentSignalNames(Circuit circuit, CircuitComponent component, Stg refinementStg) {
        ComponentInterface componentInterface = RefinementUtils.getComponentInterface(component);
        ComponentInterface refinementInterface = RefinementUtils.getModelInterface(refinementStg);
        String messageIntro = getComponentMessageIntro(circuit, component);
        if ((componentInterface == null) || (refinementInterface == null)) {
            LogUtils.logError(messageIntro + " cannot be checked against its refinement STG");
            return true;
        }

        // Circuit component and its refinement STG:
        // * Inputs of circuit component must be the same as in its refinement STG,
        //   with the addition of reset and scan inputs which are not in the refinement STG.
        //   (If there are other inputs, then issue an error.)
        // * Outputs of circuit component must be the same as in its refinement STG,
        //   with the addition of scan outputs which are not in the refinement STG.
        //   (If there are other outputs, then issue an error.)
        Set<String> extraInputSignals = componentInterface.getExtraInputSignals(refinementInterface);
        Set<String> missingInputSignals = componentInterface.getMissingInputSignals(refinementInterface).stream()
                .filter(name ->  !ResetUtils.isResetInputPortName(name) && !ScanUtils.isScanInputPortName(name))
                .collect(Collectors.toSet());

        Set<String> extraOutputSignals = componentInterface.getExtraOutputSignals(refinementInterface);
        Set<String> missingOutputSignals = componentInterface.getMissingOutputSignals(refinementInterface).stream()
                .filter(name -> !ScanUtils.isScanOutputPortName(name)).collect(Collectors.toSet());

        boolean result = !extraInputSignals.isEmpty() || !missingInputSignals.isEmpty()
                || !extraOutputSignals.isEmpty() || !missingOutputSignals.isEmpty();

        if (result) {
            String message = messageIntro + " has a mismatch of signals with its refinement STG"
                    + TextUtils.getBulletpointPair("Missing input signal", missingInputSignals)
                    + TextUtils.getBulletpointPair("Unexpected input signal", extraInputSignals)
                    + TextUtils.getBulletpointPair("Missing output signal", missingOutputSignals)
                    + TextUtils.getBulletpointPair("Unexpected output signal", extraOutputSignals);

            LogUtils.logError(message);
        }
        return result;
    }

    public static boolean hasInconsistentSignalNames(Circuit circuit, CircuitComponent component, Circuit refinementCircuit) {
        ComponentInterface componentInterface = RefinementUtils.getComponentInterface(component);
        ComponentInterface refinementInterface = RefinementUtils.getModelInterface(refinementCircuit);
        String messageIntro = getComponentMessageIntro(circuit, component);
        if ((componentInterface == null) || (refinementInterface == null)) {
            LogUtils.logError(messageIntro + " cannot be checked against its refinement STG");
            return true;
        }

        // Circuit component and its Circuit implementation (possibly via a series of refinement STGs):
        // * All interface signals must be the same in Circuit component and its Circuit implementation.
        //   (Note that we do not drop redundant inputs to support hierarchical Verilog export).
        Set<String> missingPins = componentInterface.getMissingSignals(refinementInterface);
        Set<String> extraPins = componentInterface.getExtraSignals(refinementInterface);
        Set<String> mismatchPins = componentInterface.getMismatchSignals(refinementInterface);
        boolean result = !missingPins.isEmpty() || !extraPins.isEmpty() || !mismatchPins.isEmpty();
        if (result) {
            String message = messageIntro + " has pins that do not match its Circuit implementation"
                    + TextUtils.getBulletpointPair("Missing component pin", missingPins)
                    + TextUtils.getBulletpointPair("Unexpected component pin", extraPins)
                    + TextUtils.getBulletpointPair("Incorrect I/O type pin", mismatchPins);

            LogUtils.logError(message);
        }
        return result;
    }

    private static String getStgMessageIntro(Stg stg) {
        String title = stg.getTitle();
        return "STG model" + (((title == null) || title.isEmpty()) ? "" : (" '" + title + "'"));
    }

    private static String getComponentMessageIntro(Circuit circuit, CircuitComponent component) {
        String title = circuit.getTitle();
        String location = ((title == null) || title.isEmpty()) ? "" : (" in circuit '" + title + "'");
        String ref = Identifier.truncateNamespaceSeparator(circuit.getNodeReference(component));
        return "Component '" + ref + "'" + location;
    }

    public static boolean hasInconsistentSignalNames(CircuitComponent component, Circuit refinementCircuit) {
        ComponentInterface componentInterface = RefinementUtils.getComponentInterface(component);
        ComponentInterface refinementInterface = RefinementUtils.getModelInterface(refinementCircuit);
        return hasInconsistentSignalNames(componentInterface, refinementInterface);
    }

    public static boolean hasInconsistentSignalNames(ComponentInterface ci1, ComponentInterface ci2) {
        return hasInconsistentSignalNames(ci1, ci2, false, false);
    }

    private static boolean hasInconsistentSignalNames(ComponentInterface ci1, ComponentInterface ci2,
            boolean ci1SkipSpecialSignals, boolean ci2SkipSpecialSignals) {

        if ((ci1 == null) || (ci2 == null)) {
            return true;
        }

        Set<String> ci1Inputs = ci1.getInputs().stream().filter(name -> !ci1SkipSpecialSignals
                        || !ResetUtils.isResetInputPortName(name) && !ScanUtils.isScanInputPortName(name))
                .collect(Collectors.toSet());

        Set<String> ci1Outputs = ci1.getOutputs().stream().filter(name -> !ci1SkipSpecialSignals
                        || !ScanUtils.isScanOutputPortName(name))
                .collect(Collectors.toSet());

        Set<String> ci2Inputs = ci2.getInputs().stream().filter(name -> !ci2SkipSpecialSignals
                        || !ResetUtils.isResetInputPortName(name) && !ScanUtils.isScanInputPortName(name))
                .collect(Collectors.toSet());

        Set<String> ci2Outputs = ci2.getOutputs().stream().filter(name -> !ci2SkipSpecialSignals
                        || !ScanUtils.isScanOutputPortName(name))
                .collect(Collectors.toSet());

        return hasInconsistentSignalNames(ci1Inputs, ci2Inputs) || hasInconsistentSignalNames(ci1Outputs, ci2Outputs);
    }

    private static boolean hasInconsistentSignalNames(Set<String> aSignals, Set<String> bSignals) {
        return (aSignals == null) || !aSignals.equals(bSignals);
    }

    public static Map<String, Boolean> getInterfaceInitialState(ModelEntry me) {
        return me == null ? null : getInterfaceInitialState(me.getMathModel());
    }

    public static Map<String, Boolean> getSignalsInitialState(MathModel model, Collection<String> signals) {
        Map<String, Boolean> interfaceInitialState = getInterfaceInitialState(model);
        return filterStates(interfaceInitialState, signals);
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
            result.put(circuit.getNodeReference(port), CircuitUtils.findInitToOneFromDriver(circuit, port));
        }
        return result;
    }

    public static Map<String, Boolean> getComponentSignalsInitialState(Circuit circuit, CircuitComponent component,
            Collection<String> signals) {

        Map<String, Boolean> interfaceInitialState = getComponentInterfaceInitialState(circuit, component);
        return filterStates(interfaceInitialState, signals);
    }

    public static Map<String, Boolean> filterStates(Map<String, Boolean> states, Collection<String> signals) {
        Map<String, Boolean> result = new HashMap<>();
        for (String signal : signals) {
            Boolean state = states.get(signal);
            if (state != null) {
                result.put(signal, state);
            }
        }
        return result;
    }

    public static Map<String, Boolean> getComponentInterfaceInitialState(Circuit circuit, CircuitComponent component) {
        Map<String, Boolean> result = new HashMap<>();
        Set<Contact> pins = new HashSet<>();
        pins.addAll(component.getInputs());
        pins.addAll(component.getOutputs());
        for (Contact pin : pins) {
            result.put(pin.getName(), CircuitUtils.findInitToOneFromDriver(circuit, pin));
        }
        return result;
    }

    public static boolean isInconsistentInitialStates(Map<String, Boolean> is1, Map<String, Boolean> is2) {
        return !getSignalsWithInconsistentStates(is1, is2).isEmpty();
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
            File refinementCircuitFile = getRefinementCircuitFile(component.getReferencedComponent());
            if ((refinementCircuitFile != null) && changedRefinementFiles.contains(refinementCircuitFile)) {
                try {
                    ModelEntry me = WorkUtils.loadModel(refinementCircuitFile);
                    Circuit refinementCircuit = WorkspaceUtils.getAs(me, Circuit.class);
                    ComponentInterface refinementInterface = getModelInterface(refinementCircuit);
                    updateInterface(circuit, component, refinementInterface);
                } catch (DeserialisationException e) {
                    throw new RuntimeException(e);
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

    public static Set<String> getSignalsWithInconsistentStates(Map<String, Boolean> is1, Map<String, Boolean> is2) {
        Set<String> signals = new HashSet<>(is1.keySet());
        signals.retainAll(is2.keySet());
        return signals.stream()
                .filter(signal -> is1.get(signal) != is2.get(signal))
                .collect(Collectors.toSet());
    }

    public static void updateInitialState(CircuitComponent component, Map<String, Boolean> states) {
        for (Contact contact : component.getOutputs()) {
            Boolean state = states.get(contact.getName());
            if (state != null) {
                contact.setInitToOne(state);
            }
        }
    }

}
