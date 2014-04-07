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
			UniqueNameManager<Node> newMan = getNameManager(provider);
			oldMan.remove(node);

			node2namespace.put(node,  provider);

			Node checkNode = newMan.get(name);
			// we must assign some name in any case, be it an old or a new one
			if (checkNode == null)
				setName(node, name);
			else
				newMan.setDefaultNameIfUnnamed(node);
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

	// TODO: make it work with the embedded ' characters
	private static String hPattern = "(\\"+quoteType+"[^\\"+quoteType+"]+\\"+quoteType+")(.*)";

	public static String getReferenceHead(String reference) {

		if (reference.startsWith(hierarchySeparator))
			reference = reference.substring(1);

		if (!reference.startsWith("+quoteType+")) return reference;
		Pattern pattern = Pattern.compile(hPattern);

		Matcher matcher = pattern.matcher(reference);
		if (matcher.find()) {

			String head = matcher.group(1);
			return head;
		}

		return null;
	}

	public static String getReferenceTail(String reference) {

		if (reference.startsWith(hierarchySeparator))
			reference = reference.substring(1);

		if (!reference.startsWith(quoteType)) return reference;
		Pattern pattern = Pattern.compile(hPattern);

		Matcher matcher = pattern.matcher(reference);

		if (matcher.find()) {
			String tail = matcher.group(2);
			return tail;
		}

		return null;
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

		boolean isAbsolutePath = false;

		if (provider==null) {
			isAbsolutePath = true;
			provider = topProvider;
		}

		NamespaceProvider prov = getNamespaceProvider(node);
		String ret= "";

		do {
			prov = getNamespaceProvider(node);
			if (!ret.equals(""))
				ret=hierarchySeparator+ret;

			ret=quoteType+getNameManager(prov).getName(node)+quoteType+ret;
			node = node.getParent();

		} while (prov!=null&&provider!=prov);



		return (isAbsolutePath?hierarchySeparator:"")+ret;
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
