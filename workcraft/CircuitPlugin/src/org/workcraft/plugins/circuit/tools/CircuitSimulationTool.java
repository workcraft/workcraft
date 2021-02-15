package org.workcraft.plugins.circuit.tools;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.*;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.gui.tools.Decorator;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.plugins.builtin.settings.SimulationDecorationSettings;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.stg.CircuitToStgConverter;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.converters.SignalStg;
import org.workcraft.plugins.stg.tools.StgSimulationTool;
import org.workcraft.types.Pair;
import org.workcraft.utils.ColorUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashSet;

public class CircuitSimulationTool extends StgSimulationTool {

    private CircuitToStgConverter converter;

    @Override
    public void generateUnderlyingModel(WorkspaceEntry we) {
        VisualCircuit circuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
        converter = new CircuitToStgConverter(circuit);
    }

    @Override
    public Stg getUnderlyingModel() {
        return converter.getStg().getMathModel();
    }

    @Override
    public VisualModel getUnderlyingVisualModel() {
        return converter.getStg();
    }

    @Override
    public void applySavedState(final GraphEditor editor) {
        if ((savedState == null) || savedState.isEmpty()) {
            return;
        }
        MathModel model = editor.getModel().getMathModel();
        if (model instanceof Circuit) {
            editor.getWorkspaceEntry().saveMemento();
            Circuit circuit = (Circuit) model;
            for (FunctionContact contact: circuit.getFunctionContacts()) {
                String contactName = CircuitUtils.getSignalReference(circuit, contact);
                String oneName = SignalStg.appendHighSuffix(contactName);
                Node underlyingOneNode = getUnderlyingModel().getNodeByReference(oneName);
                if ((underlyingOneNode instanceof Place) && savedState.containsKey(underlyingOneNode)) {
                    boolean signalLevel = savedState.get(underlyingOneNode) > 0;
                    contact.setInitToOne(signalLevel);
                }
            }
        }
    }

    // Return all enabled transitions associated with the contact
    public HashSet<SignalTransition> getContactExcitedTransitions(VisualContact contact) {
        HashSet<SignalTransition> result = new HashSet<>();
        if ((converter != null) && contact.isDriver()) {
            SignalStg signalStg = converter.getSignalStg(contact);
            if (signalStg != null) {
                for (VisualSignalTransition transition: signalStg.getAllTransitions()) {
                    if (isEnabledUnderlyingNode(transition.getReferencedComponent())) {
                        result.add(transition.getReferencedComponent());
                    }
                }
            }
        }
        return result;
    }

