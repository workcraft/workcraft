package org.workcraft.plugins.petri;

import java.util.HashMap;
import java.util.Map;

import org.workcraft.dom.Container;
import org.workcraft.dom.IDGenerator;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.exceptions.DuplicateIDException;
import org.workcraft.exceptions.NotFoundException;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.observation.NodesDeletedEvent;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.GeneralTwoWayMap;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.Pair;
import org.workcraft.util.TwoWayMap;


public class ReferenceManager<T>
{
	private GeneralTwoWayMap<T, Pair<String,Integer>> instances = new TwoWayMap<T, Pair<String, Integer>>();

	private Map<String, IDGenerator> generators = new HashMap<String, IDGenerator>();
	private final Func<T, String> labelGetter;

	public ReferenceManager (Func<T, String> labelGetter) {
		if(labelGetter == null)
			throw new NullPointerException();
		this.labelGetter = labelGetter;
	}

	private IDGenerator getGenerator(String label)
	{
		IDGenerator result = generators.get(label);
		if(result == null)
		{
			result = new IDGenerator();
			generators.put(label, result);
		}
		return result;
	}

	public void assign (T t) {
		final Pair<String, Integer> assigned = instances.getValue(t);
		final Integer instance;
		if (assigned != null)
			throw new ArgumentException ("Instance already assigned to \"" + labelGetter.eval(t) + "/" + assigned.getSecond() +"\"");
		final String label = labelGetter.eval(t);
		instance = getGenerator(label).getNextID();
		instances.put(t, new Pair<String, Integer>(label, instance));
	}

	private Pair<String, Integer> parseReference (String ref) {
		final int slash = ref.lastIndexOf('/');
		if(slash == -1)
			throw new ArgumentException("Invalid reference format. Expected format: 'label/id'");
		final String label = ref.substring(0, slash);
		final Integer instance = Integer.parseInt(ref.substring(slash+1));
		return new Pair<String, Integer> (label, instance);
	}

	public void assign (T t, String reference) {
		String nodeLabel = labelGetter.eval(t);
		Pair<String, Integer> ref = parseReference (reference);

		final T refHolder = instances.getKey(ref);
		if (refHolder == t)
			return;

		if(refHolder != null)
			throw new DuplicateIDException(ref.getSecond().intValue());

		if (!ref.getFirst().equals(nodeLabel))
			throw new ArgumentException ("Label mismatch: expected \"" + ref.getFirst() +"\", got \"" + nodeLabel +"\"" );


		Pair<String, Integer> oldID = instances.getValue(t);
		if (oldID != null)
			remove(t);

		instances.put(t, ref);
		getGenerator(nodeLabel).reserveID(ref.getSecond());
	}

	public String getReference (T t) {
		Pair<String, Integer> ref = instances.getValue(t);
		if(ref == null)
			throw new NotFoundException("Instance not assigned");
		if (!ref.getFirst().equals(labelGetter.eval(t)))
		{
			if(ref != null)
				remove(t);
			assign(t);
			ref = instances.getValue(t);
		}
		return ref.getFirst() + "/" + ref.getSecond();
	}

	public T getObject(String reference) {
		return instances.getKey(parseReference(reference));
	}

	public void remove(T T) {
		final Pair<String, Integer> assignment = instances.getValue(T);
		if(assignment == null)
			throw new NotFoundException("Instance not assigned");
		generators.get(assignment.getFirst()).releaseID(assignment.getSecond());
		instances.removeKey(T);
	}

	public static void attach (final ReferenceManager<Node> referenceManager, Container root, References existingReferences) {
		if(root != null)
			for (Node n: Hierarchy.getDescendantsOfType(root, Node.class)) {
				final String reference = existingReferences.getReference(n);
				if (reference != null)
					referenceManager.assign(n, reference);
			}

		HierarchySupervisor hs = new HierarchySupervisor() {
			@Override
			public void handleEvent(HierarchyEvent e) {
				if(e instanceof NodesDeletedEvent)
					for(Node node : e.getAffectedNodes())
						referenceManager.remove(node);
				if(e instanceof NodesAddedEvent)
					for(Node node : e.getAffectedNodes())
						referenceManager.assign(node);
			}
		};

		hs.attach(root, false);
	}
}
