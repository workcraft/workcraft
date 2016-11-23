/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.plugins.dfs;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;

@VisualClass (org.workcraft.plugins.dfs.VisualDfs.class)
public class Dfs extends AbstractMathModel {

    public Dfs() {
        this(null, null);
    }

    public Dfs(Container root, References refs) {
        super(root, new HierarchicalUniqueNameReferenceManager(refs) {
            @Override
            public String getPrefix(Node node) {
                if ((node instanceof Logic) || (node instanceof CounterflowLogic)) return "l";
                if ((node instanceof Register) || (node instanceof CounterflowRegister)) return "r";
                if (node instanceof ControlRegister) return "c";
                if ((node instanceof PushRegister) || (node instanceof PopRegister)) return "p";
                return super.getPrefix(node);
            }
        });
    }

    public MathConnection connect(Node first, Node second) {
        MathConnection con = new MathConnection((MathNode) first, (MathNode) second);
        Hierarchy.getNearestContainer(first, second).add(con);
        return con;
    }

    public ControlConnection controlConnect(Node first, Node second) {
        ControlConnection con = new ControlConnection((MathNode) first, (MathNode) second);
        Hierarchy.getNearestContainer(first, second).add(con);
        return con;
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

    public Collection<Node> getAllLogics() {
        Set<Node> result = new HashSet<>();
        result.addAll(getLogics());
        result.addAll(getCounterflowLogics());
        return result;
    }

    public Collection<Node> getAllRegisters() {
        Set<Node> result = new HashSet<>();
        result.addAll(getRegisters());
        result.addAll(getCounterflowRegisters());
        result.addAll(getControlRegisters());
        result.addAll(getPushRegisters());
        result.addAll(getPopRegisters());
        return result;
    }

    public Collection<Node> getAllNodes() {
        Set<Node> result = new HashSet<>();
        result.addAll(getAllLogics());
        result.addAll(getAllRegisters());
        return result;
    }

    public Set<Node> getRPreset(Node node) {
        Set<Node> result = new HashSet<>();
        result.addAll(getRPreset(node, Register.class));
        result.addAll(getRPreset(node, CounterflowRegister.class));
        result.addAll(getRPreset(node, ControlRegister.class));
        result.addAll(getRPreset(node, PushRegister.class));
        result.addAll(getRPreset(node, PopRegister.class));
        return result;
    }

    public Set<Node> getRPostset(Node node) {
        Set<Node> result = new HashSet<>();
        result.addAll(getRPostset(node, Register.class));
        result.addAll(getRPostset(node, CounterflowRegister.class));
        result.addAll(getRPostset(node, ControlRegister.class));
        result.addAll(getRPostset(node, PushRegister.class));
        result.addAll(getRPostset(node, PopRegister.class));
        return result;
    }

    public <T> Set<T> getRPreset(Node node, Class<T> type) {
        return getPreset(node, type, new Func<Node, Boolean>() {
            @Override
            public Boolean eval(Node arg) {
                return (arg instanceof Logic) || (arg instanceof CounterflowLogic);
            }
        });
    }

    public <T> Set<T> getRPostset(Node node, Class<T> type) {
        return getPostset(node, type, new Func<Node, Boolean>() {
            @Override
            public Boolean eval(Node arg) {
                return (arg instanceof Logic) || (arg instanceof CounterflowLogic);
            }
        });
    }

}
