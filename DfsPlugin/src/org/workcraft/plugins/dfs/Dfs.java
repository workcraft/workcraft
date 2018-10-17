package org.workcraft.plugins.dfs;

import org.workcraft.dom.Container;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.serialisation.References;
import org.workcraft.util.Hierarchy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Dfs extends AbstractMathModel {

    public Dfs() {
        this(null, null);
    }

    public Dfs(Container root, References refs) {
        super(root, refs);
    }

    @Override
    public void validateConnection(MathNode first, MathNode second) throws InvalidConnectionException {
        super.validateConnection(first, second);

        if (first == second) {
            throw new InvalidConnectionException("Self-loops are not allowed");
        }
        // Connection from spreadtoken logic
        if ((first instanceof Logic) && (second instanceof CounterflowLogic)) {
            throw new InvalidConnectionException("Invalid connection from spreadtoken logic to counterflow logic");
        }
        if ((first instanceof Logic) && (second instanceof CounterflowRegister)) {
            throw new InvalidConnectionException("Invalid connection from spreadtoken logic to counterflow register");
        }
        // Connection from spreadtoken register
        if ((first instanceof Register) && (second instanceof CounterflowLogic)) {
            throw new InvalidConnectionException("Invalid connection from spreadtoken register to counterflow logic");
        }
        // Connection from counterflow logic
        if ((first instanceof CounterflowLogic) && (second instanceof Logic)) {
            throw new InvalidConnectionException("Invalid connection from counterflow logic to spreadtoken logic");
        }
        if ((first instanceof CounterflowLogic) && (second instanceof Register)) {
            throw new InvalidConnectionException("Invalid connection from counterflow logic to spreadtoken register");
        }
        if ((first instanceof CounterflowLogic) && (second instanceof ControlRegister)) {
            throw new InvalidConnectionException("Invalid connection from counterflow logic to control register");
        }
        if ((first instanceof CounterflowLogic) && (second instanceof PushRegister)) {
            throw new InvalidConnectionException("Invalid connection from counterflow logic to push register");
        }
        if ((first instanceof CounterflowLogic) && (second instanceof PopRegister)) {
            throw new InvalidConnectionException("Invalid connection from counterflow logic to pop register");
        }
        // Connection from counterflow register
        if ((first instanceof CounterflowRegister) && (second instanceof Logic)) {
            throw new InvalidConnectionException("Invalid connection from counterflow register to spreadtoken logic");
        }
        if ((first instanceof CounterflowRegister) && (second instanceof ControlRegister)) {
            throw new InvalidConnectionException("Invalid connection from counterflow register to control register");
        }
        if ((first instanceof CounterflowRegister) && (second instanceof PushRegister)) {
            throw new InvalidConnectionException("Invalid connection from counterflow register to push register");
        }
        if ((first instanceof CounterflowRegister) && (second instanceof PopRegister)) {
            throw new InvalidConnectionException("Invalid connection from counterflow register to pop register");
        }
        // Connection from control register
        if ((first instanceof ControlRegister) && (second instanceof CounterflowLogic)) {
            throw new InvalidConnectionException("Invalid connection from control register to counterflow logic");
        }
        if ((first instanceof ControlRegister) && (second instanceof CounterflowRegister)) {
            throw new InvalidConnectionException("Invalid connection from control register to counterflow register");
        }
        // Connection from push register
        if ((first instanceof PushRegister) && (second instanceof CounterflowLogic)) {
            throw new InvalidConnectionException("Invalid connection from push register to counterflow logic");
        }
        if ((first instanceof PushRegister) && (second instanceof CounterflowRegister)) {
            throw new InvalidConnectionException("Invalid connection from push register to counterflow register");
        }
        // Connection from pop register
        if ((first instanceof PopRegister) && (second instanceof CounterflowLogic)) {
            throw new InvalidConnectionException("Invalid connection from pop register to counterflow logic");
        }
        if ((first instanceof PopRegister) && (second instanceof CounterflowRegister)) {
            throw new InvalidConnectionException("Invalid connection from pop register to counterflow register");
        }
    }

    public ControlConnection controlConnect(MathNode first, MathNode second) throws InvalidConnectionException {
        validateConnection(first, second);
        ControlConnection connection = new ControlConnection(first, second);
        Container container = Hierarchy.getNearestContainer(first, second);
        container.add(connection);
        return connection;
    }

    public Collection<Logic> getLogics() {
        return Hierarchy.getDescendantsOfType(getRoot(), Logic.class);
    }

    public Collection<Register> getRegisters() {
        return Hierarchy.getDescendantsOfType(getRoot(), Register.class);
    }

    public Collection<CounterflowLogic> getCounterflowLogics() {
        return Hierarchy.getDescendantsOfType(getRoot(), CounterflowLogic.class);
    }

    public Collection<CounterflowRegister> getCounterflowRegisters() {
        return Hierarchy.getDescendantsOfType(getRoot(), CounterflowRegister.class);
    }

    public Collection<ControlRegister> getControlRegisters() {
        return Hierarchy.getDescendantsOfType(getRoot(), ControlRegister.class);
    }

    public Collection<PushRegister> getPushRegisters() {
        return Hierarchy.getDescendantsOfType(getRoot(), PushRegister.class);
    }

    public Collection<PopRegister> getPopRegisters() {
        return Hierarchy.getDescendantsOfType(getRoot(), PopRegister.class);
    }

    public Collection<MathNode> getAllLogics() {
        Set<MathNode> result = new HashSet<>();
        result.addAll(getLogics());
        result.addAll(getCounterflowLogics());
        return result;
    }

    public Collection<MathNode> getAllRegisters() {
        Set<MathNode> result = new HashSet<>();
        result.addAll(getRegisters());
        result.addAll(getCounterflowRegisters());
        result.addAll(getControlRegisters());
        result.addAll(getPushRegisters());
        result.addAll(getPopRegisters());
        return result;
    }

    public Collection<MathNode> getAllNodes() {
        Set<MathNode> result = new HashSet<>();
        result.addAll(getAllLogics());
        result.addAll(getAllRegisters());
        return result;
    }

    public Set<MathNode> getRPreset(MathNode node) {
        Set<MathNode> result = new HashSet<>();
        result.addAll(getRPreset(node, Register.class));
        result.addAll(getRPreset(node, CounterflowRegister.class));
        result.addAll(getRPreset(node, ControlRegister.class));
        result.addAll(getRPreset(node, PushRegister.class));
        result.addAll(getRPreset(node, PopRegister.class));
        return result;
    }

    public Set<MathNode> getRPostset(MathNode node) {
        Set<MathNode> result = new HashSet<>();
        result.addAll(getRPostset(node, Register.class));
        result.addAll(getRPostset(node, CounterflowRegister.class));
        result.addAll(getRPostset(node, ControlRegister.class));
        result.addAll(getRPostset(node, PushRegister.class));
        result.addAll(getRPostset(node, PopRegister.class));
        return result;
    }

    public <T> Set<T> getRPreset(MathNode node, Class<T> type) {
        return getPreset(node, type, arg -> (arg instanceof Logic) || (arg instanceof CounterflowLogic));
    }

    public <T> Set<T> getRPostset(MathNode node, Class<T> type) {
        return getPostset(node, type, arg -> (arg instanceof Logic) || (arg instanceof CounterflowLogic));
    }

}
