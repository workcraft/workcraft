package org.workcraft.plugins.wtg;

import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.graph.generators.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.CommentGeneratorTool;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.plugins.dtd.VisualDtd;
import org.workcraft.plugins.dtd.VisualSignal;
import org.workcraft.plugins.dtd.VisualTransitionEvent;
import org.workcraft.plugins.wtg.properties.SignalNamePropertyDescriptor;
import org.workcraft.plugins.wtg.properties.SignalTypePropertyDescriptor;
import org.workcraft.plugins.wtg.tools.WtgConnectionTool;
import org.workcraft.plugins.wtg.tools.WtgSelectionTool;
import org.workcraft.plugins.wtg.tools.WtgSignalGeneratorTool;
import org.workcraft.plugins.wtg.tools.WtgSimulationTool;
import org.workcraft.util.Hierarchy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@DisplayName("Waveform Transition Graph")
public class VisualWtg extends VisualDtd {

    public VisualWtg(Wtg model) {
        this(model, null);
    }

    public VisualWtg(Wtg model, VisualGroup root) {
        super(model, root);
        setGraphEditorTools();
    }

    private void setGraphEditorTools() {
        List<GraphEditorTool> tools = new ArrayList<>();
        tools.add(new WtgSelectionTool());
        tools.add(new CommentGeneratorTool());
        tools.add(new WtgConnectionTool());
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(State.class), true));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(Waveform.class), true));
        tools.add(new WtgSignalGeneratorTool());
        tools.add(new WtgSimulationTool());
        setGraphEditorTools(tools);
    }

    @Override
    public void validateConnection(Node first, Node second) throws InvalidConnectionException {
        if (first == second) {
            throw new InvalidConnectionException("Self-loops are not allowed.");
        }

        if (getConnection(first, second) != null) {
            throw new InvalidConnectionException("Connection already exists.");
        }

        if ((first instanceof VisualWaveform) && (second instanceof VisualWaveform)) {
            throw new InvalidConnectionException("Cannot directly connect waveforms.");
        }

        if ((first instanceof VisualState) && (second instanceof VisualState)) {
            throw new InvalidConnectionException("Cannot directly connect states.");
        }

        if ((first instanceof VisualState) && (second instanceof VisualWaveform)) {
            if (!getPreset(second).isEmpty()) {
                throw new InvalidConnectionException("Waveform cannot have more than one preceding state.");
            }
            return;
        }

        if ((first instanceof VisualWaveform) && (second instanceof VisualState)) {
            if (!getPostset(first).isEmpty()) {
                throw new InvalidConnectionException("Waveform cannot have more than one succeeding state.");
            }
            return;
        }
        if ((first instanceof VisualTransitionEvent) && (second instanceof VisualTransitionEvent)) {
            VisualSignal firstSignal = ((VisualTransitionEvent) first).getVisualSignal();
            VisualSignal secondSignal = ((VisualTransitionEvent) second).getVisualSignal();
            Node firstWaveform = firstSignal.getParent();
            Node secondWaveform = secondSignal.getParent();
            if (firstWaveform != secondWaveform) {
                throw new InvalidConnectionException("Cannot connect events from different waveforms.");
            }
        }
        super.validateConnection(first, second);
    }

    public Collection<VisualState> getVisualStates() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualState.class);
    }

    public Collection<VisualWaveform> getVisualWaveforms() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualWaveform.class);
    }

    @Override
    public ModelProperties getProperties(Node node) {
        ModelProperties properties = super.getProperties(node);
        if (node == null) {
            Wtg wtg = (Wtg) getMathModel();
            for (String signalName : wtg.getSignalNames()) {
                SignalNamePropertyDescriptor symbolDescriptor = new SignalNamePropertyDescriptor(wtg, signalName);
                properties.insertOrderedByFirstWord(symbolDescriptor);
                SignalTypePropertyDescriptor typeDescriptor = new SignalTypePropertyDescriptor(wtg, signalName);
                properties.insertOrderedByFirstWord(typeDescriptor);
            }
        }
        return properties;
    }

}
