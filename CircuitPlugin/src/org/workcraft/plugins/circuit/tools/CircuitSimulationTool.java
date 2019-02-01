package org.workcraft.plugins.circuit.tools;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashSet;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.ContainerDecoration;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualCircuitConnection;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.VisualJoint;
import org.workcraft.plugins.circuit.stg.CircuitToStgConverter;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.shared.CommonDecorationSettings;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.converters.SignalStg;
import org.workcraft.plugins.stg.tools.StgSimulationTool;
import org.workcraft.util.Pair;

public class CircuitSimulationTool extends StgSimulationTool {

    private CircuitToStgConverter converter;

    @Override
    public void generateUnderlyingModel(VisualModel model) {
        VisualCircuit circuit = (VisualCircuit) model;
        converter = new CircuitToStgConverter(circuit);
        setUnderlyingModel(converter.getStg());
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
                Node underlyingOneNode = getUnderlyingStg().getNodeByReference(oneName);
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
                    if (isEnabledNode(transition.getReferencedTransition())) {
                        result.add(transition.getReferencedTransition());
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
                Node traceCurrentNode = getTraceCurrentNode();
                if (transitions.contains(traceCurrentNode)) {
                    transition = (SignalTransition) traceCurrentNode;
                } else if (!transitions.isEmpty()) {
                    transition = transitions.iterator().next();
                }
                if (transition != null) {
                    executeTransition(editor, transition);
                }
            }
        }
    }

    @Override
    public boolean isContainerExcited(Container container) {
        if (excitedContainers.containsKey(container)) return excitedContainers.get(container);
        boolean ret = false;
        for (Node node: container.getChildren()) {
            if (node instanceof VisualContact) {
                HashSet<SignalTransition> transitions = getContactExcitedTransitions((VisualContact) node);
                ret = ret || !transitions.isEmpty();
            }
            if (node instanceof Container) {
                ret = ret || isContainerExcited((Container) node);
            }
            if (ret) {
                break;
            }
        }
        excitedContainers.put(container, ret);
        return ret;
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return "Click on a highlighted contact or component to toggle its state.";
    }

    @Override
    public Decorator getDecorator(final GraphEditor editor) {
        return new Decorator() {
            @Override
            public Decoration getDecoration(Node node) {
                if (converter == null) return null;
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
                if (node instanceof VisualPage || node instanceof VisualGroup) {
                    return getContainerDecoration((Container) node);
                }
                return null;
            }
        };
    }

    protected Decoration getFunctionComponentDecoration(VisualFunctionComponent component) {
        boolean hasSuggestedOutput = false;
        Collection<VisualContact> excitedOutputs = getExcitedOutputs(component);
        if (!excitedOutputs.isEmpty()) {
            Node traceCurrentNode = getTraceCurrentNode();
            for (VisualContact output: excitedOutputs) {
                Pair<SignalStg, Boolean> signalStgAndInversion = converter.getSignalStgAndInvertion(output);
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
                return isExcited ? CommonDecorationSettings.getExcitedComponentColor() : null;
            }
            @Override
            public Color getBackground() {
                return isSuggested ? CommonDecorationSettings.getSuggestedComponentColor() : null;
            }
        };
    }

    public Decoration getContactDecoration(VisualContact contact) {
        Pair<SignalStg, Boolean> signalStgAndInversion = converter.getSignalStgAndInvertion(contact);
        if (signalStgAndInversion == null) {
            return null;
        }
        boolean isZeroDelay = false;
        Node parent = contact.getParent();
        if (parent instanceof VisualFunctionComponent) {
            isZeroDelay = ((VisualFunctionComponent) parent).getIsZeroDelay();
        }
        Node traceCurrentNode = getTraceCurrentNode();
        SignalStg signalStg = signalStgAndInversion.getFirst();
        boolean isInverting = signalStgAndInversion.getSecond();
        final boolean isOne = (signalStg.one.getReferencedPlace().getTokens() == 1) != isInverting;
        final boolean isZero = (signalStg.zero.getReferencedPlace().getTokens() == 1) != isInverting;
        final boolean isExcited = !getContactExcitedTransitions(contact).isEmpty() && !isZeroDelay;
        final boolean isSuggested = isExcited && signalStg.contains(traceCurrentNode) && !isZeroDelay;
        return new StateDecoration() {
            @Override
            public Color getColorisation() {
                return isExcited ? CommonDecorationSettings.getExcitedComponentColor() : null;
            }
            @Override
            public Color getBackground() {
                Color  colorisation = isSuggested ? CommonDecorationSettings.getSuggestedComponentColor() : null;
                if (isOne && !isZero) {
                    return Coloriser.colorise(CircuitSettings.getActiveWireColor(), colorisation);
                }
                if (!isOne && isZero) {
                    return Coloriser.colorise(CircuitSettings.getInactiveWireColor(), colorisation);
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
        Pair<SignalStg, Boolean> signalStgAndInversion = converter.getSignalStgAndInvertion(node);
        if (signalStgAndInversion == null) {
            return null;
        }
        SignalStg signalStg = signalStgAndInversion.getFirst();
        boolean isInverting = signalStgAndInversion.getSecond();
        final boolean isOne = (signalStg.one.getReferencedPlace().getTokens() == 1) != isInverting;
        final boolean isZero = (signalStg.zero.getReferencedPlace().getTokens() == 1) != isInverting;
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

    @Override
    public Decoration getContainerDecoration(Container container) {
        final boolean ret = isContainerExcited(container);
        return new ContainerDecoration() {
            @Override
            public Color getColorisation() {
                return null;
            }
            @Override
            public Color getBackground() {
                return null;
            }
            @Override
            public boolean isContainerExcited() {
                return ret;
            }
        };
    }

}
