package org.workcraft.dom;

import java.util.Collection;
import java.util.Set;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.framework.observation.HierarchyEvent;
import org.workcraft.framework.observation.HierarchyObserver;
import org.workcraft.framework.observation.ObservableHierarchyImpl;
import org.workcraft.framework.observation.ObservableStateImpl;
import org.workcraft.framework.observation.StateEvent;
import org.workcraft.framework.observation.StateObserver;
import org.workcraft.framework.plugins.Plugin;

/**
 * A base class for all interpreted graph models.
 * @author Ivan Poliakov
 *
 */
public abstract class AbstractModel implements Plugin, Model {
	private ObservableStateImpl observableStateImpl = new ObservableStateImpl();
	private ObservableHierarchyImpl observableHierarchyImpl = new ObservableHierarchyImpl();

	private NodeContextTracker nodeContextTracker = new NodeContextTracker();
	private NodeIDManager nodeIDManager = new NodeIDManager();
	private HangingConnectionRemover hangingConnectionRemover = new HangingConnectionRemover(nodeContextTracker);

	private String title = "";

	private Container root;

	public AbstractModel(Container root) {
		nodeContextTracker.attach(root);
		nodeIDManager.attach(root);
		hangingConnectionRemover.attach(root);

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
			throw new RuntimeException ("Cannot remove a child node from a node that is not a Container.");
	}

	public void remove (Collection<Node> nodes) {
		for (Node node : nodes)
			remove (node);
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

	public void addObserver(HierarchyObserver obs) {
		observableHierarchyImpl.addObserver(obs);
	}

	public void removeObserver(HierarchyObserver obs) {
		observableHierarchyImpl.removeObserver(obs);
	}

	public void addObserver(StateObserver obs) {
		observableStateImpl.addObserver(obs);
	}

	public void removeObserver(StateObserver obs) {
		observableStateImpl.removeObserver(obs);
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

	public void notify(HierarchyEvent e) {
		observableHierarchyImpl.sendNotification(e);
	}

	public void notify(StateEvent e) {
		observableStateImpl.sendNotification(e);
	}

	public Node getNodeByID(int ID) {
		return nodeIDManager.getNodeByID(ID);
	}

	public int getNodeID(Node node) {
		return nodeIDManager.getNodeID(node);
	}
}