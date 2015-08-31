package org.workcraft.dom.references;

import java.util.Collection;
import java.util.HashMap;

import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.observation.NodesDeletedEvent;
import org.workcraft.serialisation.References;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.Identifier;

public class HierarchicalUniqueNameReferenceManager extends HierarchySupervisor implements ReferenceManager {

	final private HashMap<NamespaceProvider, NameManager> managers = new HashMap<NamespaceProvider, NameManager>();

	// every node belongs to some name space provider (except the main root node of the model)
	final private HashMap<Node, NamespaceProvider> node2namespace = new HashMap<Node, NamespaceProvider>();
	private NamespaceProvider topProvider; // namespace provided by root
	private References refs;

	public HierarchicalUniqueNameReferenceManager() {
		this(null);
	}

	public HierarchicalUniqueNameReferenceManager(References refs) {
		this.refs = refs;
	}

	public NamespaceProvider getNamespaceProvider(Node node) {
		NamespaceProvider provider = node2namespace.get(node);
		if (provider == null) {
			Node container = node.getParent();
			if (container != null) {
				provider = Hierarchy.getNearestAncestor(container, NamespaceProvider.class);
			}
		}
		node2namespace.put(node, provider);
		return provider;
	}

	public void setNamespaceProvider(Collection<Node> nodes, NamespaceProvider provider) {
		setNamespaceProvider(nodes, this, provider);
	}

	public void setNamespaceProvider(Collection<Node> nodes,
			HierarchicalUniqueNameReferenceManager srcRefManager,
			NamespaceProvider dstProvider) {

		if (dstProvider == null) {
			dstProvider = topProvider;
		}
		for (Node node : nodes) {
			NamespaceProvider srcProvider = srcRefManager.getNamespaceProvider(node);
			if (srcProvider != null) {
				String name = srcRefManager.getName(node);

				// Clear cached data in the local and the source reference manager
				node2namespace.remove(node);
				srcRefManager.node2namespace.remove(node);

				// Do not assign name if it wasn't assigned in the first place (eg. the implicit place)
				if ((name != null) && ((dstProvider != srcProvider) || (node2namespace != srcRefManager.node2namespace))) {
					NameManager srcNameManager = srcRefManager.getNameManager(srcProvider);
					NameManager dstNameManager = this.getNameManager(dstProvider);
					srcNameManager.remove(node);
					Node clashingNode = dstNameManager.getNode(name);
					if (nodes.contains(clashingNode)) {
						String newName = dstNameManager.getDerivedName(clashingNode, name);
						dstNameManager.setName(clashingNode, newName);
					}
					String newName = dstNameManager.getDerivedName(node, name);
					dstNameManager.setName(node, newName);
				}
			}
		}

	}

	@Override
	public void attach(Node root) {
		// root must be a namespace provider
		topProvider = (NamespaceProvider)root;
		if (refs != null) {
			for(Node n : Hierarchy.getDescendantsOfType(root, Node.class)) {
				setExistingReference(n);
			}
			refs = null;
		}
		super.attach(root);
	}

	protected NameManager createNameManager() {
		return new UniqueNameManager() {
			@Override
			public String getPrefix(Node node) {
				return HierarchicalUniqueNameReferenceManager.this.getPrefix(node);
			}
		};
	}

	public NameManager getNameManager(NamespaceProvider provider) {
		if (provider == null) {
			provider = topProvider;
		}
		NameManager man = managers.get(provider);
		if (man == null) {
			man = createNameManager();
			managers.put(provider, man);
		}
		return man;
	}

	protected void setExistingReference(Node node) {
		String reference = refs.getReference(node);
		if (Identifier.isNumber(reference)) {
			String name = getName(node);
			if (name != null) {
				reference = name;
			}
		}
		if (reference != null) {
			String name = NamespaceHelper.getReferenceName(reference);
			setName(node, name);
		}
	}

	public Node getNodeByReference(NamespaceProvider provider, String reference) {
		if (topProvider == null) {
			System.err.println("HierarchicalUniqueNameReferenceManager created with topProvider==null!");
		}
		if (provider==null) {
			provider = topProvider;
		}
		if (reference.isEmpty() || reference.equals(NamespaceHelper.hierarchySeparator)) {
			return provider;
		}
		String head =  NamespaceHelper.getReferenceHead(reference);
		String tail =  NamespaceHelper.getReferenceTail(reference);
		NameManager man = getNameManager(provider);
		Node node;
		node = man.getNode(head);
		if ((node != null) && (node instanceof NamespaceProvider)) {
			return getNodeByReference((NamespaceProvider)node, tail);
		}
		return node;
	}

	@Override
	public String getNodeReference(NamespaceProvider provider, Node node) {
		if (node == topProvider) {
			return NamespaceHelper.hierarchyRoot;
		}
		if (provider == null) {
			provider = topProvider;
		}
		NamespaceProvider component = null;
		String result = "";
		do {
			component = getNamespaceProvider(node);
			if (component != null) {
				if (!result.isEmpty()) {
					result = NamespaceHelper.hierarchySeparator + result;
				}
				String name = getNameManager(component).getName(node);
				// The unnamed component just returns null
				if (name == null) return null;
				result = name + result;
				node = node.getParent();
			}
		} while ((node != null) && (component != null) && (component != provider));
		return result;
	}

	@Override
	public void handleEvent(HierarchyEvent e) {
		if (e instanceof NodesAddedEvent) {
			for (Node node : e.getAffectedNodes()) {
				if (node.getParent()!=null) {
					// if it is not a root node
					NamespaceProvider provider = getNamespaceProvider(node);
					NameManager man = getNameManager(provider);
					man.setDefaultNameIfUnnamed(node);
					String name = man.getName(node);
					// additional call to propagate the name data after calling setDefaultNameIfUnnamed
					setName(node, name);
				}
				for (Node node2 : Hierarchy.getDescendantsOfType(node, Node.class)) {
					NamespaceProvider provider2 = getNamespaceProvider(node2);
					NameManager mgr2 = getNameManager(provider2);
					mgr2.setDefaultNameIfUnnamed(node2);
					// additional call to propagate the name data after calling setDefaultNameIfUnnamed
					setName(node2, mgr2.getName(node2));
				}
			}
		}

		if (e instanceof NodesDeletedEvent) {
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
		NamespaceProvider provider = getNamespaceProvider(node);
		NameManager mgr = getNameManager(provider);
		mgr.setName(node, name);
	}

	public String getName(Node node) {
		NamespaceProvider provider = getNamespaceProvider(node);
		NameManager mgr = getNameManager(provider);
		return mgr.getName(node);
	}

	@Override
	public String getPrefix(Node node) {
		return ReferenceHelper.getDefaultPrefix(node);
	}

}
