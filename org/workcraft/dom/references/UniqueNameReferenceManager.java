package org.workcraft.dom.references;


import org.workcraft.dom.Node;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.observation.NodesDeletedEvent;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;

public class UniqueNameReferenceManager extends HierarchySupervisor implements ReferenceManager
{
	final private UniqueNameManager<Node> manager;
	private References existing;

	public UniqueNameReferenceManager(References existing, Func<Node, String> defaultName) {
		this (null, existing, defaultName);
	}
	public UniqueNameReferenceManager(UniqueNameManager<Node> manager, References existing, Func<Node, String> defaultName)
	{
		this.existing = existing;
		if (manager == null)
			this.manager = new UniqueNameManager<Node>(defaultName);
		else
			this.manager = manager;
	}


	@Override
	public void attach(Node root)
	{
		if (existing != null) {
			setExistingReference(root);
			for(Node n : Hierarchy.getDescendantsOfType(root, Node.class))
				setExistingReference(n);
			existing = null;
		}

		super.attach(root);
	}
	private void setExistingReference(Node n) {
		final String reference = existing.getReference(n);
		if (reference != null)
			manager.setName(n, reference);
	}

	@Override
	public Node getNodeByReference(String reference) {
		return manager.get(reference);
	}

	@Override
	public String getNodeReference(Node node) {
		return manager.getName(node);
	}

	@Override
	public void handleEvent(HierarchyEvent e) {
		if(e instanceof NodesAddedEvent)
			for(Node node : e.getAffectedNodes()) {
				manager.setDefaultNameIfUnnamed(node);
				for (Node node2 : Hierarchy.getDescendantsOfType(node, Node.class))
					manager.setDefaultNameIfUnnamed(node2);
			}
		if(e instanceof NodesDeletedEvent)
			for(Node node : e.getAffectedNodes()) {
				manager.remove(node);
				for (Node node2 : Hierarchy.getDescendantsOfType(node, Node.class))
					manager.remove(node2);
			}
	}

	public void setName(Node node, String label) {
		manager.setName(node, label);
	}
}