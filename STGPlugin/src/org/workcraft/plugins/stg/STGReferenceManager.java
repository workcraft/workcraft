package org.workcraft.plugins.stg;

import java.util.Collection;

import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Pair;

public class STGReferenceManager extends HierarchicalUniqueNameReferenceManager implements ReferenceManager {

	public STGReferenceManager(References existing, Func<Node, String> defaultName) {
		super(existing, defaultName);
	}

	@Override
	protected STGNameManager createNameManager() {
		return new STGNameManager(defaultName);
	}

	@Override
	protected void setExistingReference(Node n) {
		if (n instanceof STGPlace && ((STGPlace)n).isImplicit()) return;
		super.setExistingReference(n);
	}

	private STGNameManager getNameManager(Node node) {
		NamespaceProvider namespaceProvider = getNamespaceProvider(node);
		STGNameManager nameManager = (STGNameManager)getNameManager(namespaceProvider);
		return nameManager;
	}

	public Pair<String, Integer> getNamePair(Node node) {
		return getNameManager(node).getNamePair(node);
	}

	public int getInstanceNumber (Node node) {
		return getNameManager(node).getInstanceNumber(node);
	}

	public void setInstanceNumber (Node node, int number) {
		getNameManager(node).setInstanceNumber(node, number);
	}

	public Collection<SignalTransition> getSignalTransitions(String signalReference) {
		String parentReference = NamespaceHelper.getParentReference(signalReference);
		Node parent = getNodeByReference(null, parentReference);
		String signalName = NamespaceHelper.getNameFromReference(signalReference);
		return getNameManager(parent).getSignalTransitions(signalName);
	}

	public void setDefaultNameIfUnnamed(Node node) {
		getNameManager(node).setDefaultNameIfUnnamed(node);
	}


	public void setName(Node node, String s, boolean forceInstance) {
		getNameManager(node).setName(node, s, forceInstance);
	}

}
