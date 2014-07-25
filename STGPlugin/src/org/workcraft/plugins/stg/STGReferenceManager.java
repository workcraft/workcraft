package org.workcraft.plugins.stg;

import java.util.Collection;

import org.workcraft.dom.Node;
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

	public Pair<String, Integer> getNamePair(Node node) {
		return ((STGNameManager)getNameManager(getNamespaceProvider(node))).getNamePair(node);
	}

	public int getInstanceNumber (Node st) {
		return ((STGNameManager)getNameManager(getNamespaceProvider(st))).getInstanceNumber(st);
	}

	public void setInstanceNumber (Node st, int number) {
		((STGNameManager)getNameManager(getNamespaceProvider(st))).setInstanceNumber(st, number);
	}

	public Collection<SignalTransition> getSignalTransitions(String signalName) {
		return ((STGNameManager)getNameManager(null)).getSignalTransitions(signalName);

	}

	public void setDefaultNameIfUnnamed(Node node) {
		((STGNameManager)getNameManager(getNamespaceProvider(node))).setDefaultNameIfUnnamed(node);
	}


	public void setName(Node node, String s, boolean forceInstance) {
		((STGNameManager)getNameManager(getNamespaceProvider(node))).setName(node, s, forceInstance);
	}

}
