package org.workcraft.dom.references;

import java.util.HashMap;

import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.observation.NodesDeletedEvent;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;

public class HierarchicalUniqueNameReferenceManager extends HierarchySupervisor implements ReferenceManager {

	final private static String hierarchySeparator = ".";

	final private HashMap<NamespaceProvider, UniqueNameManager<Node>> managers = new HashMap<NamespaceProvider, UniqueNameManager<Node>>();
	// every node belongs to some name space provider (except the main root node of the model)
	final private HashMap<Node, NamespaceProvider> node2namespace = new HashMap<Node, NamespaceProvider>();
	final private NamespaceProvider topProvider;
	Func<Node, String> defaultName;

	private References existing;

	public HierarchicalUniqueNameReferenceManager(NamespaceProvider provider, References existing, Func<Node, String> defaultName) {
//		this (null, existing, defaultName);
		this.existing = existing;
		topProvider = provider;
		this.defaultName = defaultName;
	}

//	public HierarchicalUniqueNameReferenceManager(UniqueNameManager<Node> manager, References existing, Func<Node, String> defaultName) {
//		this.existing = existing;
//
//		if (manager == null) {
//			this.manager = new UniqueNameManager<Node>(defaultName);
//		} else {
//			this.manager = manager;
//		}
//	}

	public NamespaceProvider getNamespaceProvider(Node node) {

		NamespaceProvider topProvider = node2namespace.get(node);

		if (topProvider!=null) return topProvider;

		Node topContainer = node.getParent();

		while (topContainer!=null && !(topContainer instanceof NamespaceProvider)) {
			topContainer = topContainer.getParent();
		}

		if (topContainer instanceof NamespaceProvider)
			topProvider = (NamespaceProvider) topContainer;


		if (topProvider!=null) node2namespace.put(node, topProvider);

		return topProvider;
	}

	public void setNamespaceProvider(Node node, NamespaceProvider provider) {

		if (provider==null) provider = topProvider;

		NamespaceProvider oldProvider = getNamespaceProvider(node);
		if (oldProvider==null) {

			node2namespace.put(node,  provider);

		} else if (provider!=oldProvider) {


			String name = getName(node);
			UniqueNameManager<Node> oldMan = getNameManager(oldProvider);
			oldMan.remove(node);

			node2namespace.put(node,  provider);
			setName(node, name);
		}


	}

	@Override
	public void attach(Node root) {
		if (existing != null) {
			setExistingReference(root);
			for(Node n : Hierarchy.getDescendantsOfType(root, Node.class)) {
				setExistingReference(n);
			}
			existing = null;
		}
		super.attach(root);
	}


	private UniqueNameManager<Node> getNameManager(NamespaceProvider provider) {
		if (provider==null) provider = topProvider;


		UniqueNameManager<Node> man = managers.get(provider);
		if (man==null) {
			man = new UniqueNameManager<Node>(defaultName);
			managers.put(provider, man);
		}
		return man;
	}

	private void setExistingReference(Node n) {
		final String reference = existing.getReference(n);

		if (reference != null) {
			NamespaceProvider provider = getNamespaceProvider(n);
			UniqueNameManager<Node> man = getNameManager(provider);
			man.setName(n, reference);
		}
	}

	private static String getReferenceHead(String reference) {
		int idx = reference.indexOf(hierarchySeparator);
		if (idx==-1) return reference;
		return reference.substring(0,idx-1);
	}

	private static String getReferenceTail(String reference) {
		int idx = reference.indexOf(hierarchySeparator);
		if (idx==-1) return "";
		return reference.substring(idx+1);
	}

	public Node getNodeByReference(NamespaceProvider provider, String reference) {

		if (provider!=null&&reference.equals("")) return (Node)provider;

		String head = getReferenceHead(reference);
		String tail = getReferenceTail(reference);

		if (provider==null) provider = topProvider;
		UniqueNameManager<Node> man = getNameManager(provider);

		Node node = man.get(head);

		if (node instanceof NamespaceProvider) {
			return getNodeByReference((NamespaceProvider)node, tail);
		}

		return null;
	}

//	@Override
//	public Node getNodeByReference(String reference) {
//		return getNodeByReference(null, reference);
//	}
//
//	@Override
//	public String getNodeReference(Node node) {
//		UniqueNameManager<Node> man = getNameManager(getNamespaceProvider(node));
//		return man.getName(node);
//	}

	@Override
	public String getNodeReference(NamespaceProvider provider, Node node) {
		if (provider==null) provider = topProvider;

		NamespaceProvider prov = getNamespaceProvider(node);
		String ret= "";

		do {
			prov = getNamespaceProvider(node);
			if (!ret.equals(""))
				ret=hierarchySeparator+ret;

			ret=getNameManager(prov).getName(node)+ret;
			node = node.getParent();

		} while (prov!=null&&provider!=prov);



		return ret;
	}


	@Override
	public void handleEvent(HierarchyEvent e) {

		if(e instanceof NodesAddedEvent) {
			for(Node node : e.getAffectedNodes()) {

				NamespaceProvider provider = getNamespaceProvider(node);
				UniqueNameManager<Node> man = getNameManager(provider);
				man.setDefaultNameIfUnnamed(node);

				for (Node node2 : Hierarchy.getDescendantsOfType(node, Node.class)) {
					getNameManager(getNamespaceProvider(node2)).setDefaultNameIfUnnamed(node2);
				}

			}
		}

		if(e instanceof NodesDeletedEvent) {
			for(Node node : e.getAffectedNodes()) {
				getNameManager(getNamespaceProvider(node)).remove(node);
				node2namespace.remove(node);

				for (Node node2 : Hierarchy.getDescendantsOfType(node, Node.class)) {
					getNameManager(getNamespaceProvider(node2)).remove(node2);
					node2namespace.remove(node2);
				}
			}
		}
	}

	public void setName(Node node, String name) {
		UniqueNameManager<Node> man = getNameManager(getNamespaceProvider(node));
		man.setName(node, name);
	}

	public String getName(Node node) {
		UniqueNameManager<Node> man = getNameManager(getNamespaceProvider(node));
		return man.getName(node);
	}


}
