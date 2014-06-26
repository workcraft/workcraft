package org.workcraft.dom.references;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.observation.NodesDeletedEvent;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.Identifier;

public class HierarchicalUniqueNameReferenceManager extends HierarchySupervisor implements ReferenceManager {

	final private HashMap<NamespaceProvider, NameManager<Node>> managers = new HashMap<NamespaceProvider, NameManager<Node>>();

	// every node belongs to some name space provider (except the main root node of the model)
	final private HashMap<Node, NamespaceProvider> node2namespace = new HashMap<Node, NamespaceProvider>();
	final private NamespaceProvider topProvider; // namespace provided by root

	protected Func<Node, String> defaultName;
	private References existing;

	public HierarchicalUniqueNameReferenceManager(NamespaceProvider provider, References existing, Func<Node, String> defaultName) {

		this.existing = existing;
		topProvider = provider;
		this.defaultName = defaultName;

		if (topProvider==null)
			System.err.println("HierarchicalUniqueNameReferenceManager created with provider==null!");
	}


	public NamespaceProvider getNamespaceProvider(Node node) {

		NamespaceProvider provider = node2namespace.get(node);

		if (provider!=null) return provider;

		Node container = node.getParent();
		if (container != null)
			provider = Hierarchy.getNearestAncestor(container, NamespaceProvider.class);
		else
			// the root node does not have a provider
			return null;
			//provider = topProvider;


		if (provider!=null)
			node2namespace.put(node, provider);

		return provider;
	}


	public void setNamespaceProvider(Node node, NamespaceProvider provider) {
		setNamespaceProvider(node, this, provider);
	}

	public void setNamespaceProvider(Node node, HierarchicalUniqueNameReferenceManager nodeManager, NamespaceProvider provider) {

		if (provider==null) provider = topProvider;

		NamespaceProvider oldProvider = getNamespaceProvider(node);
		if (oldProvider==null) return;

		String name = nodeManager.getName(node);

		// clear cached data in the local and the foreign reference manager
		node2namespace.remove(node);
		nodeManager.node2namespace.remove(node);

		// do not assign name if it wasn't assigned in the first place (eg. the implicit place)
		if (name!=null&&(provider!=oldProvider||node2namespace!=nodeManager.node2namespace)) {
			NameManager<Node> oldMan = getNameManager(oldProvider);
			NameManager<Node> newMan = getNameManager(provider);

			oldMan.remove(node);

			Node checkNode = newMan.get(name);
			// we must assign some name in any case, be it an old or a new one
			if (checkNode == null)
				newMan.setName(node, name);
			else {
				newMan.setDefaultNameIfUnnamed(node);
				// additional call to propagate the name data after calling setDefaultNameIfUnnamed
				setName(node, newMan.getName(node));

			}
//			String n = newMan.getName(node);

		}
	}

	@Override
	public void attach(Node root) {

		if (existing != null) {
			//setExistingReference(root);

			for(Node n : Hierarchy.getDescendantsOfType(root, Node.class)) {
				setExistingReference(n);
			}
			existing = null;
		}

		super.attach(root);
	}


	protected NameManager<Node> createNameManager() {
		return new UniqueNameManager<Node>(defaultName);
	}

	protected NameManager<Node> getNameManager(NamespaceProvider provider) {


		if (provider==null) provider = topProvider;

		NameManager<Node> man = managers.get(provider);

		if (man==null) {
			man = createNameManager();
			managers.put(provider, man);
		}
		return man;
	}

	protected void setExistingReference(Node n) {
		String reference = existing.getReference(n);

		if (Identifier.isNumber(reference)) {
			String nm = getName(n);
			if (nm!=null)
				reference = nm;
		}

		if (reference != null) {

			String name =
					HierarchicalNames.getNameFromReference(reference);

			setName(n, name);
		}
	}



	public Node getNodeByReference(NamespaceProvider provider, String reference) {
		return getNodeByReference(provider, reference, false);
	}

	public Node getNodeByReferenceQuiet(NamespaceProvider provider, String reference) {
		return getNodeByReference(provider, reference, true);
	}

	public Node getNodeByReference(NamespaceProvider provider, String reference, boolean quiet) {

		if (provider!=null&&reference.equals("")) return (Node)provider;

		String head =  HierarchicalNames.getReferenceHead(reference);
		String tail =  HierarchicalNames.getReferenceTail(reference);

//		boolean isAbsolutePath = false;
//		if (reference.startsWith(HierarchicalNames.hierarchySeparator)) isAbsolutePath = true;
//		if (provider==null||isAbsolutePath) {
//			provider = topProvider;
//		}

		if (provider==null) provider = topProvider;

		NameManager<Node> man = getNameManager(provider);

		Node node;

		node = man.get(head);
		if (node==null) return null;

		if (node instanceof NamespaceProvider) {
			return getNodeByReference((NamespaceProvider)node, tail, quiet);
		}

		return node;
	}

	@Override
	public String getNodeReference(NamespaceProvider provider, Node node) {

		boolean isAbsolutePath = false;

		if (provider==null) {
			isAbsolutePath = true;
			provider = topProvider;
		}

		NamespaceProvider prov = null;
		String ret= "";

		do {
			prov = getNamespaceProvider(node);

			if (prov==null) break; // we've just reached the root, do not add it's name

			if (!ret.equals(""))
				ret= HierarchicalNames.hierarchySeparator+ret;


			String name = getNameManager(prov).getName(node);
			// the unnamed component just returns null
			if (name==null) return null;

			// for now don't use quotes
//			ret= HierarchicalNames.quoteType+name+ HierarchicalNames.quoteType+ret;
			ret= name+ret;
			node = node.getParent();

		} while (prov!=null&&provider!=prov);

		return (provider==topProvider&&isAbsolutePath? HierarchicalNames.hierarchySeparator:"")+ret;
	}


	@Override
	public void handleEvent(HierarchyEvent e) {

		if(e instanceof NodesAddedEvent) {
			for(Node node : e.getAffectedNodes()) {


				if (node.getParent()!=null) {
					// if it is not a root node
					NamespaceProvider provider = getNamespaceProvider(node);
					NameManager<Node> man = getNameManager(provider);
					man.setDefaultNameIfUnnamed(node);

					// additional call to propagate the name data after calling setDefaultNameIfUnnamed
					setName(node, man.getName(node));
				}

				for (Node node2 : Hierarchy.getDescendantsOfType(node, Node.class)) {
					getNameManager(getNamespaceProvider(node2)).setDefaultNameIfUnnamed(node2);
					// additional call to propagate the name data after calling setDefaultNameIfUnnamed
					setName(node2, getNameManager(getNamespaceProvider(node2)).getName(node2));
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

		NameManager<Node> man = getNameManager(getNamespaceProvider(node));
		man.setName(node, name);
	}


	public boolean isNamed(Node node) {
		NameManager<Node> man = getNameManager(getNamespaceProvider(node));
		return man.isNamed(node);
	}

	public String getName(Node node) {
		NameManager<Node> man = getNameManager(getNamespaceProvider(node));
		return man.getName(node);
	}


}
