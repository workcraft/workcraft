package org.workcraft.plugins.fsm.tools;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.gui.tools.Decorator;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.plugins.builtin.settings.CommonDecorationSettings;
import org.workcraft.plugins.fsm.*;
import org.workcraft.plugins.fsm.converters.FsmToPetriConverter;
import org.workcraft.plugins.petri.*;
import org.workcraft.plugins.petri.tools.PetriSimulationTool;

import java.awt.*;
import java.awt.event.MouseEvent;

public class FsmSimulationTool extends PetriSimulationTool {

    private FsmToPetriConverter converter;

    @Override
    public void activated(final GraphEditor editor) {
        super.activated(editor);
        setStatePaneVisibility(false);
    }

    @Override
    public String getTraceLabelByReference(String ref) {
        String label = null;
        if (ref != null) {
            label = converter.getSymbol(ref);
            if (label == "") {
                label = Character.toString(VisualEvent.EPSILON_SYMBOL);
            }
        }
        if (label == null) {
            label = super.getTraceLabelByReference(ref);
        }
        return label;
    }

    @Override
    public void generateUnderlyingModel(VisualModel model) {
        final VisualFsm fsm = (VisualFsm) model;
        final VisualPetri petri = new VisualPetri(new Petri());
        converter = new FsmToPetriConverter(fsm, petri);
        setUnderlyingModel(converter.getDstModel());
    }

    @Override
    public void applySavedState(final GraphEditor editor) {
        if ((savedState == null) || savedState.isEmpty()) {
            return;
        }
        MathModel model = editor.getModel().getMathModel();
        if (model instanceof Fsm) {
            editor.getWorkspaceEntry().saveMemento();
            Fsm fsm = (Fsm) model;
            for (State state: fsm.getStates()) {
                String ref = fsm.getNodeReference(state);
                Node underlyingNode = getUnderlyingPetri().getNodeByReference(ref);
                if ((underlyingNode instanceof Place) && savedState.containsKey(underlyingNode)) {
                    boolean isInitial = savedState.get(underlyingNode) > 0;
                    state.setInitial(isInitial);
                }
            }
        }
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            VisualModel model = e.getModel();
            Node deepestNode = HitMan.hitDeepest(e.getPosition(), model.getRoot(),
                    node -> getExcitedTransitionOfNode(node) != null);

            Transition transition = getExcitedTransitionOfNode(deepestNode);
            if (transition != null) {
                executeTransition(e.getEditor(), transition);
            }
        }
    }

    @Override
    public boolean isContainerExcited(Container container) {
        if (excitedContainers.containsKey(container)) return excitedContainers.get(container);
        boolean ret = false;
        for (Node node: container.getChildren()) {
            if (node instanceof VisualEvent) {
                ret = ret || (getExcitedTransitionOfNode(node) != null);
            }
            if (node instanceof Container) {
                ret = ret || isContainerExcited((Container) node);
            }
            if (ret) break;
        }
        excitedContainers.put(container, ret);
        return ret;
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return "Click on a highlighted arc to trigger its event.";
    }

    @Override
    public Decorator getDecorator(final GraphEditor editor) {
        return new Decorator() {
            @Override
            public Decoration getDecoration(Node node) {
                if (converter == null) return null;
                if (node instanceof VisualState) {
                    return getStateDecoration((VisualState) node);
                } else if (node instanceof VisualEvent) {
                    return getEventDecoration((VisualEvent) node);
                } else if (node instanceof VisualPage || node instanceof VisualGroup) {
                    return getContainerDecoration((Container) node);
                }
                return null;
            }
        };
    }

    public Decoration getEventDecoration(VisualEvent event) {
        Node transition = getTraceCurrentNode();
        final boolean isExcited = getExcitedTransitionOfNode(event) != null;
        final boolean isSuggested = isExcited && converter.isRelated(event, transition);
        return new Decoration() {
            @Override
            public Color getColorisation() {
                return isExcited ? CommonDecorationSettings.getSimulationExcitedComponentColor() : null;
            }

            @Override
            public Color getBackground() {
                return isSuggested ? CommonDecorationSettings.getSimulationSuggestedComponentColor() : null;
            }
        };
    }

    public Decoration getStateDecoration(VisualState state) {
        VisualPlace p = converter.getRelatedPlace(state);
        if (p == null) {
            return null;
        }
        final boolean isMarkedPlace = p.getReferencedPlace().getTokens() > 0;
        return new Decoration() {
            @Override
            public Color getColorisation() {
                return isMarkedPlace ? CommonDecorationSettings.getSimulationExcitedComponentColor() : null;
            }
            @Override
            public Color getBackground() {
                return null;
            }
        };
    }

    private Transition getExcitedTransitionOfNode(Node node) {
        if ((node != null) && (node instanceof VisualEvent)) {
            VisualTransition vTransition = converter.getRelatedTransition((VisualEvent) node);
            if (vTransition != null) {
                Transition transition = vTransition.getReferencedTransition();
                if (isEnabledNode(transition)) {
                    return transition;
                }
            }
        }
        return null;
    }

}
