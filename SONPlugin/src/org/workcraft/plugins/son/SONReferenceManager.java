package org.workcraft.plugins.son;

import org.workcraft.dom.Node;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.dom.references.UniqueNameManager;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.observation.NodesDeletedEvent;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.Event;
import org.workcraft.plugins.son.elements.NamedElement;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;

public class SONReferenceManager extends HierarchySupervisor implements ReferenceManager {
	private UniqueNameManager<Node> defaultNameManager;
	private References existingReferences;

	public SONReferenceManager(References existingReferences) {
		this.existingReferences = existingReferences;
		this.defaultNameManager = new UniqueNameManager<Node>(new Func<Node, String>() {
			@Override
			public String eval(Node arg) {
				if (arg instanceof Condition)
					return "c";
				if (arg instanceof Event)
					return "e";
				if (arg instanceof SONConnection)
					return "con";
				if (arg instanceof ChannelPlace)
					return "q";
				return "node";
			}
		});
	}

	@Override
	public void attach(Node root) {
		if (root == null) {
			throw new NullPointerException();
		}
		if (existingReferences != null) {
			setExistingReference(root);
			for (Node n: Hierarchy.getDescendantsOfType(root, Node.class)) {
				setExistingReference(n);
			}
			existingReferences = null;
		}
		super.attach(root);
	}

	private void setExistingReference(Node node) {
		final String ref = existingReferences.getReference(node);
		if (ref != null) {
			setName(node, ref);
		}
	}

	@Override
	public Node getNodeByReference(String ref) {
		return defaultNameManager.get(ref);
	}

	@Override
	public String getNodeReference(Node node) {
		return defaultNameManager.getName(node);
	}

	public String getName (Node node) {
		return defaultNameManager.getName(node);
	}

	public void setName(Node node, String ref) {
		defaultNameManager.setName(node, ref);
		if (node instanceof NamedElement) {
			((NamedElement)node).setName(getName(node));
		}
	}

	@Override
	public void handleEvent(HierarchyEvent e) {
		if(e instanceof NodesDeletedEvent) {
			for(Node node : e.getAffectedNodes()) {
				nodeRemoved(node);
				for (Node n : Hierarchy.getDescendantsOfType(node, Node.class)) {
					nodeRemoved(n);
				}
			}
		}
		if(e instanceof NodesAddedEvent) {
			for(Node node : e.getAffectedNodes()) {
				setDefaultNameIfUnnamed(node);
				for (Node n : Hierarchy.getDescendantsOfType(node, Node.class)) {
					setDefaultNameIfUnnamed(n);
				}
			}
		}
	}

	public void setDefaultNameIfUnnamed(Node node) {
		defaultNameManager.setDefaultNameIfUnnamed(node);
		if (node instanceof NamedElement) {
			((NamedElement)node).setName(getName(node));
		}
	}

	private void nodeRemoved(Node node) {
		defaultNameManager.remove(node);
		if (node instanceof NamedElement) {
			((NamedElement)node).setName("");
		}
	}

}
