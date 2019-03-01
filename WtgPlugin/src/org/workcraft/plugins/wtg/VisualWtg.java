package org.workcraft.plugins.wtg;

import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Container;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.gui.tools.CommentGeneratorTool;
import org.workcraft.gui.tools.GraphEditorTool;
import org.workcraft.gui.tools.NodeGeneratorTool;
import org.workcraft.gui.properties.ModelProperties;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.dtd.Signal;
import org.workcraft.plugins.dtd.VisualDtd;
import org.workcraft.plugins.dtd.VisualSignal;
import org.workcraft.plugins.wtg.properties.SignalDeclarationPropertyDescriptor;
import org.workcraft.plugins.wtg.tools.WtgConnectionTool;
import org.workcraft.plugins.wtg.tools.WtgSelectionTool;
import org.workcraft.plugins.wtg.tools.WtgSignalGeneratorTool;
import org.workcraft.plugins.wtg.tools.WtgSimulationTool;
import org.workcraft.plugins.wtg.utils.WtgUtils;
import org.workcraft.utils.Hierarchy;

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
            LinkedList<String> signalNames = new LinkedList<>(getMathModel().getSignalNames());
            signalNames.sort(Comparator.comparing(String::toString));
            Container container = getCurrentLevel();
            if (container instanceof VisualWaveform) {
                VisualWaveform waveform = (VisualWaveform) container;
                for (String signalName : signalNames) {
                    properties.insertOrderedByFirstWord(new SignalDeclarationPropertyDescriptor(this, waveform, signalName));
                }
                properties.removeByName(AbstractVisualModel.PROPERTY_TITLE);
            } else {
                for (String signalName : signalNames) {
                    properties.insertOrderedByFirstWord(getSignalNameProperty(signalName));
                    properties.insertOrderedByFirstWord(getSignalTypeProperty(signalName));
                }
            }
        }
        return properties;
    }

    private PropertyDescriptor getSignalNameProperty(String signalName) {
        return new PropertyDeclaration<VisualWtg, String>(
                this, signalName + " name", String.class) {
            @Override
            public String getter(VisualWtg object) {
                return signalName;
            }
            @Override
            public void setter(VisualWtg object, String value) {
                WtgUtils.renameSignal(getMathModel(), signalName, value);
            }
        };
    }

    private PropertyDescriptor getSignalTypeProperty(String signalName) {
        return new PropertyDeclaration<VisualWtg, Signal.Type>(
                this, signalName + " type", Signal.Type.class) {
            @Override
            public Signal.Type getter(VisualWtg object) {
                Wtg wtg = getMathModel();
                for (Signal signal : wtg.getSignals()) {
                    if (!signalName.equals(wtg.getName(signal))) continue;
                    return signal.getType();
                }
                return null;
            }
            @Override
            public void setter(VisualWtg object, Signal.Type value) {
                Wtg wtg = getMathModel();
                for (Signal signal : wtg.getSignals()) {
                    if (!signalName.equals(wtg.getName(signal))) continue;
                    signal.setType(value);
                }
            }
        };
    }

    @Override
    public void afterPaste() {
        super.afterPaste();
        Collection<VisualNode> selection = new ArrayList<>(getSelection());
        for (VisualNode node : selection) {
            if (node.getParent() == getRoot()) {
                if (node instanceof VisualSignal) {
                    remove(node);
                }
            } else {
                if ((node instanceof VisualState) || (node instanceof VisualWaveform)) {
                    remove(node);
                }
            }
        }
    }

}
