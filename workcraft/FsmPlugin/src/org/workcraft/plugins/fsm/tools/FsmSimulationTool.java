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
import org.workcraft.plugins.builtin.settings.SimulationDecorationSettings;
import org.workcraft.plugins.fsm.*;
import org.workcraft.plugins.fsm.converters.FsmToPetriConverter;
import org.workcraft.plugins.petri.*;
import org.workcraft.plugins.petri.tools.PetriSimulationTool;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

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
            if (label.isEmpty()) {
                label = VisualEvent.EPSILON_SYMBOL;
            }
        }
        if (label == null) {
            label = super.getTraceLabelByReference(ref);
        }
        return label;
    }

    @Override
    public void generateUnderlyingModel(WorkspaceEntry we) {
        converter = new FsmToPetriConverter(WorkspaceUtils.getAs(we, VisualFsm.class));
    }

    @Override
    public PetriModel getUnderlyingModel() {
        return converter.getDstModel().getMathModel();
    }

    @Override
    public VisualModel getUnderlyingVisualModel() {
        return converter.getDstModel();
    }

    @Override
    public void applySavedState(final GraphEditor editor) {
        if ((savedState == null) || savedState.isEmpty()) {
            return;
        }
        MathModel model = editor.getModel().getMathModel();
        if (model instanceof Fsm fsm) {
            editor.getWorkspaceEntry().saveMemento();
            for (State state: fsm.getStates()) {
                String ref = fsm.getNodeReference(state);
                Node underlyingNode = getUnderlyingModel().getNodeByReference(ref);
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
                executeUnderlyingNode(e.getEditor(), transition);
            }
        }
    }

    @Override
    public boolean isContainerExcited(VisualModel model, Container container) {
        if (excitedContainers.containsKey(container)) {
            return excitedContainers.get(container);
        }
        boolean ret = false;
        for (Node node: container.getChildren()) {
            if (node instanceof VisualEvent) {
                ret = ret || (getExcitedTransitionOfNode(node) != null);
            }
            if (node instanceof Container) {
                ret = ret || isContainerExcited(model, (Container) node);
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
        return node -> {
            if (converter == null) {
                return null;
            }
            VisualModel model = editor.getModel();
            if ((node instanceof VisualPage) || (node instanceof VisualGroup)) {
                return getContainerDecoration(model, (Container) node);
            }
            if (node instanceof VisualState) {
                return getStateDecoration((VisualState) node);
            }
            if (node instanceof VisualEvent) {
                return getEventDecoration((VisualEvent) node);
            }
            return null;
        };
    }

    public Decoration getEventDecoration(VisualEvent event) {
        Node transition = getCurrentUnderlyingNode();
        final boolean isExcited = getExcitedTransitionOfNode(event) != null;
        final boolean isSuggested = isExcited && converter.isRelated(event, transition);
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

    public Decoration getStateDecoration(VisualState state) {
        VisualPlace p = converter.getRelatedPlace(state);
        if (p == null) {
            return null;
        }
        final boolean isMarkedPlace = p.getReferencedComponent().getTokens() > 0;
        return new Decoration() {
            @Override
            public Color getColorisation() {
                return isMarkedPlace ? SimulationDecorationSettings.getExcitedComponentColor() : null;
            }
        };
    }

    private Transition getExcitedTransitionOfNode(Node node) {
        if (node instanceof VisualEvent event) {
            VisualTransition vTransition = converter.getRelatedTransition(event);
            if (vTransition != null) {
                Transition transition = vTransition.getReferencedComponent();
                if (isEnabledUnderlyingNode(transition)) {
                    return transition;
                }
            }
        }
        return null;
    }

}
