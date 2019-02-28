package org.workcraft.plugins.dfs;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.ShortName;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.gui.tools.*;
import org.workcraft.plugins.dfs.tools.CycleAnalyserTool;
import org.workcraft.plugins.dfs.tools.DfsSimulationTool;
import org.workcraft.utils.Hierarchy;

import java.util.*;

@DisplayName("Dataflow Structure")
@ShortName("DFS")
public class VisualDfs extends AbstractVisualModel {

    public VisualDfs(Dfs model) {
        this(model, null);
    }

    public VisualDfs(Dfs model, VisualGroup root) {
        super(model, root);
        setGraphEditorTools();
    }

    private void setGraphEditorTools() {
        List<GraphEditorTool> tools = new ArrayList<>();
        tools.add(new SelectionTool(true, false, true, true));
        tools.add(new CommentGeneratorTool());
        tools.add(new ConnectionTool());
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(Logic.class)));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(Register.class)));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(ControlRegister.class)));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(PushRegister.class)));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(PopRegister.class)));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(CounterflowLogic.class)));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(CounterflowRegister.class)));
        tools.add(new CycleAnalyserTool());
        tools.add(new DfsSimulationTool());
        setGraphEditorTools(tools);
    }

    @Override
    public Dfs getMathModel() {
        return (Dfs) super.getMathModel();
    }

    @Override
    public VisualConnection connect(VisualNode first, VisualNode second, MathConnection mConnection) throws InvalidConnectionException {
        validateConnection(first, second);
        VisualComponent c1 = (VisualComponent) first;
        VisualComponent c2 = (VisualComponent) second;
        MathNode ref1 = c1.getReferencedComponent();
        MathNode ref2 = c2.getReferencedComponent();
        VisualConnection ret = null;
        if (first instanceof VisualControlRegister) {
            if (mConnection == null) {
                mConnection = getMathModel().controlConnect(ref1, ref2);
            }
            ret = new VisualControlConnection((ControlConnection) mConnection, c1, c2);
        } else {
            if (mConnection == null) {
                mConnection = getMathModel().connect(ref1, ref2);
            }
            ret = new VisualConnection(mConnection, c1, c2);
        }
        if (ret != null) {
            Hierarchy.getNearestContainer(c1, c2).add(ret);
        }
        return ret;
    }

    public String getName(VisualComponent component) {
        return getMathModel().getName(component.getReferencedComponent());
    }

    public Set<VisualNode> getRPreset(VisualNode node) {
        Set<VisualNode> result = new HashSet<>();
        result.addAll(getRPreset(node, VisualRegister.class));
        result.addAll(getRPreset(node, VisualCounterflowRegister.class));
        result.addAll(getRPreset(node, VisualControlRegister.class));
        result.addAll(getRPreset(node, VisualPushRegister.class));
        result.addAll(getRPreset(node, VisualPopRegister.class));
        return result;
    }

    public Set<VisualNode> getRPostset(VisualNode node) {
        Set<VisualNode> result = new HashSet<>();
        result.addAll(getRPostset(node, VisualRegister.class));
        result.addAll(getRPostset(node, VisualCounterflowRegister.class));
        result.addAll(getRPostset(node, VisualControlRegister.class));
        result.addAll(getRPostset(node, VisualPushRegister.class));
        result.addAll(getRPostset(node, VisualPopRegister.class));
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

    public Collection<VisualNode> getAllVisualLogics() {
        Set<VisualNode> result = new HashSet<>();
        result.addAll(getVisualLogics());
        result.addAll(getVisualCounterflowLogics());
        return result;
    }

    public Collection<VisualNode> getAllVisualRegisters() {
        Set<VisualNode> result = new HashSet<>();
        result.addAll(getVisualRegisters());
        result.addAll(getVisualCounterflowRegisters());
        result.addAll(getVisualControlRegisters());
        result.addAll(getVisualPushRegisters());
        result.addAll(getVisualPopRegisters());
        return result;
    }

    public Collection<VisualNode> getAllVisualNodes() {
        Set<VisualNode> result = new HashSet<>();
        result.addAll(getAllVisualLogics());
        result.addAll(getAllVisualRegisters());
        return result;
    }

}
