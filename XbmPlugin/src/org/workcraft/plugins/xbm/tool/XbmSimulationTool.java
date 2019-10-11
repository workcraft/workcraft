package org.workcraft.plugins.xbm.tool;

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
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.tools.StgSimulationTool;
import org.workcraft.plugins.xbm.*;
import org.workcraft.plugins.xbm.converters.ElementaryCycle;
import org.workcraft.plugins.xbm.converters.VisualBurstTransition;
import org.workcraft.plugins.xbm.converters.XbmToStgConverter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class XbmSimulationTool extends StgSimulationTool {

    private XbmToStgConverter converter;
    private final Map<XbmSignal, Boolean> conditionalValue = new HashMap<>();
    private final Set<JCheckBox> conditionalCheckBoxes = new LinkedHashSet<>();

    private static final String CHECKBOX_NAME_PREFIX = "checkBox";

    @Override
    public void generateUnderlyingModel(VisualModel model) {
        final VisualXbm xbm  = (VisualXbm) model;
        final VisualStg stg = new VisualStg(new Stg());
        converter = new XbmToStgConverter(xbm, stg);
        if (!conditionalValue.isEmpty()) conditionalValue.clear();
        for (XbmSignal xbmSignal : xbm.getMathModel().getSignals(XbmSignal.Type.CONDITIONAL)) {
            conditionalValue.put(xbmSignal, false);
        }
        setUnderlyingModel(converter.getDstModel());
    }

    @Override
    public void applySavedState(final GraphEditor editor) {
        if ((savedState == null) || savedState.isEmpty()) {
            return;
        }
        MathModel model = editor.getModel().getMathModel();
        if (model instanceof Xbm) {
            editor.getWorkspaceEntry().saveMemento();
            Xbm xbm = (Xbm) model;
            for (XbmState state: xbm.getXbmStates()) {
                String ref = xbm.getNodeReference(state);
                Node underlyingNode = getUnderlyingPetri().getNodeByReference(ref);
                if ((underlyingNode instanceof Place) && savedState.containsKey(underlyingNode)) {
                    boolean isInitial = savedState.get(underlyingNode) > 0;
                    state.setInitial(isInitial);
                }
            }
        }
    }

    @Override
    public void updateState(GraphEditor editor) {
        super.updateState(editor);
        for (XbmSignal xbmSignal : conditionalValue.keySet()) {
            ElementaryCycle elemCycle = converter.getRelatedElementaryCycle(xbmSignal);
            for (JCheckBox checkBox: conditionalCheckBoxes) {
                if (checkBox.getName().equals(xbmSignal.getName() + CHECKBOX_NAME_PREFIX)) {
                    VisualPlace placeLow = elemCycle.getLow();
                    VisualPlace placeHigh = elemCycle.getHigh();
                    if (placeLow.getReferencedComponent().getTokens() > 0 && placeHigh.getReferencedComponent().getTokens() <= 0) {
                        checkBox.setSelected(false);
                    } else if (placeLow.getReferencedComponent().getTokens() <= 0 && placeHigh.getReferencedComponent().getTokens() > 0) {
                        checkBox.setSelected(true);
                    }
                }
            }
        }
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return "Click on a highlighted arc to trigger its event.";
    }

    @Override
    public Decorator getDecorator(final GraphEditor editor) {
        return node -> {
            if (converter == null) return null;
            if (node instanceof VisualXbmState) {
                return getStateDecoration((VisualXbmState) node);
            } else if (node instanceof VisualBurstEvent) {
                return getEventDecoration((VisualBurstEvent) node);
            } else if (node instanceof VisualPage || node instanceof VisualGroup) {
                return getContainerDecoration((Container) node);
            }
            return null;
        };
    }

    @Override
    public JPanel getControlsPanel(GraphEditor editor) {
        JPanel fullPanel = new JPanel();
        fullPanel.setLayout(new BorderLayout());
        fullPanel.add(super.getControlsPanel(editor), BorderLayout.CENTER);
        if (!conditionalValue.isEmpty()) {
            fullPanel.add(createConditionalSignalSetters(editor), BorderLayout.SOUTH);
        }
        return fullPanel;
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            VisualModel model = e.getModel();
            Node deepestNode = HitMan.hitDeepest(e.getPosition(), model.getRoot(),
                    node -> getExcitedTransitionOfNode(node) != null);

            //TODO Add VisualBurstTransition here
            if (deepestNode instanceof VisualBurstEvent) {
                Set<Transition> transitions = getExcitedTransitionPathOfNode(deepestNode);
                for (Transition transition: transitions) {
                    if (transition != null) {
                        executeTransition(e.getEditor(), transition);
                    }
                }
            }
        }
    }

    public Decoration getStateDecoration(VisualXbmState state) {
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

    private Decoration getEventDecoration(VisualBurstEvent event) {
        Node transition = getTraceCurrentNode();
        Set<Transition> transitions = getExcitedTransitionPathOfNode(event);
        final boolean isExcited = !transitions.isEmpty();
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

    private Set<Transition> getExcitedTransitionPathOfNode(Node node) {
        Set<Transition> result = new LinkedHashSet<>();
        if ((node != null) && (node instanceof VisualBurstEvent)) {
            VisualBurstTransition vBurstTransition = converter.getRelatedSignalBurstTransition((VisualBurstEvent) node);
            if (vBurstTransition != null) {
                if (isEnabledNode(vBurstTransition.getStart().getReferencedTransition())) {
                    result.add(vBurstTransition.getStart().getReferencedTransition()); //FORK
                    for (VisualTransition visualTransition: vBurstTransition.getInputTransitions()) {
                        result.add(visualTransition.getReferencedTransition());
                    }
                    result.add(vBurstTransition.getSplit().getReferencedTransition()); //JOIN_FORK
                    for (VisualTransition visualTransition: vBurstTransition.getOutputTransitions()) {
                        result.add(visualTransition.getReferencedTransition());
                    }
                    result.add(vBurstTransition.getEnd().getReferencedTransition()); //JOIN
                }
            }
        }
        return result;
    }

    private Transition getExcitedTransitionOfNode(Node node) {
        if ((node != null) && (node instanceof VisualBurstEvent)) {
            VisualBurstTransition vBurstTransition = converter.getRelatedSignalBurstTransition((VisualBurstEvent) node);
            if (vBurstTransition != null && vBurstTransition.getStart() != null) {
                if (isEnabledNode(vBurstTransition.getStart().getReferencedTransition())) {
                    return vBurstTransition.getStart().getReferencedTransition();
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    private JPanel createConditionalSignalSetters(GraphEditor editor) {
        if (!conditionalCheckBoxes.isEmpty()) conditionalCheckBoxes.clear();
        JPanel conditionalSetterTools = new JPanel();
        conditionalSetterTools.setLayout(new GridLayout(conditionalValue.keySet().size(), 1));
        for (Map.Entry<XbmSignal, Boolean> entry: conditionalValue.entrySet()) {
            JPanel signalEntry = new JPanel(new GridLayout(1, 2));
            JLabel name = new JLabel(entry.getKey().getName(), SwingConstants.CENTER);
            JCheckBox value = new JCheckBox();
            value.setName(entry.getKey().getName() + CHECKBOX_NAME_PREFIX);
            value.setHorizontalAlignment(SwingConstants.CENTER);
            value.addActionListener(event -> {
                ElementaryCycle elemCycle = converter.getRelatedElementaryCycle(entry.getKey());
                if (value.isSelected()) {
                    conditionalValue.put(entry.getKey(), true);
                    VisualTransition transition = elemCycle.getRising();
                    fireElementaryCycleTransition(editor, transition);
                } else {
                    conditionalValue.put(entry.getKey(), false);
                    VisualTransition transition = elemCycle.getFalling();
                    fireElementaryCycleTransition(editor, transition);
                }
            });
            signalEntry.add(name);
            signalEntry.add(value);
            conditionalSetterTools.add(signalEntry);
            conditionalCheckBoxes.add(value);
        }
        return conditionalSetterTools;
    }

    private void fireElementaryCycleTransition(GraphEditor editor, VisualTransition transition) {
        if (super.isEnabledNode(transition.getReferencedComponent())) {
            executeTransition(editor, transition.getReferencedComponent());
            editor.requestFocus();
        }
    }
}
