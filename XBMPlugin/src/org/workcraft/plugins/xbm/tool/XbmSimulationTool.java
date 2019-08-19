package org.workcraft.plugins.xbm.tool;

import javafx.geometry.HorizontalDirection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.layouts.WrapLayout;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.gui.tools.Decorator;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.plugins.builtin.settings.SimulationDecorationSettings;
import org.workcraft.plugins.fsm.*;
import org.workcraft.plugins.petri.*;
import org.workcraft.plugins.petri.tools.PetriSimulationTool;
import org.workcraft.plugins.xbm.*;
import org.workcraft.plugins.xbm.converters.XbmToPetriConverter;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

//FIXME After running the tool, the states no longer retain any encoding
public class XbmSimulationTool extends PetriSimulationTool {

    private XbmToPetriConverter converter;
    private final Map<Signal, Boolean> conditionalValue = new HashMap<>();

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
                if (node instanceof VisualXbmState) {
                    return getStateDecoration((VisualXbmState) node);
                } else if (node instanceof VisualBurstEvent) {
                    return getEventDecoration((VisualBurstEvent) node);
                } else if (node instanceof VisualPage || node instanceof VisualGroup) {
                    return getContainerDecoration((Container) node);
                }
                return null;
            }
        };
    }

    public Decoration getEventDecoration(VisualBurstEvent event) {
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
                if (isEnabledNode(vTransition)) {
                    return vTransition.getReferencedTransition();
                }
            }
        }
        return null;
    }

    @Override
    public boolean isEnabledNode(Node node) {
        if (node instanceof VisualTransition) {
            VisualTransition vTransition = (VisualTransition) node;
            VisualBurstEvent vBurstEvent = converter.getRelatedEvent(vTransition);
            boolean isEnabledTransition = super.isEnabledNode(vTransition.getReferencedTransition());
            boolean satisfiesConditional = true;
            if (isEnabledTransition && vBurstEvent.getReferencedBurstEvent().hasConditional()) {
                Conditional conditional = vBurstEvent.getReferencedBurstEvent().getConditionalMapping();
                for (Map.Entry<String, Boolean> entryConditional: conditional.entrySet()) {
                    String name = entryConditional.getKey();
                    boolean expectedSigVal = entryConditional.getValue();
                    for (Map.Entry<Signal, Boolean> entryCondSimVal: conditionalValue.entrySet()) {

                        if (entryCondSimVal.getKey().getName().equals(name)) {
                            if (expectedSigVal) {
                                satisfiesConditional = satisfiesConditional && entryCondSimVal.getValue();
                            }
                            else {
                                satisfiesConditional = satisfiesConditional && !entryCondSimVal.getValue();
                            }
                        }
                    }
                }
            }
            return isEnabledTransition && satisfiesConditional; //result
        }
        else return super.isEnabledNode(node);
    }

    //FIXME Missing entries when adding new signals that are conditionals
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

        //Adjust tools to include toggleable value
        for (Map.Entry<Signal, Boolean> entry: conditionalValue.entrySet()) {

            JPanel signalEntry = new JPanel();
            signalEntry.setLayout(new GridLayout(1,2));
            JLabel name = new JLabel(entry.getKey().getName());
            name.setHorizontalAlignment(SwingConstants.CENTER);
            JCheckBox value = new JCheckBox();
            value.setHorizontalAlignment(SwingConstants.CENTER);
            value.addChangeListener(event -> {
                if (value.isSelected()) {
                    conditionalValue.put(entry.getKey(), true);
                }
                else {
                    conditionalValue.put(entry.getKey(), false);
                }
                editor.requestFocus();
            });
            signalEntry.add(name);
            signalEntry.add(value);

            conditionalSetterTools.add(signalEntry);
        }
        return conditionalSetterTools;
    }
}
