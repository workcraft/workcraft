package org.workcraft.plugins.wtg.tools;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.dtd.VisualEvent;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.shared.CommonDecorationSettings;
import org.workcraft.plugins.stg.NamedTransition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.tools.StgSimulationTool;
import org.workcraft.plugins.wtg.State;
import org.workcraft.plugins.wtg.VisualState;
import org.workcraft.plugins.wtg.VisualWaveform;
import org.workcraft.plugins.wtg.Wtg;
import org.workcraft.plugins.wtg.converter.WtgToStgConverter;
import org.workcraft.plugins.wtg.decorations.StateDecoration;
import org.workcraft.plugins.wtg.decorations.WaveformDecoration;

import java.awt.*;
import java.awt.event.MouseEvent;

public class WtgSimulationTool extends StgSimulationTool {
    private WtgToStgConverter converter;

    @Override
    public void activated(final GraphEditor editor) {
        super.activated(editor);
        setStatePaneVisibility(true);
    }

    @Override
    public void generateUnderlyingModel(VisualModel model) {
        final Wtg wtg = (Wtg) model.getMathModel();
        final Stg stg = new Stg();
        converter = new WtgToStgConverter(wtg, stg);
        setUnderlyingModel(new VisualStg(converter.getDstModel()));
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
    public void applySavedState(final GraphEditor editor) {
        if ((savedState == null) || savedState.isEmpty()) {
            return;
        }
        MathModel model = editor.getModel().getMathModel();
        if (model instanceof Wtg) {
            editor.getWorkspaceEntry().saveMemento();
            Wtg wtg = (Wtg) model;
            for (State state : wtg.getStates()) {
                String ref = wtg.getNodeReference(state);
                Node underlyingNode = getUnderlyingStg().getNodeByReference(ref);
                if ((underlyingNode instanceof StgPlace) && savedState.containsKey(underlyingNode)
                        && (savedState.get(underlyingNode) > 0)) {
                    state.setInitial(true);
                    return;
                }
            }
        }
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return "Click on a highlighted event to trigger it.";
    }

    @Override
    public Decorator getDecorator(final GraphEditor editor) {
        return node -> {
            if (converter == null) {
                return null;
            }
            if (node instanceof VisualState) {
                return getStateDecoration((VisualState) node);
            }
            if (node instanceof VisualWaveform) {
                return getWaveformDecoration((VisualWaveform) node);
            }
            if (node instanceof VisualEvent) {
                return getEventDecoration((VisualEvent) node);
            }
            return null;
        };
    }

    public Decoration getStateDecoration(VisualState state) {
        StgPlace p = converter.getRelatedPlace(state.getReferencedState());
        if (p == null) {
            return null;
        }
        return p.getTokens() > 0 ? StateDecoration.Marked.INSTANCE : StateDecoration.Unmarked.INSTANCE;
    }

    private Decoration getWaveformDecoration(VisualWaveform waveform) {
        boolean isWaveformActive = !isInNodalState() && isContainerExcited(waveform);
        return isWaveformActive ? WaveformDecoration.Active.INSTANCE : WaveformDecoration.Inactive.INSTANCE;
    }

    private boolean isInNodalState() {
        Wtg wtg = converter.getSrcModel();
        for (State state : wtg.getStates()) {
            StgPlace place = converter.getRelatedPlace(state);
            if ((place != null) && (place.getTokens() > 0)) {
                return true;
            }
        }
        return false;
    }

    public Decoration getEventDecoration(VisualEvent event) {
        MathNode transition = getTraceCurrentNode();
        final boolean isExcited = getExcitedTransitionOfNode(event) != null;
        final boolean isSuggested = isExcited && converter.isRelated(event.getReferencedSignalEvent(), transition);
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

    private Transition getExcitedTransitionOfNode(Node node) {
        if (node instanceof VisualEvent) {
            VisualEvent event = (VisualEvent) node;
            NamedTransition transition = converter.getRelatedTransition(event.getReferencedSignalEvent());
            if (transition != null) {
                if (isEnabledNode(transition)) {
                    return transition;
                }
            }
        }
        return null;
    }

}
