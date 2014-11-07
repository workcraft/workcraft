package org.workcraft.dom.visual;

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.util.Hierarchy;

public class SelectionHelper {

	static public Collection<Node> getOrderedCurrentLevelSelection(VisualModel model) {
		HashSet<Node> result = new HashSet<Node>();
		Collection<Node> selection = model.getSelection();
		Container currentLevel = model.getCurrentLevel();
		for(Node node : currentLevel.getChildren()) {
			if (model.isGroupable(node) && selection.contains(node)) {
				result.add(node);
			}
		}
		return result;
	}

	static public Collection<Node> getRecursiveSelection(VisualModel model) {
		HashSet<Node> result = new HashSet<Node>();
		for (Node node : model.getSelection()) {
			if (node instanceof VisualNode) {
				result.add(node);
				result.addAll(Hierarchy.getDescendantsOfType(node, VisualNode.class));
			}
		}
		return result;
	}

	static public Collection<VisualConnection> getCurrentLevelConnections(VisualModel model) {
		return Hierarchy.getChildrenOfType(model.getCurrentLevel(), VisualConnection.class);
	}

	static public Collection<Node> getGroupableCurrentLevelSelection(VisualModel model) {
		HashSet<Node> result = new HashSet<Node>();
		Collection<Node> currentLevelSelection = getOrderedCurrentLevelSelection(model);
        for (Node node : currentLevelSelection) {
        	if (model.isGroupable(node) && !(node instanceof VisualConnection)) {
            	result.add(node);
            }
        }
        Collection<Node> recursiveSelection = getRecursiveSelection(model);
		Collection<VisualConnection> currentLevelConnections = getCurrentLevelConnections(model);
        for (VisualConnection connection : currentLevelConnections) {
        	VisualComponent first = connection.getFirst();
        	VisualComponent second = connection.getSecond();
        	if (recursiveSelection.contains(first) && recursiveSelection.contains(second)) {
        		result.add(connection);
        	}
        }
		return result;
	}

}
