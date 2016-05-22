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

package org.workcraft.observation;

import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.dom.Node;

public class NodesReparentedEvent implements HierarchyEvent {
    private final Node oldParentNode;
    private final Node newParentNode;
    private final Collection<Node> affectedNodes;

    public NodesReparentedEvent(Node oldParentNode, Node newParentNode, Collection<Node> affectedNodes) {
        this.oldParentNode = oldParentNode;
        this.newParentNode = newParentNode;
        this.affectedNodes = affectedNodes;
    }

    public NodesReparentedEvent(Node oldParentNode, Node newParentNode, Node affectedNode) {
        this.oldParentNode = oldParentNode;
        this.newParentNode = newParentNode;
        this.affectedNodes = new ArrayList<Node>();
        affectedNodes.add(affectedNode);
    }

    public Collection<Node> getAffectedNodes() {
        return affectedNodes;
    }

    public Node getOldParent() {
        return oldParentNode;
    }

    public Node getNewParent() {
        return newParentNode;
    }

    public Object getSender() {
        return newParentNode;
    }
}
