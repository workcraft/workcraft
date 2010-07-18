package org.workcraft.plugins.stg;

import java.util.HashMap;
import java.util.Map;

import org.workcraft.dom.references.IDGenerator;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.exceptions.DuplicateIDException;
import org.workcraft.exceptions.NotFoundException;
import org.workcraft.util.Func;
import org.workcraft.util.GeneralTwoWayMap;
import org.workcraft.util.Pair;
import org.workcraft.util.TwoWayMap;


/**
 * @author mech
 *
 * @param <T>
 */
public class InstanceManager<T>
{
	private GeneralTwoWayMap<T, Pair<String,Integer>> instances = new TwoWayMap<T, Pair<String, Integer>>();

	private Map<String, IDGenerator> generators = new HashMap<String, IDGenerator>();
	private final Func<T, String> labelGetter;

	public InstanceManager (Func<T, String> labelGetter) {
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

	public boolean contains(T t) {
		return instances.containsKey(t);
	}


	/**
	 * Automatically assign a new name to <i>t</i>, taking the name from label getter and auto-generating instance number.
	 */
	public void assign (T t) {
		final Pair<String, Integer> assigned = instances.getValue(t);
		final Integer instance;
		if (assigned != null)
			throw new ArgumentException ("Instance already assigned to \"" + labelGetter.eval(t) + "/" + assigned.getSecond() +"\"");
		final String label = labelGetter.eval(t);
		instance = getGenerator(label).getNextID();
		instances.put(t, new Pair<String, Integer>(label, instance));
	}

	/**
	 * Manually assign a new name to <i>t</i>, auto-generating instance number.
	 */
	public void assign (T t, String name) {
		assign (t, Pair.of(name, (Integer)null));
	}

	/**
	 * Manually assign an instance number to <i>t</i>.
	 */
	public void assign (T t, int instance) {
		assign (t, Pair.of(labelGetter.eval(t), instance));
	}

	/**
	 * Manually assign a full reference to <i>t</i>, forcing instance number.
	 */
	public void assign (T t, Pair<String, Integer> reference) {
		final Pair<String, Integer> assigned = instances.getValue(t);

		if (reference.getSecond() == null) {
			if (assigned != null) {
				if (assigned.getFirst().equals(reference.getFirst())) // already registered with same name
					return;
				// release old instance
				remove (t);
			}

			instances.put(t, Pair.of(reference.getFirst(), getGenerator(reference.getFirst()).getNextID()));
		}
		else {
			// check if desired instance is already taken
			final T refHolder = instances.getKey(reference);

			// requested instance already taken by t, do nothing
			if (refHolder == t)
					return;

			// requested instance taken by somebody else
			if(refHolder != null)
					throw new DuplicateIDException (reference.getSecond());

			// release old instance
			if (assigned != null)
				remove(t);

			instances.put(t, reference);
			getGenerator(reference.getFirst()).reserveID(reference.getSecond());
		}
	}

	public Pair<String, Integer> getInstance (T t) {
		return instances.getValue(t);
	}

	public T getObject(Pair<String, Integer> ref) {
		return instances.getKey(ref);
	}

	public void remove(T T) {
		final Pair<String, Integer> assignment = instances.getValue(T);
		if(assignment == null)
			throw new NotFoundException("Instance not assigned");
		generators.get(assignment.getFirst()).releaseID(assignment.getSecond());
		instances.removeKey(T);
	}
}
