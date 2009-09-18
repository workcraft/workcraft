package org.workcraft.dom;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.workcraft.Plugin;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.ModelValidationException;

/**
 * A base class for all interpreted graph models.
 * @author Ivan Poliakov
 *
 */
public abstract class AbstractModel implements Plugin, Model, NodeContext {
	private NodeContextTracker nodeContextTracker = new NodeContextTracker();
	private NodeIDManager nodeIDManager = new NodeIDManager();

	private String title = "";

	private Container root;

	public AbstractModel(Container root) {
		nodeContextTracker.attach(root);
		nodeIDManager.attach(root);

		this.root = root;
	}

	public Model getMathModel() {
		return this;
	}

	public VisualModel getVisualModel() {
		return null;
	}

	public void add (Node node) {
		root.add(node);
	}

	public void remove (Node node) {
		if (node.getParent() instanceof Container)
			((Container)node.getParent()).remove(node);
		else
			throw new RuntimeException ("Cannot remove a child node from a node that is not a Container (or null).");
	}

	public void remove (Collection<Node> nodes) {
		LinkedList<Node> toRemove = new LinkedList<Node>(nodes);
		for (Node node : toRemove) {
			// some nodes may be removed as a result of removing other nodes in the list,
			// e.g. hanging connections so need to check
			if (node.getParent() != null)
				remove (node);
		}
	}

	public abstract Connection connect(Node first, Node second) throws InvalidConnectionException;

	public String getDisplayName() {
		DisplayName name = this.getClass().getAnnotation(DisplayName.class);
		if (name == null)
			return this.getClass().getSimpleName();
		else
			return name.value();
	}

	final public String getTitle() {
		return title;
	}

	final public void setTitle(String title) {
		this.title = title;
	}

	abstract public void validate() throws ModelValidationException;

	public final Container getRoot() {
		return root;
	}

	public Set<Connection> getConnections(Node component) {
		return nodeContextTracker.getConnections(component);
	}

	public Set<Node> getPostset(Node component) {
		return nodeContextTracker.getPostset(component);
	}

	public Set<Node> getPreset(Node component) {
		return nodeContextTracker.getPreset(component);
	}

	public Node getNodeByID(int ID) {
		return nodeIDManager.getNodeByID(ID);
	}

	public int getNodeID(Node node) {
		return nodeIDManager.getNodeID(node);
	}
}