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

import org.workcraft.dom.Node;

public abstract class StateSupervisor extends HierarchySupervisor implements StateObserver {

    private void nodeAdded(Node node) {
        if (node instanceof ObservableState) {
            ((ObservableState) node).addObserver(this);
        }
        for (Node n : node.getChildren()) {
            nodeAdded(n);
        }
    }

    private void nodeRemoved(Node node) {
        if (node instanceof ObservableState) {
            ((ObservableState) node).removeObserver(this);
        }
        for (Node n : node.getChildren()) {
            nodeRemoved(n);
        }
    }

    @Override
    final public void handleEvent(HierarchyEvent e) {
        if (e instanceof NodesAddedEvent) {
            for (Node n : e.getAffectedNodes()) {
                nodeAdded(n);
            }
        } else if (e instanceof NodesDeletedEvent) {
            for (Node n : e.getAffectedNodes()) {
                nodeRemoved(n);
            }
        }
        handleHierarchyEvent(e);
    }

    public void handleHierarchyEvent(HierarchyEvent e) {

    }

    @Override
    final public void notify(StateEvent e) {
        handleEvent(e);
    }

    public abstract void handleEvent(StateEvent e);

}
