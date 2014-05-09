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

public class HierarchicalUniqueNameReferenceManager extends HierarchySupervisor implements ReferenceManager {
	final private static String quoteType = "'";
	final private static String hierarchySeparator = "/";

	final private HashMap<NamespaceProvider, NameManager<Node>> managers = new HashMap<NamespaceProvider, NameManager<Node>>();

	// every node belongs to some name space provider (except the main root node of the model)
	final private HashMap<Node, NamespaceProvider> node2namespace = new HashMap<Node, NamespaceProvider>();
	final private NamespaceProvider topProvider; // namespace provided by root

	protected Func<Node, String> defaultName;
	private References existing;

	public HierarchicalUniqueNameReferenceManager(NamespaceProvider provider, References existing, Func<Node, String> defaultName) {
//		this (null, existing, defaultName);
		this.existing = existing;
		topProvider = provider;
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

	public void setNamespaceProvider(Node node, HierarchicalUniqueNameReferenceManager nodeManager, NamespaceProvider provider) {

		if (provider==null) provider = topProvider;

		NamespaceProvider oldProvider = getNamespaceProvider(node);
		if (oldProvider==null) return;

		node2namespace.remove(node);
		nodeManager.node2namespace.remove(node);


		if (provider!=oldProvider) {

			NameManager<Node> oldMan = getNameManager(oldProvider);
			NameManager<Node> newMan = getNameManager(provider);

			String name = nodeManager.getName(node);

			oldMan.remove(node);

			Node checkNode = newMan.get(name);
			// we must assign some name in any case, be it an old or a new one
			if (checkNode == null)
				newMan.setName(node, name);
			else
				newMan.setDefaultNameIfUnnamed(node);


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

	private void setExistingReference(Node n) {
		final String reference = existing.getReference(n);

		if (reference != null) {
			NamespaceProvider provider = getNamespaceProvider(n);
			NameManager<Node> man = getNameManager(provider);
			String name = getNameFromReference(reference);

			man.setName(n, name);
		}
	}

	// TODO: make it work with the embedded ' characters
	private static String hPattern = "(\\"+quoteType+"([^\\"+quoteType+"]+)\\"+quoteType+")(.*)";

	public static String getReferenceHead(String reference) {

		if (reference.startsWith(hierarchySeparator))
			reference = reference.substring(1);

		if (!reference.startsWith(quoteType))
			return reference;

		Pattern pattern = Pattern.compile(hPattern);

		Matcher matcher = pattern.matcher(reference);
		if (matcher.find()) {

			String head = matcher.group(2);
			return head;
		}

		return null;
	}

	public static String getReferenceTail(String reference) {

		if (reference.startsWith(hierarchySeparator))
			reference = reference.substring(1);

		if (!reference.startsWith(quoteType)) return "";
		Pattern pattern = Pattern.compile(hPattern);

		Matcher matcher = pattern.matcher(reference);

		if (matcher.find()) {
			String tail = matcher.group(3);
			return tail;
		}

		return null;
	}

	public static String getNameFromReference(String reference) {

		String head = getReferenceHead(reference);
		String tail = getReferenceTail(reference);

		if (tail.equals("")) return head;
		else
			return getNameFromReference(tail);

	}

	public Node getNodeByReference(NamespaceProvider provider, String reference) {

		if (provider!=null&&reference.equals("")) return (Node)provider;

		String head = getReferenceHead(reference);
		String tail = getReferenceTail(reference);

		boolean isAbsolutePath = false;

		if (reference.startsWith(hierarchySeparator)) isAbsolutePath = true;

		if (provider==null||isAbsolutePath) {
			provider = topProvider;
		}

		NameManager<Node> man = getNameManager(provider);

		Node node = man.get(head);

		if (node instanceof NamespaceProvider) {
			return getNodeByReference((NamespaceProvider)node, tail);
		}

		return null;
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
				ret=hierarchySeparator+ret;


			String name = getNameManager(prov).getName(node);
			// the unnamed component just returns null
			if (name==null) return null;

			ret=quoteType+name+quoteType+ret;
			node = node.getParent();

		} while (prov!=null&&provider!=prov);

		return (provider==topProvider&&isAbsolutePath?hierarchySeparator:"")+ret;
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
				}

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
