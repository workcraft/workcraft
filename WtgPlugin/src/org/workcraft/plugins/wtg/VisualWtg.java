package org.workcraft.plugins.wtg;

import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Container;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.graph.generators.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.CommentGeneratorTool;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.gui.properties.ModelProperties;
import org.workcraft.plugins.dtd.VisualDtd;
import org.workcraft.plugins.wtg.properties.SignalDeclarationPropertyDescriptor;
import org.workcraft.plugins.wtg.properties.SignalNamePropertyDescriptor;
import org.workcraft.plugins.wtg.properties.SignalTypePropertyDescriptor;
import org.workcraft.plugins.wtg.tools.WtgConnectionTool;
import org.workcraft.plugins.wtg.tools.WtgSelectionTool;
import org.workcraft.plugins.wtg.tools.WtgSignalGeneratorTool;
import org.workcraft.plugins.wtg.tools.WtgSimulationTool;
import org.workcraft.util.Hierarchy;

import java.util.*;

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
    public Wtg getMathModel() {
        return (Wtg) super.getMathModel();
    }

    public Collection<VisualState> getVisualStates() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualState.class);
    }

    public Collection<VisualWaveform> getVisualWaveforms() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualWaveform.class);
    }

    @Override
    public ModelProperties getProperties(VisualNode node) {
        ModelProperties properties = super.getProperties(node);
        if (node == null) {
            Container container = getCurrentLevel();
            VisualWaveform waveform = null;
            if (container instanceof VisualWaveform) {
                waveform = (VisualWaveform) container;
            }
            LinkedList<String> signalNames = new LinkedList<>(getMathModel().getSignalNames());
            signalNames.sort(Comparator.comparing(String::toString));
            for (String signalName : signalNames) {
                if (waveform != null) {
                    properties.insertOrderedByFirstWord(new SignalDeclarationPropertyDescriptor(this, waveform, signalName));
                }
                properties.insertOrderedByFirstWord(new SignalNamePropertyDescriptor(getMathModel(), signalName));
                properties.insertOrderedByFirstWord(new SignalTypePropertyDescriptor(getMathModel(), signalName));
            }
        }
        return properties;
    }

}
