package org.workcraft.dom.references;

import java.util.HashMap;

import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
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
	private NamespaceProvider topProvider; // namespace provided by root

	protected Func<Node, String> defaultName;
	private References existing;

	public HierarchicalUniqueNameReferenceManager(References existing, Func<Node, String> defaultName) {

		this.existing = existing;
		this.defaultName = defaultName;

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

	public void setNamespaceProvider(Node node, HierarchicalUniqueNameReferenceManager sourceReferenceManager, NamespaceProvider provider) {

		if (provider==null) provider = topProvider;

		NamespaceProvider oldProvider = sourceReferenceManager.getNamespaceProvider(node);
		if (oldProvider==null) return;

		String name = sourceReferenceManager.getName(node);

		// clear cached data in the local and the foreign reference manager
		node2namespace.remove(node);
		sourceReferenceManager.node2namespace.remove(node);

		// do not assign name if it wasn't assigned in the first place (eg. the implicit place)
		if (name!=null&&(provider!=oldProvider||node2namespace!=sourceReferenceManager.node2namespace)) {
			NameManager<Node> oldMan = sourceReferenceManager.getNameManager(oldProvider);
			NameManager<Node> newMan = getNameManager(provider);

			oldMan.remove(node);

			Node checkNode = newMan.get(name);
			// we must assign some name in any case, be it an old or a new one
			if (checkNode == null)
				newMan.setName(node, name);
			else {
				newMan.setDefaultNameIfUnnamed(node, name);
				// the node was not added yet as a child of the target container, so using setName from the reference manager is not possible yet
			}

		}
	}

	@Override
	public void attach(Node root) {
		// root must be a namespace provider
		topProvider = (NamespaceProvider)root;


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
					NamespaceHelper.getNameFromReference(reference);

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

		if (topProvider==null)
			System.err.println("HierarchicalUniqueNameReferenceManager created with topProvider==null!");

		if (provider==null) provider = topProvider;

		if (reference.equals("")||reference.equals(NamespaceHelper.hierarchySeparator))
			return (Node)provider;

		String head =  NamespaceHelper.getReferenceHead(reference);
		String tail =  NamespaceHelper.getReferenceTail(reference);

//		boolean isAbsolutePath = false;
//		if (reference.startsWith(HierarchicalNames.hierarchySeparator)) isAbsolutePath = true;
//		if (provider==null||isAbsolutePath) {
//			provider = topProvider;
//		}

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

		//boolean isAbsolutePath = false;

		if (provider==null) {
			//isAbsolutePath = true;
			provider = topProvider;
		}

		NamespaceProvider prov = null;
		String ret= "";

		do {
			prov = getNamespaceProvider(node);

			if (prov==null) break; // we've just reached the root, do not add it's name

			if (!ret.equals(""))
				ret= NamespaceHelper.hierarchySeparator+ret;


			String name = getNameManager(prov).getName(node);
			// the unnamed component just returns null
			if (name==null) return null;

			// for now don't use quotes
//			ret= HierarchicalNames.quoteType+name+ HierarchicalNames.quoteType+ret;
			ret= name+ret;
			node = node.getParent();

		} while (prov!=null&&provider!=prov);

		return ret;
		// uncomment if the absolute path begins with the separator
		// return (provider==topProvider&&isAbsolutePath? HierarchicalNames.hierarchySeparator:"")+ret;
	}


	@Override
	public void handleEvent(HierarchyEvent e) {

		if (e instanceof NodesAddedEvent) {
			for(Node node : e.getAffectedNodes()) {

				if (node.getParent()!=null) {
					// if it is not a root node
					NamespaceProvider provider = getNamespaceProvider(node);
					NameManager<Node> man = getNameManager(provider);
					man.setDefaultNameIfUnnamed(node);
					String name = man.getName(node);
					// additional call to propagate the name data after calling setDefaultNameIfUnnamed
					setName(node, name);
				}

				for (Node node2 : Hierarchy.getDescendantsOfType(node, Node.class)) {
					getNameManager(getNamespaceProvider(node2)).setDefaultNameIfUnnamed(node2, null);
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
