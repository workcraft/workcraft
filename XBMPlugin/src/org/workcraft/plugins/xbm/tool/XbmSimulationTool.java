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
import org.workcraft.plugins.fsm.*;
import org.workcraft.plugins.petri.*;
import org.workcraft.plugins.petri.tools.PetriSimulationTool;
import org.workcraft.plugins.xbm.*;
import org.workcraft.plugins.xbm.converters.ElementaryCycle;
import org.workcraft.plugins.xbm.converters.XbmToPetriConverter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class XbmSimulationTool extends PetriSimulationTool {

    private XbmToPetriConverter converter;
    private final Map<Signal, Boolean> conditionalValue = new HashMap<>();
    private final Set<JCheckBox> conditionalCheckBoxes = new LinkedHashSet<>();

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
        final VisualXbm xbm = (VisualXbm) model;
        final VisualPetri petri = new VisualPetri(new Petri());
        converter = new XbmToPetriConverter(xbm, petri);
        if (!conditionalValue.isEmpty()) conditionalValue.clear();
        for (Signal signal: xbm.getMathModel().getSignals(Signal.Type.CONDITIONAL)) {
            conditionalValue.put(signal, false);
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
    public String getHintText(final GraphEditor editor) {
        return "Click on a highlighted arc to trigger its event.";
    }

    //TODO Revamp this code and possibly make the elementary cycle as a separate class
    @Override
    public void updateState(GraphEditor editor) {
        super.updateState(editor);

        for (Signal signal: conditionalValue.keySet()) {
            ElementaryCycle elemCycle = converter.getRelatedElementaryCycle(signal);
            for (JCheckBox checkBox: conditionalCheckBoxes) {
                VisualPlace placeLow = elemCycle.getLow();
                VisualPlace placeHigh = elemCycle.getHigh();

                if (placeLow.getReferencedPlace().getTokens() > 0 && placeHigh.getReferencedPlace().getTokens() <= 0) {
                    checkBox.setSelected(false);
                }
                else if (placeLow.getReferencedPlace().getTokens() <= 0 && placeHigh.getReferencedPlace().getTokens() > 0) {
                    checkBox.setSelected(true);
                }
            }
        }
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

    private Decoration getEventDecoration(VisualBurstEvent event) {
        Node transition = getTraceCurrentNode();
        final boolean isExcited = getExcitedTransitionOfNode(event) != null ;
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

    public Decoration getStateDecoration(VisualXbmState state) {
        VisualPlace p = converter.getRelatedPlace(state);
        if (p == null) {
            return null;
        }
        final boolean isMarkedPlace = p.getReferencedPlace().getTokens() > 0;

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
        if ((node != null) && (node instanceof VisualBurstEvent)) {
            VisualTransition vTransition = converter.getRelatedTransition((VisualBurstEvent) node);
            if (vTransition != null) {
                if (isEnabledNode(vTransition.getReferencedTransition())) {
                    return vTransition.getReferencedTransition();
                }
            }
        }
        return null;
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

    private JPanel createConditionalSignalSetters(GraphEditor editor) {
        JPanel conditionalSetterTools = new JPanel();
        conditionalSetterTools.setLayout(new GridLayout(conditionalValue.keySet().size(), 1));
        for (Map.Entry<Signal, Boolean> entry: conditionalValue.entrySet()) {
            JPanel signalEntry = new JPanel(new GridLayout(1,2));
            JLabel name = new JLabel(entry.getKey().getName(), SwingConstants.CENTER);
            JCheckBox value = new JCheckBox();
            value.setHorizontalAlignment(SwingConstants.CENTER);
            value.addActionListener(event -> {
                ElementaryCycle elemCycle = converter.getRelatedElementaryCycle(entry.getKey());
                if (value.isSelected()) {
                    conditionalValue.put(entry.getKey(), true);
                    VisualTransition transition = elemCycle.getRising();
                    fireElementaryCycleTransition(editor, transition);
                }
                else {
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

    private final void fireElementaryCycleTransition(GraphEditor editor, VisualTransition transition) {
        if (super.isEnabledNode(transition.getReferencedTransition())) {
            executeTransition(editor, transition.getReferencedTransition());
            editor.requestFocus();
        }
    }
}
