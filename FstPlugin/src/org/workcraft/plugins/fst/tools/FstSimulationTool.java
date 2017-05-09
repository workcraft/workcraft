package org.workcraft.plugins.fst.tools;

import java.awt.Color;
import java.awt.event.MouseEvent;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
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
import org.workcraft.plugins.shared.CommonSimulationSettings;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.tools.StgSimulationTool;
import org.workcraft.util.Func;

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
            if (label == "") label = Character.toString(VisualEvent.EPSILON_SYMBOL);
        }
        if (label == null) {
            label = super.getTraceLabelByReference(ref);
        }
        return label;
    }

    @Override
    public void generateUnderlyingModel(VisualModel model) {
        final VisualFst fst = (VisualFst) model;
        final VisualStg stg = new VisualStg(new Stg());
        converter = new FstToStgConverter(fst, stg);
        setUnderlyingModel(converter.getDstModel());
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
                Node underlyingNode = getUnderlyingStg().getNodeByReference(ref);
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
            Node node = HitMan.hitDeepest(e.getPosition(), model.getRoot(),
                    new Func<Node, Boolean>() {
                        @Override
                        public Boolean eval(Node node) {
                            return getExcitedTransitionOfNode(node) != null;
                        }
                    });

            Transition transition = null;
            if (transition == null) {
                transition = getExcitedTransitionOfNode(node);
            }
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
        return "Click on a highlighted arc to trigger its signal event.";
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
                return isExcited ? CommonSimulationSettings.getExcitedComponentColor() : null;
            }

            @Override
            public Color getBackground() {
                return isSuggested ? CommonSimulationSettings.getSuggestedComponentColor() : null;
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
                return isMarkedPlace ? CommonSimulationSettings.getExcitedComponentColor() : null;
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
