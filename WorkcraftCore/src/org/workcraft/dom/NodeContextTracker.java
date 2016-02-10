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

package org.workcraft.dom;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.observation.NodesDeletedEvent;
import org.workcraft.observation.NodesReparentingEvent;

public class NodeContextTracker extends HierarchySupervisor implements NodeContext {
    private HashMap<Node, LinkedHashSet<Node>> presets = new HashMap<Node, LinkedHashSet<Node>>();
    private HashMap<Node, LinkedHashSet<Node>> postsets = new HashMap<Node, LinkedHashSet<Node>>();
    private HashMap<Node, LinkedHashSet<Connection>> connections = new HashMap<Node, LinkedHashSet<Connection>>();

    private void initHashes(Node n) {
        LinkedHashSet<Node> set = presets.get(n);
        if (set == null) {
            presets.put(n, new LinkedHashSet<Node>());
        }
        set = postsets.get(n);
        if (set == null) {
            postsets.put(n, new LinkedHashSet<Node>());
        }
        LinkedHashSet<Connection> conSet = connections.get(n);
        if (conSet == null) {
            connections.put(n, new LinkedHashSet<Connection>());
        }
    }

    private void removeHashes(Node n) {
        presets.remove(n);
        postsets.remove(n);
        connections.remove(n);
    }

    private void nodeAdded(Node n) {
        initHashes(n);

        if (n instanceof Connection) {
            Connection con = (Connection)n;
            Node c1 = con.getFirst();
            Node c2 = con.getSecond();

            initHashes(c1);
            initHashes(c2);

            postsets.get(c1).add(c2);
            presets.get(c2).add(c1);
            connections.get(c1).add(con);
            connections.get(c2).add(con);
        }

        for (Node nn : n.getChildren()) {
            nodeAdded(nn);
        }
    }

    private void nodeRemoved(Node node) {
        LinkedHashSet<Node> nodePostset = postsets.get(node);
        if (nodePostset != null) {
            for (Node succNode: nodePostset) {
                LinkedHashSet<Node> succNodePreset = presets.get(succNode);
                if (succNodePreset != null) {
                    succNodePreset.remove(node);
                }
            }
        }
        LinkedHashSet<Node> nodePreset = presets.get(node);
        if (nodePreset != null) {
            for (Node predNode: nodePreset) {
                LinkedHashSet<Node> predNodePostset = postsets.get(predNode);
                if (predNodePostset != null) {
                    predNodePostset.remove(node);
                }
            }
        }
        removeHashes(node);

        if (node instanceof Connection) {
            Connection connection = (Connection)node;
            Node first = connection.getFirst();
            Node second = connection.getSecond();

            LinkedHashSet<Node> firstPostset = postsets.get(first);
            if (firstPostset != null) {
                firstPostset.remove(second);
            }
            LinkedHashSet<Node> secondPreset = presets.get(second);
            if (secondPreset != null) {
                secondPreset.remove(first);
            }
            LinkedHashSet<Connection> firstConnections = connections.get(first);
            if (firstConnections != null) {
                firstConnections.remove(connection);
            }
            LinkedHashSet<Connection> secondConnections = connections.get(second);
            if (secondConnections != null) {
                secondConnections.remove(connection);
            }
        }

        Collection<Node> children = node.getChildren();
        if (children != null) {
            for (Node childNode : children) {
                nodeRemoved(childNode);
            }
        }
    }

    @Override
    public Set<Node> getPreset(Node node) {
        return Collections.unmodifiableSet(presets.get(node));
    }

    @Override
    public Set<Node> getPostset(Node node) {
        return Collections.unmodifiableSet(postsets.get(node));
    }

    @Override
    public Set<Connection> getConnections(Node node) {
        Set<Connection> ret = connections.get(node);
        if (ret == null) {
            ret = new HashSet<Connection>();
        }
        return Collections.unmodifiableSet(ret);
    }

    @Override
    public boolean hasConnection(Node first, Node second) {
        LinkedHashSet<Node> firstPostset = postsets.get(first);
        return (firstPostset != null) && (firstPostset.contains(second));
    }

    @Override
    public Connection getConnection(Node first, Node second) {
        for(Connection connection : getConnections(first)) {
            if ((connection.getFirst() == first) && (connection.getSecond() == second)) {
                return connection;
            }
        }
        return null;
    }

    @Override
    public void handleEvent(HierarchyEvent e) {
        if (e instanceof NodesReparentingEvent) {
            for (Node n : e.getAffectedNodes()) {
                nodeAdded(n);
            }
        } else if (e instanceof NodesAddedEvent) {
            for (Node n : e.getAffectedNodes()) {
                nodeAdded(n);
            }
        } else if (e instanceof NodesDeletedEvent) {
            for (Node n : e.getAffectedNodes()) {
                nodeRemoved(n);
            }
        }
    }

}
