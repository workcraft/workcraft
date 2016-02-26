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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.workcraft.annotations.CustomTools;
import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.ShortName;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.util.Hierarchy;

@DisplayName("Dataflow Structure")
@ShortName("DFS")
@CustomTools(DfsToolsProvider.class)
public class VisualDfs extends AbstractVisualModel {

    public VisualDfs(Dfs model) throws VisualModelInstantiationException {
        this(model, null);
    }

    public VisualDfs(Dfs model, VisualGroup root) {
        super(model, root);
        if (root == null) {
            try {
                createDefaultFlatStructure();
            } catch (NodeCreationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void validateConnection(Node first, Node second)    throws InvalidConnectionException {
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

    public <R> Set<R> getRPostset(Node node, Class<R> rType) {
        Set<R> result = new HashSet<>();
        Set<Node> visited = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();
        queue.add(node);
        while (!queue.isEmpty()) {
            Node cur = queue.remove();
            if (visited.contains(cur)) continue;
            visited.add(cur);
            for (Node succ: getPostset(cur)) {
                if (!(succ instanceof VisualComponent)) continue;
                try {
                    result.add(rType.cast(succ));
                } catch (ClassCastException e) {
                    if ((succ instanceof VisualLogic) || (succ instanceof VisualCounterflowLogic)) {
                        queue.add(succ);
                    }
                }
            }
        }
        return result;
    }

    public <R> Set<R> getRPreset(Node node, Class<R> rType) {
        Set<R> result = new HashSet<>();
        Set<Node> visited = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();
        queue.add(node);
        while (!queue.isEmpty()) {
            Node cur = queue.remove();
            if (visited.contains(cur)) continue;
            visited.add(cur);
            for (Node pred: getPreset(cur)) {
                if (!(pred instanceof VisualComponent)) continue;
                try {
                    result.add(rType.cast(pred));
                } catch (ClassCastException e) {
                    if ((pred instanceof VisualLogic) || (pred instanceof VisualCounterflowLogic)) {
                        queue.add(pred);
                    }
                }
            }
        }
        return result;
    }

}
