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

package org.workcraft.dom.references;

import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.observation.NodesDeletedEvent;
import org.workcraft.util.TwoWayMap;

public class DefaultReferenceManager extends HierarchySupervisor implements ReferenceManager {
    private IDGenerator idGenerator = new IDGenerator();
    private TwoWayMap<String, Node> nodes = new TwoWayMap<String, Node>();

    @Override
    public void handleEvent(HierarchyEvent e) {
        if (e instanceof NodesAddedEvent) {
            for (Node n : e.getAffectedNodes()) {
                nodeAdded(n);
            }
        } else if (e instanceof NodesDeletedEvent) {
            for (Node n: e.getAffectedNodes()) {
                nodeRemoved(n);
            }
        }
    }

    private void nodeRemoved(Node node) {
        nodes.removeValue(node);
        for (Node n: node.getChildren()) {
            nodeRemoved(n);
        }
    }

    private void nodeAdded(Node node) {
        String id = Integer.toString(idGenerator.getNextID());
        nodes.put(id, node);
        for (Node n : node.getChildren()) {
            nodeAdded(n);
        }
    }

    @Override
    public Node getNodeByReference(NamespaceProvider provider, String reference) {
        return nodes.getValue(reference);
    }

    @Override
    public String getNodeReference(NamespaceProvider provider, Node node) {
        return nodes.getKey(node);
    }

    @Override
    public String getPrefix(Node node) {
        return ReferenceHelper.getDefaultPrefix(node);
    }

}
