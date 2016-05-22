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

import java.util.HashMap;
import java.util.HashSet;

import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.Dependent;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.observation.NodesDeletedEvent;

public class DefaultMathNodeRemover extends HierarchySupervisor {

    private final HashMap<MathNode, HashSet<Dependent>> referenceTracker = new HashMap<>();

    private void addReference(MathNode mathNode, Dependent dependentNode) {
        if (mathNode != null) {
            HashSet<Dependent> refs = referenceTracker.get(mathNode);
            if (refs == null) {
                refs = new HashSet<Dependent>();
                referenceTracker.put(mathNode, refs);
            }
            refs.add(dependentNode);
        }
    }

    private void removeReference(MathNode mathNode, Dependent dependentNode) {
        if (mathNode != null) {
            HashSet<Dependent> refs = referenceTracker.get(mathNode);
            if (refs != null) {
                refs.remove(dependentNode);
            }
            if ((refs == null) || refs.isEmpty()) {
                referenceTracker.remove(mathNode);
                Node parent = mathNode.getParent();
                if (parent instanceof Container) {
                    ((Container) parent).remove(mathNode);
                }
            }
        }
    }

    @Override
    public void handleEvent(HierarchyEvent e) {
        if (e instanceof NodesDeletedEvent) {
            for (Node node : e.getAffectedNodes()) {
                nodeRemoved(node);
            }
        }
        if (e instanceof NodesAddedEvent) {
            for (Node node : e.getAffectedNodes()) {
                nodeAdded(node);
            }
        }
    }

    private void nodeAdded(Node node) {
        if (node instanceof Dependent) {
            Dependent dependentNode = (Dependent) node;
            for (MathNode mathNode : dependentNode.getMathReferences()) {
                addReference(mathNode, dependentNode);
            }
        }
        if (node != null) {
            for (Node childNode : node.getChildren()) {
                nodeAdded(childNode);
            }
        }
    }

    private void nodeRemoved(Node node) {
        if (node instanceof Dependent) {
            Dependent dependentNode = (Dependent) node;
            for (MathNode mathNode : dependentNode.getMathReferences()) {
                removeReference(mathNode, dependentNode);
            }
        }
        if (node != null) {
            for (Node childNode : node.getChildren()) {
                nodeRemoved(childNode);
            }
        }
    }

}
