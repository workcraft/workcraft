package org.workcraft.plugins.wtg.tools;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.gui.tools.Decorator;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.plugins.builtin.settings.SimulationDecorationSettings;
import org.workcraft.plugins.dtd.*;
import org.workcraft.plugins.dtd.utils.DtdUtils;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.NamedTransition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.plugins.stg.tools.StgSimulationTool;
import org.workcraft.plugins.wtg.*;
import org.workcraft.plugins.wtg.converter.WtgToStgConverter;
import org.workcraft.plugins.wtg.decorations.StateDecoration;
import org.workcraft.plugins.wtg.decorations.WaveformDecoration;
import org.workcraft.plugins.wtg.utils.VerificationUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class WtgSimulationTool extends StgSimulationTool {

    private WtgToStgConverter converter;

    @Override
    public boolean checkPrerequisites(final GraphEditor editor) {
        final Wtg wtg = (Wtg) editor.getModel().getMathModel();
        return VerificationUtils.checkStructure(wtg) && VerificationUtils.checkNameCollisions(wtg);
    }

    @Override
    public void activated(final GraphEditor editor) {
        super.activated(editor);
        setStatePaneVisibility(true);
    }

    @Override
    public void generateUnderlyingModel(WorkspaceEntry we) {
        final Wtg wtg = WorkspaceUtils.getAs(we, Wtg.class);
        if (VerificationUtils.checkStructure(wtg) && VerificationUtils.checkNameCollisions(wtg)) {
            converter = new WtgToStgConverter(wtg);
        }
    }

    @Override
    public Stg getUnderlyingModel() {
        return converter.getDstModel();
    }

    @Override
    public VisualModel getUnderlyingVisualModel() {
        return null;
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            Point2D pos = e.getPosition();
            VisualModel model = e.getModel();
            Node deepestNode = HitMan.hitDeepest(pos, model.getRoot(), node -> node instanceof VisualSignal);

            Transition transition = null;
            if (deepestNode instanceof VisualSignal) {
                transition = getExcitedTransitionOfSignal(model, (VisualSignal) deepestNode);
            } else if (deepestNode instanceof VisualEvent event) {
                transition = getExcitedTransitionOfEvent(event);
                if (transition == null) {
                    transition = getExcitedTransitionOfSignal(model, event.getVisualSignal());
                }
            }

            if (transition != null) {
                executeUnderlyingNode(e.getEditor(), transition);
            }
        }
    }

    @Override
    public boolean isContainerExcited(VisualModel model, Container container) {
        if (container instanceof VisualWaveform) {
            Waveform waveform = ((VisualWaveform) container).getReferencedComponent();
            return converter.isActiveWaveform(waveform);
        }
        return false;
    }

    @Override
    public void applySavedState(final GraphEditor editor) {
        if ((savedState == null) || savedState.isEmpty()) {
            return;
        }
        MathModel model = editor.getModel().getMathModel();
        if (model instanceof Wtg wtg) {
            editor.getWorkspaceEntry().saveMemento();
            for (State state : wtg.getStates()) {
                String ref = wtg.getNodeReference(state);
                Node underlyingNode = getUnderlyingModel().getNodeByReference(ref);
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
                return getWaveformDecoration(editor.getModel(), (VisualWaveform) node);
            }
            if (node instanceof VisualEvent) {
                return getEventDecoration((VisualEvent) node);
            }
            if (node instanceof VisualLevelConnection) {
                return getLevelDecoration(editor.getModel(), (VisualLevelConnection) node);
            }
            return null;
        };
    }

    private Decoration getStateDecoration(VisualState state) {
        StgPlace p = converter.getRelatedPlace(state.getReferencedComponent());
        if (p == null) {
            return null;
        }
        return p.getTokens() > 0 ? StateDecoration.Marked.INSTANCE : StateDecoration.Unmarked.INSTANCE;
    }

    private Decoration getWaveformDecoration(VisualModel model, VisualWaveform waveform) {
        boolean isWaveformActive = !isInNodalState() && isContainerExcited(model, waveform);
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

    private Decoration getEventDecoration(VisualEvent event) {
        MathNode transition = getCurrentUnderlyingNode();
        final boolean isExcited = getExcitedTransitionOfEvent(event) != null;
        final boolean isSuggested = isExcited && converter.isRelated(event.getReferencedComponent(), transition);
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

    private Decoration getLevelDecoration(VisualModel model, VisualLevelConnection level) {
        MathNode transition = getCurrentUnderlyingNode();
        VisualEvent firstEvent = (VisualEvent) level.getFirst();
        VisualEvent secondEvent = (VisualEvent) level.getSecond();
        VisualSignal signal = secondEvent.getVisualSignal();

        Signal.State state = DtdUtils.getNextState(firstEvent.getReferencedComponent());
        NamedTransition enabledUnstableTransition = converter.getEnabledUnstableTransition(signal.getReferencedComponent());
        boolean isEnabledUnstable = (state == Signal.State.UNSTABLE) && (enabledUnstableTransition != null);
        boolean isExcitedWaveform = isContainerExcited(model, getWaveform(signal));

        final boolean isExcited = isEnabledUnstable && isExcitedWaveform;
        final boolean isSuggested = isExcited && (enabledUnstableTransition == transition);
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

    private VisualWaveform getWaveform(VisualSignal signal) {
        if (signal != null) {
            Node parent = signal.getParent();
            if (parent instanceof VisualWaveform) {
                return (VisualWaveform) parent;
            }
        }
        return null;
    }

    private Transition getExcitedTransitionOfEvent(VisualEvent event) {
        if (event != null) {
            NamedTransition transition = converter.getRelatedTransition(event.getReferencedComponent());
            if ((transition != null) && isEnabledUnderlyingNode(transition)) {
                return transition;
            }
        }
        return null;
    }

    private Transition getExcitedTransitionOfSignal(VisualModel model, VisualSignal signal) {
        Transition result = null;
        VisualWaveform waveform = getWaveform(signal);
        if ((waveform != null) && isContainerExcited(model, waveform)) {
            result = converter.getEnabledUnstableTransition(signal.getReferencedComponent());
            if (result == null) {
                result = getExcitedTransitionOfEvent(signal.getVisualSignalEntry());
            }
            if (result == null) {
                for (VisualTransitionEvent transition : signal.getVisualTransitions()) {
                    result = getExcitedTransitionOfEvent(transition);
                    if (result != null) break;
                }
            }
            if (result == null) {
                result = getExcitedTransitionOfEvent(signal.getVisualSignalExit());
            }
        }
        return result;
    }

}
