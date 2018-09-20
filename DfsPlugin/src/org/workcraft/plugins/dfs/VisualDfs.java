package org.workcraft.plugins.dfs;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.ShortName;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.graph.generators.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.*;
import org.workcraft.plugins.dfs.tools.CycleAnalyserTool;
import org.workcraft.plugins.dfs.tools.DfsSimulationTool;
import org.workcraft.util.Hierarchy;

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
        if (root == null) {
            try {
                createDefaultFlatStructure();
            } catch (NodeCreationException e) {
                throw new RuntimeException(e);
            }
        }
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
    public void validateConnection(Node first, Node second) throws InvalidConnectionException {
        if (first == null || second == null) {
            throw new InvalidConnectionException("Invalid connection");
        }
        if (first == second) {
            throw new InvalidConnectionException("Self-loops are not allowed");
        }
        // Connection from spreadtoken logic
        if (first instanceof VisualLogic && second instanceof VisualCounterflowLogic) {
            throw new InvalidConnectionException("Invalid connection from spreadtoken logic to counterflow logic");
        }
        if (first instanceof VisualLogic && second instanceof VisualCounterflowRegister) {
            throw new InvalidConnectionException("Invalid connection from spreadtoken logic to counterflow register");
        }
        // Connection from spreadtoken register
        if (first instanceof VisualRegister && second instanceof VisualCounterflowLogic) {
            throw new InvalidConnectionException("Invalid connection from spreadtoken register to counterflow logic");
        }
        // Connection from counterflow logic
        if (first instanceof VisualCounterflowLogic && second instanceof VisualLogic) {
            throw new InvalidConnectionException("Invalid connection from counterflow logic to spreadtoken logic");
        }
        if (first instanceof VisualCounterflowLogic && second instanceof VisualRegister) {
            throw new InvalidConnectionException("Invalid connection from counterflow logic to spreadtoken register");
        }
        if (first instanceof VisualCounterflowLogic && second instanceof VisualControlRegister) {
            throw new InvalidConnectionException("Invalid connection from counterflow logic to control register");
        }
        if (first instanceof VisualCounterflowLogic && second instanceof VisualPushRegister) {
            throw new InvalidConnectionException("Invalid connection from counterflow logic to push register");
        }
        if (first instanceof VisualCounterflowLogic && second instanceof VisualPopRegister) {
            throw new InvalidConnectionException("Invalid connection from counterflow logic to pop register");
        }
        // Connection from counterflow register
        if (first instanceof VisualCounterflowRegister && second instanceof VisualLogic) {
            throw new InvalidConnectionException("Invalid connection from counterflow register to spreadtoken logic");
        }
        if (first instanceof VisualCounterflowRegister && second instanceof VisualControlRegister) {
            throw new InvalidConnectionException("Invalid connection from counterflow register to control register");
        }
        if (first instanceof VisualCounterflowRegister && second instanceof VisualPushRegister) {
            throw new InvalidConnectionException("Invalid connection from counterflow register to push register");
        }
        if (first instanceof VisualCounterflowRegister && second instanceof VisualPopRegister) {
            throw new InvalidConnectionException("Invalid connection from counterflow register to pop register");
        }
        // Connection from control register
        if (first instanceof VisualControlRegister && second instanceof VisualCounterflowLogic) {
            throw new InvalidConnectionException("Invalid connection from control register to counterflow logic");
        }
        if (first instanceof VisualControlRegister && second instanceof VisualCounterflowRegister) {
            throw new InvalidConnectionException("Invalid connection from control register to counterflow register");
        }
        // Connection from push register
        if (first instanceof VisualPushRegister && second instanceof VisualCounterflowLogic) {
            throw new InvalidConnectionException("Invalid connection from push register to counterflow logic");
        }
        if (first instanceof VisualPushRegister && second instanceof VisualCounterflowRegister) {
            throw new InvalidConnectionException("Invalid connection from push register to counterflow register");
        }
        // Connection from pop register
        if (first instanceof VisualPopRegister && second instanceof VisualCounterflowLogic) {
            throw new InvalidConnectionException("Invalid connection from pop register to counterflow logic");
        }
        if (first instanceof VisualPopRegister && second instanceof VisualCounterflowRegister) {
            throw new InvalidConnectionException("Invalid connection from pop register to counterflow register");
        }
    }

    @Override
    public VisualConnection connect(Node first, Node second, MathConnection mConnection) throws InvalidConnectionException {
        validateConnection(first, second);
        VisualComponent c1 = (VisualComponent) first;
        VisualComponent c2 = (VisualComponent) second;
        MathNode ref1 = c1.getReferencedComponent();
        MathNode ref2 = c2.getReferencedComponent();
        VisualConnection ret = null;
        if (first instanceof VisualControlRegister) {
            if (mConnection == null) {
                mConnection = ((Dfs) getMathModel()).controlConnect(ref1, ref2);
            }
            ret = new VisualControlConnection((ControlConnection) mConnection, c1, c2);
        } else {
            if (mConnection == null) {
                mConnection = ((Dfs) getMathModel()).connect(ref1, ref2);
            }
            ret = new VisualConnection(mConnection, c1, c2);
        }
        if (ret != null) {
            Hierarchy.getNearestContainer(c1, c2).add(ret);
        }
        return ret;
    }

    public String getName(VisualComponent component) {
        return ((Dfs) getMathModel()).getName(component.getReferencedComponent());
    }

    public Set<Node> getRPreset(Node node) {
        Set<Node> result = new HashSet<>();
        result.addAll(getRPreset(node, VisualRegister.class));
        result.addAll(getRPreset(node, VisualCounterflowRegister.class));
        result.addAll(getRPreset(node, VisualControlRegister.class));
        result.addAll(getRPreset(node, VisualPushRegister.class));
        result.addAll(getRPreset(node, VisualPopRegister.class));
        return result;
    }

    public Set<Node> getRPostset(Node node) {
        Set<Node> result = new HashSet<>();
        result.addAll(getRPostset(node, VisualRegister.class));
        result.addAll(getRPostset(node, VisualCounterflowRegister.class));
        result.addAll(getRPostset(node, VisualControlRegister.class));
        result.addAll(getRPostset(node, VisualPushRegister.class));
        result.addAll(getRPostset(node, VisualPopRegister.class));
        return result;
    }

    public <T> Set<T> getRPreset(Node node, Class<T> type) {
        return getPreset(node, type, arg -> (arg instanceof VisualLogic) || (arg instanceof VisualCounterflowLogic));
    }

    public <T> Set<T> getRPostset(Node node, Class<T> type) {
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
