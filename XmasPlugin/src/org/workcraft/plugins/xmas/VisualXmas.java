package org.workcraft.plugins.xmas;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.ShortName;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.graph.generators.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.CommentGeneratorTool;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.plugins.xmas.components.*;
import org.workcraft.plugins.xmas.tools.SyncSelectionTool;
import org.workcraft.plugins.xmas.tools.XmasConnectionTool;
import org.workcraft.plugins.xmas.tools.XmasSimulationTool;
import org.workcraft.util.Hierarchy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@DisplayName("xMAS Circuit")
@ShortName("xMAS")
public class VisualXmas extends AbstractVisualModel {

    public VisualXmas(Xmas model) {
        this(model, null);
    }

    public VisualXmas(Xmas model, VisualGroup root) {
        super(model, root);
        setGraphEditorTools();
    }

    private void setGraphEditorTools() {
        List<GraphEditorTool> tools = new ArrayList<>();
        tools.add(new SyncSelectionTool());
        tools.add(new CommentGeneratorTool());
        tools.add(new XmasConnectionTool());

        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(SourceComponent.class)));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(SinkComponent.class)));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(FunctionComponent.class)));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(QueueComponent.class)));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(ForkComponent.class)));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(JoinComponent.class)));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(SwitchComponent.class)));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(MergeComponent.class)));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(CreditComponent.class)));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(SyncComponent.class)));

        tools.add(new XmasSimulationTool());
        setGraphEditorTools(tools);
    }

    @Override
    public Xmas getMathModel() {
        return (Xmas) super.getMathModel();
    }

    @Override
    public VisualXmasConnection connect(VisualNode first, VisualNode second, MathConnection mConnection)
            throws InvalidConnectionException {

        validateConnection(first, second);
        if (mConnection == null) {
            MathNode mFirst = getMathReference(first);
            MathNode mSecond = getMathReference(second);
            mConnection = getMathModel().connect(mFirst, mSecond);
        }
        VisualXmasConnection vConnection = new VisualXmasConnection(mConnection, first, second);
        Node parent = Hierarchy.getCommonParent(first, second);
        VisualGroup container = Hierarchy.getNearestAncestor(parent, VisualGroup.class);
        container.add(vConnection);
        return vConnection;
    }

    public Collection<VisualXmasComponent> getNodes() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualXmasComponent.class);
    }

}
