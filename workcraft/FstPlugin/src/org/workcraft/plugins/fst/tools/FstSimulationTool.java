package org.workcraft.plugins.fst.tools;

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
import org.workcraft.plugins.fsm.State;
import org.workcraft.plugins.fsm.VisualEvent;
import org.workcraft.plugins.fsm.VisualState;
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.plugins.fst.converters.FstToStgConverter;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.tools.StgSimulationTool;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.awt.*;
import java.awt.event.MouseEvent;

public class FstSimulationTool extends StgSimulationTool {

    private FstToStgConverter converter;

    @Override
    public void activated(final GraphEditor editor) {
        super.activated(editor);
        setStatePaneVisibility(true);
    }

    @Override
    public String getTraceLabelByReference(String ref) {
        String label = null;
        if (ref != null) {
            label = converter.getEventLabel(ref);
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
        converter = new FstToStgConverter(WorkspaceUtils.getAs(we, VisualFst.class));
    }

    @Override
    public Stg getUnderlyingModel() {
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
        if (model instanceof Fst) {
            editor.getWorkspaceEntry().saveMemento();
            Fst fst = (Fst) model;
            for (State state: fst.getStates()) {
                String ref = fst.getNodeReference(state);
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
        boolean result = false;
        for (Node node: container.getChildren()) {
            if (node instanceof VisualEvent) {
                result = result || (getExcitedTransitionOfNode(node) != null);
            }
            if (node instanceof Container) {
                result = result || isContainerExcited(model, (Container) node);
            }
            if (result) break;
        }
        excitedContainers.put(container, result);
        return result;
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return "Click on a highlighted arc to trigger its signal event.";
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
                Transition transition = vTransition.getReferencedComponent();
                if (isEnabledUnderlyingNode(transition)) {
                    return transition;
                }
            }
        }
        return null;
    }

}
