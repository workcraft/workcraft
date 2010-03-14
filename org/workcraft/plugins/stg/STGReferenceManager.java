package org.workcraft.plugins.stg;

import java.util.Collection;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.dom.references.UniqueNameManager;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.exceptions.DuplicateIDException;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.observation.NodesDeletedEvent;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.Identifier;
import org.workcraft.util.ListMap;
import org.workcraft.util.Pair;
import org.workcraft.util.Triple;

public class STGReferenceManager extends HierarchySupervisor implements ReferenceManager {
	private InstanceManager<SignalTransition> instanceManager;
	private UniqueNameManager<Node> uniqueNameManager;
	private References existingReferences;

	private ListMap<String, SignalTransition> transitions = new ListMap<String, SignalTransition>();

	private int counter = 0;

	public STGReferenceManager(References existingReferences) {
		this.existingReferences = existingReferences;

		this.uniqueNameManager = new UniqueNameManager<Node>(new Func<Node, String>() {
			@Override
			public String eval(Node arg) {
				if (arg instanceof Place)
					return "p";
				if (arg instanceof Transition)
					return "dummy";
				if (arg instanceof Connection)
					return "con";
				if (arg instanceof Container)
					return "group";
				return "node";
			}
		});

		this.instanceManager = new InstanceManager<SignalTransition>(new Func<SignalTransition, String>() {
			@Override
			public String eval(SignalTransition arg) {
				return arg.getSignalName() + arg.getDirection();
			}
		});
	}

	@Override
	public void attach(Node root) {
		if (root == null)
			throw new NullPointerException();

		if (existingReferences != null) {
			setExistingReference(root);
			for (Node n: Hierarchy.getDescendantsOfType(root, Node.class))
				setExistingReference(n);
			existingReferences = null;
		}

		super.attach(root);
	}

	private void setExistingReference(Node n) {
		final String reference = existingReferences.getReference(n);
		if (reference != null)
			setName (n, reference);
	}

	@Override
	public Node getNodeByReference(String reference) {
		try {
			return instanceManager.getObject(LabelParser.parse(reference));
		} catch (ArgumentException e) {
			return uniqueNameManager.get(reference);
		}
	}

	@Override
	public String getNodeReference(Node node) {
		if (node instanceof SignalTransition) {
			final SignalTransition st = (SignalTransition)node;
			return st.getSignalName() + st.getDirection() + "/" + instanceManager.getInstance(st).getSecond();
		} else
			return uniqueNameManager.getName(node);
	}

	public String getName (Node node) {
		if (node instanceof SignalTransition)
			return ((SignalTransition)node).getSignalName();
		else
			return uniqueNameManager.getName(node);
	}

	public Collection<SignalTransition> getSignalTransitions(String signalName) {
		return transitions.get(signalName);
	}

	public int getInstanceNumber (SignalTransition st) {
		return instanceManager.getInstance(st).getSecond();
	}

	public void setName(Node node, String s) {
		if (node instanceof SignalTransition) {
			final SignalTransition st = (SignalTransition)node;

			try {
				final Triple<String, Direction, Integer> r = LabelParser.parseFull(s);
				instanceManager.assign(st, Pair.of(r.getFirst()+r.getSecond(), r.getThird()));

				transitions.remove(st.getSignalName(), st);
				transitions.put(r.getFirst(), st);

				st.setSignalName(r.getFirst());
				st.setDirection(r.getSecond());
			} catch (DuplicateIDException e) {
				throw new ArgumentException ("Instance number " + e.getId() + " is already taken.");
			} catch (ArgumentException e) {
				if (Identifier.isValid(s)) {
					transitions.remove(s, st);
					transitions.put(s, st);

					st.setSignalName(s);
				} else
					throw new ArgumentException ("\"" + s + "\" is not a valid signal transition label.");
			}
		}
		else
			uniqueNameManager.setName(node, s);
	}


	@Override
	public void handleEvent(HierarchyEvent e) {
		if(e instanceof NodesDeletedEvent)
			for(Node node : e.getAffectedNodes()) {
				nodeRemoved(node);
				for (Node n : Hierarchy.getDescendantsOfType(node, Node.class))
					nodeRemoved(n);
			}
		if(e instanceof NodesAddedEvent)
			for(Node node : e.getAffectedNodes()) {
				setDefaultNameIfUnnamed(node);
				for (Node n : Hierarchy.getDescendantsOfType(node, Node.class))
					setDefaultNameIfUnnamed(n);
			}
	}

	private void setDefaultNameIfUnnamed(Node node) {
		if (node instanceof SignalTransition) {
			final SignalTransition st = (SignalTransition)node;

			if (instanceManager.contains(st))
				return;

			String name = "signal" + counter++;
			st.setSignalName(name);
			transitions.put(name, st);
			instanceManager.assign(st);
		} else
			uniqueNameManager.setDefaultNameIfUnnamed(node);
	}

	private void nodeRemoved(Node node) {
		if (node instanceof SignalTransition) {
			final SignalTransition st = (SignalTransition)node;
			transitions.remove(st.getSignalName(), st);
			instanceManager.remove(st);
		} else
			uniqueNameManager.remove(node);
	}
}