package org.workcraft.plugins.wtg;

import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Container;
import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.properties.ModelProperties;
import org.workcraft.gui.properties.PropertyHelper;
import org.workcraft.gui.tools.CommentGeneratorTool;
import org.workcraft.gui.tools.NodeGeneratorTool;
import org.workcraft.plugins.dtd.VisualDtd;
import org.workcraft.plugins.dtd.VisualSignal;
import org.workcraft.plugins.wtg.tools.SignalGeneratorTool;
import org.workcraft.plugins.wtg.tools.WtgConnectionTool;
import org.workcraft.plugins.wtg.tools.WtgSelectionTool;
import org.workcraft.plugins.wtg.tools.WtgSimulationTool;
import org.workcraft.utils.Hierarchy;

import java.util.ArrayList;
import java.util.Collection;

@DisplayName("Waveform Transition Graph")
public class VisualWtg extends VisualDtd {

    public VisualWtg(Wtg model) {
        this(model, null);
    }

    public VisualWtg(Wtg model, VisualGroup root) {
        super(model, root);
    }

    @Override
    public void registerGraphEditorTools() {
        addGraphEditorTool(new WtgSelectionTool());
        addGraphEditorTool(new CommentGeneratorTool());
        addGraphEditorTool(new WtgConnectionTool());
        addGraphEditorTool(new NodeGeneratorTool(new DefaultNodeGenerator(State.class), true));
        addGraphEditorTool(new NodeGeneratorTool(new DefaultNodeGenerator(Waveform.class), true));
        addGraphEditorTool(new SignalGeneratorTool());
        addGraphEditorTool(new WtgSimulationTool());
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
            if (container instanceof VisualWaveform) {
                VisualWaveform waveform = (VisualWaveform) container;
                properties.addAll(WtgPropertyHelper.getSignalDeclarationProperties(this, waveform));
                properties.removeByName(AbstractVisualModel.PROPERTY_TITLE);
            } else {
                properties.add(PropertyHelper.getSignalSectionProperty(this));
                properties.addAll(WtgPropertyHelper.getSignalProperties(this));
            }
        }
        return properties;
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
