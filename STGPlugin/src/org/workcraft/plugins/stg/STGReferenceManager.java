package org.workcraft.plugins.stg;

import java.util.Collection;

import org.workcraft.dom.Node;
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

//	@Override
//	public void attach(Node root) {
//		if (root == null) {
//			throw new NullPointerException();
//		}
//
//		if (existingReferences != null) {
//			setExistingReference(root);
//			for (Node n: Hierarchy.getDescendantsOfType(root, Node.class)) {
//				setExistingReference(n);
//			}
//			existingReferences = null;
//		}
//
//		super.attach(root);
//	}
//
	@Override
	protected STGNameManager createNameManager() {
		return new STGNameManager(defaultName);
	}

//	protected NameManager<Node> createNameManager() {
//	});
//
//	this.instancedNameManager = new InstanceManager<Node>(new Func<Node, String>() {
//		@Override
//		public String eval(Node arg) {
//			if (arg instanceof SignalTransition) {
//				return ((SignalTransition) arg).getSignalName() + ((SignalTransition) arg).getDirection();
//			} else if (arg instanceof DummyTransition) {
//				return ((DummyTransition)arg).getName();
//			} else {
//				throw new RuntimeException ("Unexpected class " + arg.getClass().getName());
//			}
//		}
//	});
//		return new UniqueNameManager<Node>(defaultName);
//	}

	@Override
	protected void setExistingReference(Node n) {
		if (n instanceof STGPlace && ((STGPlace)n).isImplicit()) return;
		super.setExistingReference(n);
	}

//	private void setExistingReference(Node n) {
//		final String reference = existingReferences.getReference(n);
//		if (reference != null) {
//			if (n instanceof STGPlace) {
//				if (! ((STGPlace) n).isImplicit()) {
//					setName (n, reference);
//				}
//			} else {
//				setName (n, reference);
//			}
//		}
//	}

//	@Override
//	public Node getNodeByReference(NamespaceProvider provider, String reference) {
//
//		Pair<String, Integer> instancedName = LabelParser.parseInstancedTransition(reference);
//
//		if (instancedName != null)	{
//			if (instancedName.getSecond() == null) {
//				instancedName = Pair.of(instancedName.getFirst(), 0);
//			}
//			Node node = instancedNameManager.getObject(instancedName);
//			if (node != null) {
//				return node;
//			}
//		}
//
//		return super.getNodeByReference(provider, reference);
//	}

//	@Override
//	public String getNodeReference(NamespaceProvider provider, Node node) {
//
//		if (node instanceof SignalTransition) {
//			final SignalTransition st = (SignalTransition)node;
//			final Integer instance = instancedNameManager.getInstance(st).getSecond();
//			if (instance == 0)
//				return st.getSignalName() + st.getDirection();
//			else
//				return st.getSignalName() + st.getDirection() + "/" + instance;
//
//		} else if (node instanceof Transition) {
//			final Transition t = (Transition)node;
//			final Pair<String, Integer> name = instancedNameManager.getInstance(t);
//			if (name.getSecond() == 0) {
//				return name.getFirst();
//			} else {
//				return name.getFirst() + "/" + name.getSecond();
//			}
//		}
//		return defaultNameManager.getName(node);
//
//	}


//	@Override
//	public void handleEvent(HierarchyEvent e) {
//		if(e instanceof NodesDeletedEvent)
//			for(Node node : e.getAffectedNodes()) {
//				nodeRemoved(node);
//				for (Node n : Hierarchy.getDescendantsOfType(node, Node.class))
//					nodeRemoved(n);
//			}
//		if(e instanceof NodesAddedEvent)
//			for(Node node : e.getAffectedNodes()) {
//				setDefaultNameIfUnnamed(node);
//				for (Node n : Hierarchy.getDescendantsOfType(node, Node.class))
//					setDefaultNameIfUnnamed(n);
//			}
//	}




//	private void nodeRemoved(Node node) {
//		if (node instanceof SignalTransition) {
//			final SignalTransition st = (SignalTransition)node;
//			signalTransitions.remove(st.getSignalName(), st);
//			instancedNameManager.remove(st);
//		}
//		if (node instanceof DummyTransition) {
//			final DummyTransition dt = (DummyTransition)node;
//			dummyTransitions.remove(dt.getName(), dt);
//			instancedNameManager.remove(dt);
//		} else {
//			defaultNameManager.remove(node);
//		}
//	}

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
