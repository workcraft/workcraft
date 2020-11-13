package org.workcraft.plugins.dfs;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.ShortName;
import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.tools.CommentGeneratorTool;
import org.workcraft.gui.tools.ConnectionTool;
import org.workcraft.gui.tools.NodeGeneratorTool;
import org.workcraft.plugins.dfs.tools.CycleAnalyserTool;
import org.workcraft.plugins.dfs.tools.DfsSelectionTool;
import org.workcraft.plugins.dfs.tools.DfsSimulationTool;
import org.workcraft.utils.Hierarchy;

import java.util.Collection;
import java.util.Set;

@DisplayName("Dataflow Structure")
@ShortName("DFS")
public class VisualDfs extends AbstractVisualModel {

    public VisualDfs(Dfs model) {
        this(model, null);
    }

    public VisualDfs(Dfs model, VisualGroup root) {
        super(model, root);
    }

    @Override
    public void registerGraphEditorTools() {
        addGraphEditorTool(new DfsSelectionTool());
        addGraphEditorTool(new CommentGeneratorTool());
        addGraphEditorTool(new ConnectionTool());
        addGraphEditorTool(new NodeGeneratorTool(new DefaultNodeGenerator(Logic.class)));
        addGraphEditorTool(new NodeGeneratorTool(new DefaultNodeGenerator(Register.class)));
        addGraphEditorTool(new NodeGeneratorTool(new DefaultNodeGenerator(ControlRegister.class)));
        addGraphEditorTool(new NodeGeneratorTool(new DefaultNodeGenerator(PushRegister.class)));
        addGraphEditorTool(new NodeGeneratorTool(new DefaultNodeGenerator(PopRegister.class)));
        addGraphEditorTool(new NodeGeneratorTool(new DefaultNodeGenerator(CounterflowLogic.class)));
        addGraphEditorTool(new NodeGeneratorTool(new DefaultNodeGenerator(CounterflowRegister.class)));
        addGraphEditorTool(new CycleAnalyserTool());
        addGraphEditorTool(new DfsSimulationTool());
    }

    @Override
    public Dfs getMathModel() {
        return (Dfs) super.getMathModel();
    }

    @Override
    public VisualConnection connect(VisualNode first, VisualNode second, MathConnection mConnection)
            throws InvalidConnectionException {

        validateConnection(first, second);
        VisualComponent c1 = (VisualComponent) first;
        VisualComponent c2 = (VisualComponent) second;
        MathNode ref1 = c1.getReferencedComponent();
        MathNode ref2 = c2.getReferencedComponent();
        VisualConnection result;
        if (first instanceof VisualControlRegister) {
            if (mConnection == null) {
                mConnection = getMathModel().controlConnect(ref1, ref2);
            }
            result = new VisualControlConnection((ControlConnection) mConnection, c1, c2);
        } else {
            if (mConnection == null) {
                mConnection = getMathModel().connect(ref1, ref2);
            }
            result = new VisualConnection(mConnection, c1, c2);
        }
        Hierarchy.getNearestContainer(c1, c2).add(result);
        return result;
    }

    public <T> Set<T> getRPreset(VisualNode node, Class<T> type) {
        return getPreset(node, type, arg -> (arg instanceof VisualLogic) || (arg instanceof VisualCounterflowLogic));
    }

    public <T> Set<T> getRPostset(VisualNode node, Class<T> type) {
        return getPostset(node, type, arg -> (arg instanceof VisualLogic) || (arg instanceof VisualCounterflowLogic));
    }

    public Collection<VisualLogic> getVisualLogics() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualLogic.class);
    }

    public Collection<VisualRegister> getVisualRegisters() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualRegister.class);
    }

    public Collection<VisualCounterflowLogic> getVisualCounterflowLogics() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualCounterflowLogic.class);
    }

    public Collection<VisualCounterflowRegister> getVisualCounterflowRegisters() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualCounterflowRegister.class);
    }

    public Collection<VisualControlRegister> getVisualControlRegisters() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualControlRegister.class);
    }

    public Collection<VisualPushRegister> getVisualPushRegisters() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualPushRegister.class);
    }

    public Collection<VisualPopRegister> getVisualPopRegisters() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualPopRegister.class);
    }

}
