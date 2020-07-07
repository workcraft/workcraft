package org.workcraft.plugins.xmas;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.ShortName;
import org.workcraft.dom.Node;
import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.tools.CommentGeneratorTool;
import org.workcraft.gui.tools.NodeGeneratorTool;
import org.workcraft.plugins.xmas.components.*;
import org.workcraft.plugins.xmas.tools.SyncSelectionTool;
import org.workcraft.plugins.xmas.tools.XmasConnectionTool;
import org.workcraft.plugins.xmas.tools.XmasSimulationTool;
import org.workcraft.utils.Hierarchy;

import java.util.Collection;

@DisplayName("xMAS Circuit")
@ShortName("xMAS")
public class VisualXmas extends AbstractVisualModel {

    public VisualXmas(Xmas model) {
        this(model, null);
    }

    public VisualXmas(Xmas model, VisualGroup root) {
        super(model, root);
    }

    @Override
    public void registerGraphEditorTools() {
        addGraphEditorTool(new SyncSelectionTool());
        addGraphEditorTool(new CommentGeneratorTool());
        addGraphEditorTool(new XmasConnectionTool());
        addGraphEditorTool(new NodeGeneratorTool(new DefaultNodeGenerator(SourceComponent.class)));
        addGraphEditorTool(new NodeGeneratorTool(new DefaultNodeGenerator(SinkComponent.class)));
        addGraphEditorTool(new NodeGeneratorTool(new DefaultNodeGenerator(FunctionComponent.class)));
        addGraphEditorTool(new NodeGeneratorTool(new DefaultNodeGenerator(QueueComponent.class)));
        addGraphEditorTool(new NodeGeneratorTool(new DefaultNodeGenerator(ForkComponent.class)));
        addGraphEditorTool(new NodeGeneratorTool(new DefaultNodeGenerator(JoinComponent.class)));
        addGraphEditorTool(new NodeGeneratorTool(new DefaultNodeGenerator(SwitchComponent.class)));
        addGraphEditorTool(new NodeGeneratorTool(new DefaultNodeGenerator(MergeComponent.class)));
        addGraphEditorTool(new NodeGeneratorTool(new DefaultNodeGenerator(CreditComponent.class)));
        addGraphEditorTool(new NodeGeneratorTool(new DefaultNodeGenerator(SyncComponent.class)));
        addGraphEditorTool(new XmasSimulationTool());
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
            MathNode mFirst = getReferencedComponent(first);
            MathNode mSecond = getReferencedComponent(second);
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