    private Collection<VisualContact> getExcitedOutputs(VisualFunctionComponent component) {
        HashSet<VisualContact> excitedOutputs = new HashSet<>();
        if (!component.getIsZeroDelay()) {
            for (VisualContact output: component.getVisualOutputs()) {
                HashSet<SignalTransition> excitedTransitions = getContactExcitedTransitions(output);
                if (!excitedTransitions.isEmpty()) {
                    excitedOutputs.add(output);
                }
            }
        }
        return excitedOutputs;
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            VisualModel model = e.getModel();
            Node deepestNode = HitMan.hitDeepest(e.getPosition(), model.getRoot(),
                    node -> (node instanceof VisualFunctionComponent) || (node instanceof VisualContact));

            GraphEditor editor = e.getEditor();
            VisualContact contact = null;
            if (deepestNode instanceof VisualContact) {
                contact = (VisualContact) deepestNode;
            } else if (deepestNode instanceof VisualFunctionComponent) {
                VisualFunctionComponent component = (VisualFunctionComponent) deepestNode;
                Collection<VisualContact> excitedOutputs = getExcitedOutputs(component);
                if (excitedOutputs.size() == 1) {
                    contact = excitedOutputs.iterator().next();
                } else if (excitedOutputs.size() > 1) {
                    flashIssue(editor, "More than one output of this component is enabled.");
                }
            }

            if (contact != null) {
                hideIssue(editor);
                HashSet<SignalTransition> transitions = getContactExcitedTransitions(contact);
                SignalTransition transition = null;
                Node traceCurrentNode = getCurrentUnderlyingNode();
                if (transitions.contains(traceCurrentNode)) {
                    transition = (SignalTransition) traceCurrentNode;
                } else if (!transitions.isEmpty()) {
                    transition = transitions.iterator().next();
                }
                if (transition != null) {
                    executeUnderlyingNode(editor, transition);
                }
            }
        }
    }

    @Override
    public boolean isContainerExcited(VisualModel model, Container container) {
        if (excitedContainers.containsKey(container)) {
            return excitedContainers.get(container);
        }
        boolean result = false;
        for (Node node : container.getChildren()) {
            if (node instanceof VisualContact) {
                VisualContact contact = (VisualContact) node;
                HashSet<SignalTransition> transitions = getContactExcitedTransitions(contact);
                result = !transitions.isEmpty();
            } else if (node instanceof Container) {
                result = isContainerExcited(model, (Container) node);
            }
            if (result) {
                break;
            }
        }
        excitedContainers.put(container, result);
        return result;
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return "Click on a highlighted contact or component to toggle its state.";
    }

    @Override
    public Decorator getDecorator(final GraphEditor editor) {
        return node -> {
            if (converter == null) {
                return null;
            }
            if ((node instanceof VisualPage) || (node instanceof VisualGroup)) {
                return getContainerDecoration(editor.getModel(), (Container) node);
            }
            if (node instanceof VisualFunctionComponent) {
                return getFunctionComponentDecoration((VisualFunctionComponent) node);
            }
            if (node instanceof VisualContact) {
                return getContactDecoration((VisualContact) node);
            }
            if (node instanceof VisualCircuitConnection) {
                return getConnectionOrJointDecoration((VisualCircuitConnection) node);
            }
            if (node instanceof VisualJoint) {
                return getConnectionOrJointDecoration((VisualJoint) node);
            }
            return null;
        };
    }

    protected Decoration getFunctionComponentDecoration(VisualFunctionComponent component) {
        boolean hasSuggestedOutput = false;
        Collection<VisualContact> excitedOutputs = getExcitedOutputs(component);
        if (!excitedOutputs.isEmpty()) {
            Node traceCurrentNode = getCurrentUnderlyingNode();
            for (VisualContact output: excitedOutputs) {
                Pair<SignalStg, Boolean> signalStgAndInversion = converter.getSignalStgAndInversion(output);
                if (signalStgAndInversion != null) {
                    SignalStg signalStg = signalStgAndInversion.getFirst();
                    if (signalStg.contains(traceCurrentNode)) {
                        hasSuggestedOutput = true;
                        break;
                    }
                }
            }
        }
        final boolean isExcited = !excitedOutputs.isEmpty();
        final boolean isSuggested = hasSuggestedOutput;
        return new Decoration() {
            @Override
            public Color getColorisation() {
                return isExcited ? SimulationDecorationSettings.getExcitedComponentColor() : null;
            }
            @Override
            public Color getBackground() {
                return isSuggested ? SimulationDecorationSettings.getSuggestedComponentColor() : null;
            }
        };
    }

    public Decoration getContactDecoration(VisualContact contact) {
        Pair<SignalStg, Boolean> signalStgAndInversion = converter.getSignalStgAndInversion(contact);
        if (signalStgAndInversion == null) {
            return null;
        }
        boolean isZeroDelay = false;
        Node parent = contact.getParent();
        if (parent instanceof VisualFunctionComponent) {
            isZeroDelay = ((VisualFunctionComponent) parent).getIsZeroDelay();
        }
        Node traceCurrentNode = getCurrentUnderlyingNode();
        SignalStg signalStg = signalStgAndInversion.getFirst();
        boolean isInverting = signalStgAndInversion.getSecond();
        final boolean isOne = (signalStg.one.getReferencedComponent().getTokens() == 1) != isInverting;
        final boolean isZero = (signalStg.zero.getReferencedComponent().getTokens() == 1) != isInverting;
        final boolean isExcited = !getContactExcitedTransitions(contact).isEmpty() && !isZeroDelay;
        final boolean isSuggested = isExcited && signalStg.contains(traceCurrentNode);
        return new StateDecoration() {
            @Override
            public Color getColorisation() {
                return isExcited ? SimulationDecorationSettings.getExcitedComponentColor() : null;
            }
            @Override
            public Color getBackground() {
                Color  colorisation = isSuggested ? SimulationDecorationSettings.getSuggestedComponentColor() : null;
                if (isOne && !isZero) {
                    return ColorUtils.colorise(CircuitSettings.getActiveWireColor(), colorisation);
                }
                if (!isOne && isZero) {
                    return ColorUtils.colorise(CircuitSettings.getInactiveWireColor(), colorisation);
                }
                return colorisation;
            }
            @Override
            public boolean showForcedInit() {
                return false;
            }
        };
    }

    public Decoration getConnectionOrJointDecoration(VisualNode node) {
        Pair<SignalStg, Boolean> signalStgAndInversion = converter.getSignalStgAndInversion(node);
        if (signalStgAndInversion == null) {
            return null;
        }
        SignalStg signalStg = signalStgAndInversion.getFirst();
        boolean isInverting = signalStgAndInversion.getSecond();
        final boolean isOne = (signalStg.one.getReferencedComponent().getTokens() == 1) != isInverting;
        final boolean isZero = (signalStg.zero.getReferencedComponent().getTokens() == 1) != isInverting;
        return new StateDecoration() {
            @Override
            public Color getColorisation() {
                if (isOne && !isZero) {
                    return CircuitSettings.getActiveWireColor();
                }
                if (!isOne && isZero) {
                    return CircuitSettings.getInactiveWireColor();
                }
                return null;
            }
            @Override
            public Color getBackground() {
                return null;
            }
            @Override
            public boolean showForcedInit() {
                return false;
            }
        };
    }

}
